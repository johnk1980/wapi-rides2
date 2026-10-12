-- =============================================================================
-- WAPI RIDES — PHASE 4.7B
-- MESSAGING SYSTEM: DATABASE FOUNDATION (NON-RECURSIVE RLS & BOOKING CONVERSATION RPC)
-- =============================================================================
-- Step 0 (Optional Pre-Check in Supabase SQL Editor):
--   SELECT tablename, policyname, cmd, roles, qual, with_check
--   FROM pg_policies
--   WHERE schemaname = 'public'
--     AND tablename IN ('conversations', 'conversation_members', 'messages')
--   ORDER BY tablename, policyname;
-- =============================================================================

BEGIN;

-- -----------------------------------------------------------------------------
-- 1. CREATE NON-RECURSIVE MEMBERSHIP HELPER
--    Bypasses conversation_members RLS via SECURITY DEFINER and checks only
--    whether auth.uid() is a member of p_conversation_id.
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.is_conversation_member(p_conversation_id uuid)
RETURNS boolean
LANGUAGE sql
STABLE
SECURITY DEFINER
SET search_path = public
AS $$
    SELECT EXISTS (
        SELECT 1
        FROM public.conversation_members cm
        WHERE cm.conversation_id = p_conversation_id
          AND cm.user_id = auth.uid()
    );
$$;

REVOKE ALL ON FUNCTION public.is_conversation_member(uuid) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.is_conversation_member(uuid) FROM anon;
GRANT EXECUTE ON FUNCTION public.is_conversation_member(uuid) TO authenticated;

-- -----------------------------------------------------------------------------
-- 2. REPLACE CONVERSATIONS RLS
--    Dynamically drop any existing policies on public.conversations first so
--    no recursive SELECT or unintended client INSERT/UPDATE/DELETE policy remains,
--    then create ONLY "conversations_select_member".
-- -----------------------------------------------------------------------------
ALTER TABLE public.conversations ENABLE ROW LEVEL SECURITY;

DO $$
DECLARE
    v_pol record;
BEGIN
    FOR v_pol IN
        SELECT policyname
        FROM pg_policies
        WHERE schemaname = 'public'
          AND tablename = 'conversations'
    LOOP
        EXECUTE format('DROP POLICY IF EXISTS %I ON public.conversations', v_pol.policyname);
    END LOOP;
END
$$;

CREATE POLICY "conversations_select_member"
ON public.conversations
FOR SELECT
TO authenticated
USING (
    public.is_conversation_member(id)
);

-- -----------------------------------------------------------------------------
-- 3. REPLACE CONVERSATION_MEMBERS RLS
--    Dynamically drop any existing policies on public.conversation_members first
--    so no recursive SELECT or unintended client INSERT/UPDATE/DELETE policy remains,
--    then create ONLY "conversation_members_select_member".
-- -----------------------------------------------------------------------------
ALTER TABLE public.conversation_members ENABLE ROW LEVEL SECURITY;

DO $$
DECLARE
    v_pol record;
BEGIN
    FOR v_pol IN
        SELECT policyname
        FROM pg_policies
        WHERE schemaname = 'public'
          AND tablename = 'conversation_members'
    LOOP
        EXECUTE format('DROP POLICY IF EXISTS %I ON public.conversation_members', v_pol.policyname);
    END LOOP;
END
$$;

CREATE POLICY "conversation_members_select_member"
ON public.conversation_members
FOR SELECT
TO authenticated
USING (
    public.is_conversation_member(conversation_id)
);

