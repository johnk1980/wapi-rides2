-- =============================================================================
-- WAPI RIDES — PHASE 4.4
-- Controlled Booking & Trip Lifecycle RPC Functions
--
-- Required Database Functions:
--   1. public.start_booking(p_booking_id uuid)
--   2. public.complete_booking(p_booking_id uuid)
--   3. public.cancel_booking(p_booking_id uuid)
--
-- Security & Authority Model:
--   - All functions use LANGUAGE plpgsql, SECURITY DEFINER, SET search_path = public
--   - Verify auth.uid() is non-null and authorized
--   - Lock trip and booking rows (FOR UPDATE) to prevent race conditions
--   - Reject invalid state transitions
--   - Preserve no_show and disputed booking states untouched
--   - Revoke EXECUTE from PUBLIC and anon; grant EXECUTE only to authenticated
-- =============================================================================

-- -----------------------------------------------------------------------------
-- 1. public.start_booking(p_booking_id uuid)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.start_booking(p_booking_id uuid)
RETURNS public.bookings
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_caller_id uuid := auth.uid();
    v_booking public.bookings%ROWTYPE;
    v_trip public.trips%ROWTYPE;
BEGIN
    IF v_caller_id IS NULL THEN
        RAISE EXCEPTION 'Unauthorized: authentication required';
    END IF;

    SELECT *
    INTO v_booking
    FROM public.bookings
    WHERE id = p_booking_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Booking not found';
    END IF;

    IF v_booking.driver_id IS DISTINCT FROM v_caller_id THEN
        RAISE EXCEPTION 'Unauthorized: only the trip driver can start this trip';
    END IF;

    SELECT *
    INTO v_trip
    FROM public.trips
    WHERE id = v_booking.trip_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Associated trip not found';
    END IF;

    IF v_trip.driver_id IS DISTINCT FROM v_caller_id THEN
        RAISE EXCEPTION 'Unauthorized: only the trip driver can start this trip';
    END IF;

    -- Idempotency: if the booking is already in_progress and trip is in_progress,
    -- ensure any remaining confirmed bookings on this trip are also moved to in_progress and return.
    IF v_booking.status = 'in_progress' THEN
        IF v_trip.trip_status IN ('published', 'full') THEN
            UPDATE public.trips
            SET trip_status = 'in_progress',
                updated_at = now()
            WHERE id = v_trip.id;
        END IF;

        UPDATE public.bookings
        SET status = 'in_progress',
            updated_at = now()
        WHERE trip_id = v_trip.id
          AND status = 'confirmed';

        SELECT * INTO v_booking FROM public.bookings WHERE id = p_booking_id;
        RETURN v_booking;
    END IF;

    -- Reject invalid booking status transitions (completed, cancelled, no_show, disputed)
    IF v_booking.status <> 'confirmed' THEN
        IF v_booking.status = 'completed' THEN
            RAISE EXCEPTION 'This trip has already been completed';
        ELSE
            RAISE EXCEPTION 'Invalid status transition: cannot start booking with status %', v_booking.status;
        END IF;
    END IF;

    -- Reject if trip is cancelled or completed
    IF v_trip.trip_status = 'cancelled' THEN
        RAISE EXCEPTION 'Cannot start a cancelled trip';
    END IF;

    IF v_trip.trip_status = 'completed' THEN
        RAISE EXCEPTION 'This trip has already been completed';
    END IF;

    -- Move the trip to in_progress if not already in_progress
    IF v_trip.trip_status <> 'in_progress' THEN
        UPDATE public.trips
        SET trip_status = 'in_progress',
            updated_at = now()
        WHERE id = v_trip.id;
    END IF;

    -- Multi-passenger rule: move all eligible confirmed bookings for this trip to in_progress
    -- (Do not touch cancelled, completed, no_show, or disputed bookings; keep completed_at NULL)
    UPDATE public.bookings
    SET status = 'in_progress',
        updated_at = now()
    WHERE trip_id = v_trip.id
      AND status = 'confirmed';

    SELECT *
    INTO v_booking
    FROM public.bookings
    WHERE id = p_booking_id;

    RETURN v_booking;
END;
$$;

REVOKE ALL ON FUNCTION public.start_booking(uuid) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.start_booking(uuid) FROM anon;
GRANT EXECUTE ON FUNCTION public.start_booking(uuid) TO authenticated;


-- -----------------------------------------------------------------------------
-- 2. public.complete_booking(p_booking_id uuid)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.complete_booking(p_booking_id uuid)
RETURNS public.bookings
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_caller_id uuid := auth.uid();
    v_booking public.bookings%ROWTYPE;
    v_trip public.trips%ROWTYPE;
    v_now timestamptz := now();
