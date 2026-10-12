-- =============================================================================
-- WAPI RIDES — PHASE 4.5
-- Ratings & Trust System PostgreSQL RPC Function
--
-- Function:
--   public.submit_rating(
--       p_booking_id uuid,
--       p_rating integer,
--       p_review text
--   )
--
-- Security & Data Integrity Rules:
--   - SECURITY DEFINER with SET search_path = public
--   - Requires authenticated caller via auth.uid()
--   - Never accepts reviewer_id, reviewee_id, or trip_id from the Android client
--   - Derives reviewer_id, reviewee_id, and trip_id exclusively from the locked
--     public.bookings row and auth.uid()
--   - Requires booking.status = 'completed'
--   - Validates p_rating BETWEEN 1 AND 5
--   - Trims review text and stores NULL when empty/blank
--   - Prevents duplicate rating for the same (booking_id, reviewer_id)
--   - Concurrently locks the reviewee's public.profiles row and recalculates
--     rating_count = COUNT(*) and rating_average = ROUND(AVG(rating)::numeric, 2)
--     clamped to [1.00, 5.00] from public.ratings
--   - Does NOT modify completed_trips during this phase
-- =============================================================================

CREATE OR REPLACE FUNCTION public.submit_rating(
    p_booking_id uuid,
    p_rating integer,
    p_review text DEFAULT NULL
)
RETURNS public.ratings
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_caller_id uuid := auth.uid();
    v_booking public.bookings%ROWTYPE;
    v_trip public.trips%ROWTYPE;
    v_reviewer_id uuid;
    v_reviewee_id uuid;
    v_clean_review text;
    v_inserted_rating public.ratings%ROWTYPE;
    v_new_count integer;
    v_new_avg numeric(3,2);
BEGIN
    -- 1. Require authenticated caller
    IF v_caller_id IS NULL THEN
        RAISE EXCEPTION 'Authentication required';
    END IF;

    -- 2. Validate rating range (1 to 5)
    IF p_rating IS NULL OR p_rating < 1 OR p_rating > 5 THEN
        RAISE EXCEPTION 'Rating must be between 1 and 5';
    END IF;

    -- 3. Lock and verify booking exists
    SELECT * INTO v_booking
    FROM public.bookings
    WHERE id = p_booking_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Booking not found';
    END IF;

    -- 4. Booking status must be 'completed'
    IF v_booking.status <> 'completed' THEN
        RAISE EXCEPTION 'Booking is not completed';
    END IF;

    -- 5. Verify booking.trip_id corresponds to an existing trip
    SELECT * INTO v_trip
    FROM public.trips
    WHERE id = v_booking.trip_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Booking not found';
    END IF;

    -- 6. Determine participant relationship strictly from auth.uid() and booking
    IF v_caller_id = v_booking.passenger_id AND v_caller_id <> v_booking.driver_id THEN
        v_reviewer_id := v_booking.passenger_id;
        v_reviewee_id := v_booking.driver_id;
    ELSIF v_caller_id = v_booking.driver_id AND v_caller_id <> v_booking.passenger_id THEN
        v_reviewer_id := v_booking.driver_id;
        v_reviewee_id := v_booking.passenger_id;
    ELSE
        RAISE EXCEPTION 'User is not a participant';
    END IF;

    IF v_reviewer_id = v_reviewee_id THEN
        RAISE EXCEPTION 'User is not a participant';
    END IF;

    -- 7. Prevent duplicate rating for the same booking by the same reviewer
    IF EXISTS (
        SELECT 1
        FROM public.ratings
        WHERE booking_id = v_booking.id
          AND reviewer_id = v_reviewer_id
    ) THEN
        RAISE EXCEPTION 'Already rated this booking';
    END IF;

    -- 8. Trim review text; allow NULL/empty review
    v_clean_review := NULLIF(BTRIM(COALESCE(p_review, '')), '');

    -- 9. Insert rating into public.ratings (protected by unique_reviewer_booking constraint)
    BEGIN
        INSERT INTO public.ratings (
            trip_id,
            booking_id,
            reviewer_id,
            reviewee_id,
            rating,
            review
        )
        VALUES (
            v_booking.trip_id,
            v_booking.id,
            v_reviewer_id,
            v_reviewee_id,
            p_rating,
            v_clean_review
        )
        RETURNING * INTO v_inserted_rating;
    EXCEPTION
        WHEN unique_violation THEN
            RAISE EXCEPTION 'Already rated this booking';
    END;

    -- 10. Lock reviewee profile row for safe concurrent rating aggregation
    PERFORM 1
    FROM public.profiles
    WHERE id = v_reviewee_id
    FOR UPDATE;

    SELECT
        COUNT(*)::integer,
        CASE
            WHEN COUNT(*) = 0 THEN 0.00
            ELSE LEAST(5.00, GREATEST(1.00, ROUND(AVG(rating)::numeric, 2)))
        END
    INTO v_new_count, v_new_avg
    FROM public.ratings
    WHERE reviewee_id = v_reviewee_id;

    UPDATE public.profiles
    SET rating_count = COALESCE(v_new_count, 0),
        rating_average = COALESCE(v_new_avg, 0.00)
    WHERE id = v_reviewee_id;

    RETURN v_inserted_rating;
END;
$$;

REVOKE ALL ON FUNCTION public.submit_rating(uuid, integer, text) FROM PUBLIC;
GRANT EXECUTE ON FUNCTION public.submit_rating(uuid, integer, text) TO authenticated;