-- -----------------------------------------------------------------------------
-- 4. REPLACE MESSAGES RLS
--    Dynamically drop any existing policies on public.messages first, then
--    create ONLY:
--      - messages_select_member (SELECT)
--      - messages_insert_member (INSERT)
--      - messages_update_read_member (UPDATE - other participant's messages only)
--    (No DELETE policy)
-- -----------------------------------------------------------------------------
ALTER TABLE public.messages ENABLE ROW LEVEL SECURITY;

DO $$
DECLARE
    v_pol record;
BEGIN
    FOR v_pol IN
        SELECT policyname
        FROM pg_policies
        WHERE schemaname = 'public'
          AND tablename = 'messages'
    LOOP
        EXECUTE format('DROP POLICY IF EXISTS %I ON public.messages', v_pol.policyname);
    END LOOP;
END
$$;

CREATE POLICY "messages_select_member"
ON public.messages
FOR SELECT
TO authenticated
USING (
    public.is_conversation_member(conversation_id)
);

CREATE POLICY "messages_insert_member"
ON public.messages
FOR INSERT
TO authenticated
WITH CHECK (
    sender_id = auth.uid()
    AND public.is_conversation_member(conversation_id)
    AND length(btrim(message)) > 0
);

CREATE POLICY "messages_update_read_member"
ON public.messages
FOR UPDATE
TO authenticated
USING (
    public.is_conversation_member(conversation_id)
    AND sender_id <> auth.uid()
)
WITH CHECK (
    public.is_conversation_member(conversation_id)
    AND sender_id <> auth.uid()
);

-- -----------------------------------------------------------------------------
-- 5. CREATE BOOKING CONVERSATION RPC
--    public.get_or_create_booking_conversation(p_booking_id uuid)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.get_or_create_booking_conversation(p_booking_id uuid)
RETURNS public.conversations
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_caller_id uuid := auth.uid();
    v_booking public.bookings%ROWTYPE;
    v_conversation public.conversations%ROWTYPE;
BEGIN
    -- A. Require auth.uid()
    IF v_caller_id IS NULL THEN
        RAISE EXCEPTION 'Authentication required';
    END IF;

    -- B. Load and lock the booking row
    SELECT *
    INTO v_booking
    FROM public.bookings
    WHERE id = p_booking_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Booking not found';
    END IF;

    -- C. Require the authenticated user to be either driver_id or passenger_id
    IF v_caller_id IS DISTINCT FROM v_booking.driver_id
       AND v_caller_id IS DISTINCT FROM v_booking.passenger_id THEN
        RAISE EXCEPTION 'Not a participant in this booking';
    END IF;

    -- D. Only allow conversation creation/access for confirmed, in_progress, or completed bookings
    IF v_booking.status NOT IN ('confirmed', 'in_progress', 'completed') THEN
        RAISE EXCEPTION 'Conversation unavailable for this booking status';
    END IF;

    -- E. Find an existing conversation by booking_id = v_booking.id
    SELECT c.*
    INTO v_conversation
    FROM public.conversations c
    WHERE c.booking_id = v_booking.id
    LIMIT 1;

    -- F. If not found, create public.conversations(trip_id, booking_id)
    IF NOT FOUND OR v_conversation.id IS NULL THEN
        INSERT INTO public.conversations (
            trip_id,
            booking_id
        )
        VALUES (
            v_booking.trip_id,
            v_booking.id
        )
        RETURNING * INTO v_conversation;
    END IF;

    -- G. Idempotently insert BOTH legitimate participants into public.conversation_members
    INSERT INTO public.conversation_members (
        conversation_id,
        user_id
    )
    VALUES
        (v_conversation.id, v_booking.driver_id),
        (v_conversation.id, v_booking.passenger_id)
    ON CONFLICT DO NOTHING;

    -- H. Return the complete conversation row
    RETURN v_conversation;
END;
$$;

-- -----------------------------------------------------------------------------
-- 6. FUNCTION PERMISSIONS
-- -----------------------------------------------------------------------------
REVOKE ALL ON FUNCTION public.get_or_create_booking_conversation(uuid)
FROM PUBLIC;

REVOKE ALL ON FUNCTION public.get_or_create_booking_conversation(uuid)
FROM anon;

GRANT EXECUTE ON FUNCTION public.get_or_create_booking_conversation(uuid)
TO authenticated;

COMMIT;