BEGIN
    IF v_caller_id IS NULL THEN
        RAISE EXCEPTION 'Unauthorized: authentication required';
    END IF;

    SELECT *
    INTO v_booking
    FROM public.bookings
    WHERE id = p_booking_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Booking not found';
    END IF;

    IF v_booking.driver_id IS DISTINCT FROM v_caller_id THEN
        RAISE EXCEPTION 'Unauthorized: only the trip driver can complete this trip';
    END IF;

    SELECT *
    INTO v_trip
    FROM public.trips
    WHERE id = v_booking.trip_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Associated trip not found';
    END IF;

    IF v_trip.driver_id IS DISTINCT FROM v_caller_id THEN
        RAISE EXCEPTION 'Unauthorized: only the trip driver can complete this trip';
    END IF;

    -- Idempotency: if the booking is already completed, return it without duplicating anything
    IF v_booking.status = 'completed' THEN
        RETURN v_booking;
    END IF;

    -- Reject invalid booking status transitions (confirmed, cancelled, no_show, disputed)
    IF v_booking.status <> 'in_progress' THEN
        RAISE EXCEPTION 'Invalid status transition: booking must be in_progress to complete (current: %)', v_booking.status;
    END IF;

    IF v_trip.trip_status <> 'in_progress' THEN
        IF v_trip.trip_status = 'completed' THEN
            RAISE EXCEPTION 'This trip has already been completed';
        ELSE
            RAISE EXCEPTION 'Invalid status transition: trip must be in_progress to complete (current: %)', v_trip.trip_status;
        END IF;
    END IF;

    -- Complete all eligible in_progress bookings belonging to the trip
    -- (Do not modify available_seats, total_seats, contribution_amount, driver_id, passenger_id,
    --  and do not touch cancelled, no_show, or disputed bookings)
    UPDATE public.bookings
    SET status = 'completed',
        completed_at = COALESCE(completed_at, v_now),
        updated_at = v_now
    WHERE trip_id = v_trip.id
      AND status = 'in_progress';

    -- Complete the trip
    UPDATE public.trips
    SET trip_status = 'completed',
        updated_at = v_now
    WHERE id = v_trip.id;

    SELECT *
    INTO v_booking
    FROM public.bookings
    WHERE id = p_booking_id;

    RETURN v_booking;
END;
$$;

REVOKE ALL ON FUNCTION public.complete_booking(uuid) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.complete_booking(uuid) FROM anon;
GRANT EXECUTE ON FUNCTION public.complete_booking(uuid) TO authenticated;


-- -----------------------------------------------------------------------------
-- 3. public.cancel_booking(p_booking_id uuid)
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION public.cancel_booking(p_booking_id uuid)
RETURNS public.bookings
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_caller_id uuid := auth.uid();
    v_booking public.bookings%ROWTYPE;
    v_trip public.trips%ROWTYPE;
    v_new_available_seats integer;
    v_new_trip_status text;
    v_now timestamptz := now();
BEGIN
    IF v_caller_id IS NULL THEN
        RAISE EXCEPTION 'Unauthorized: authentication required';
    END IF;

    SELECT *
    INTO v_booking
    FROM public.bookings
    WHERE id = p_booking_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Booking not found';
    END IF;

    -- Authorization: only the booking's passenger or driver may cancel
    IF v_caller_id IS DISTINCT FROM v_booking.passenger_id
       AND v_caller_id IS DISTINCT FROM v_booking.driver_id THEN
        RAISE EXCEPTION 'Unauthorized: you are not allowed to cancel this booking';
    END IF;

    -- Only confirmed bookings can be cancelled (reject in_progress, completed, cancelled, no_show, disputed)
    IF v_booking.status <> 'confirmed' THEN
        RAISE EXCEPTION 'This booking can no longer be cancelled';
    END IF;

    SELECT *
    INTO v_trip
    FROM public.trips
    WHERE id = v_booking.trip_id
    FOR UPDATE;

    IF NOT FOUND THEN
        RAISE EXCEPTION 'Associated trip not found';
    END IF;

    -- Reject cancellation if the trip has already started, completed, or been cancelled
    IF v_trip.trip_status NOT IN ('published', 'full') THEN
        RAISE EXCEPTION 'This booking can no longer be cancelled';
    END IF;

    -- Mark booking as cancelled
    UPDATE public.bookings
    SET status = 'cancelled',
        cancelled_at = v_now,
        updated_at = v_now
    WHERE id = p_booking_id;

    -- Atomically restore reserved seats to trips.available_seats without exceeding total_seats
    v_new_available_seats := LEAST(v_trip.total_seats, v_trip.available_seats + v_booking.seats_booked);

    -- If the trip was previously 'full' and seats are now available before departure, return to 'published'
    IF v_trip.trip_status = 'full' AND v_new_available_seats > 0 THEN
        v_new_trip_status := 'published';
    ELSE
        v_new_trip_status := v_trip.trip_status;
    END IF;

    UPDATE public.trips
    SET available_seats = v_new_available_seats,
        trip_status = v_new_trip_status,
        updated_at = v_now
    WHERE id = v_trip.id;

    SELECT *
    INTO v_booking
    FROM public.bookings
    WHERE id = p_booking_id;

    RETURN v_booking;
END;
$$;

REVOKE ALL ON FUNCTION public.cancel_booking(uuid) FROM PUBLIC;
REVOKE ALL ON FUNCTION public.cancel_booking(uuid) FROM anon;
GRANT EXECUTE ON FUNCTION public.cancel_booking(uuid) TO authenticated;
