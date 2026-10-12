-- =============================================================================
-- WAPI RIDES — PHASE 4.7A
-- NOTIFICATIONS SYSTEM (POSTGRESQL TRIGGERS + UPDATED SECURITY DEFINER RPCS)
-- =============================================================================
-- Run this complete script in the Supabase SQL Editor.
--
-- Security & Architecture Guarantees:
-- 1. All notification inserts occur inside PostgreSQL using `security definer`
--    with `set search_path = public`.
-- 2. No normal-user INSERT policy is added to `public.notifications`.
--    Authenticated users can only SELECT and UPDATE their own notifications.
-- 3. `notifications.data` stores only safe navigation IDs (`trip_id`,
--    `trip_request_id`, `booking_id`) — never phone numbers, emails, or tokens.
-- 4. Phase 4.4 booking lifecycle RPCs (`confirm_trip_request`, `start_booking`,
--    `complete_booking`, `cancel_booking`) and `trip_requests` triggers
--    automatically generate all 8 required notification types without duplicate
--    inserts on idempotent retries.
-- =============================================================================

begin;

-- -----------------------------------------------------------------------------
-- 0. Ensure RLS on public.notifications allows SELECT & UPDATE of own rows only
--    (No normal-user INSERT policy)
-- -----------------------------------------------------------------------------
alter table public.notifications enable row level security;

drop policy if exists "notifications_select_own" on public.notifications;
create policy "notifications_select_own"
on public.notifications
for select
to authenticated
using (user_id = auth.uid());

drop policy if exists "notifications_update_own" on public.notifications;
create policy "notifications_update_own"
on public.notifications
for update
to authenticated
using (user_id = auth.uid())
with check (user_id = auth.uid());

-- -----------------------------------------------------------------------------
-- 1. Helper Trigger Function: Seat Request Created -> Notify Driver
--    Type: 'trip_request_received'
-- -----------------------------------------------------------------------------
create or replace function public.handle_trip_request_created_notification()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_trip public.trips%rowtype;
    v_seat_word text;
begin
    if new.status <> 'pending' then
        return new;
    end if;

    select *
    into v_trip
    from public.trips
    where id = new.trip_id;

    if not found or v_trip.driver_id is null or v_trip.driver_id = new.passenger_id then
        return new;
    end if;

    v_seat_word := case when coalesce(new.requested_seats, 1) = 1 then 'seat' else 'seats' end;

    insert into public.notifications (
        user_id,
        type,
        title,
        body,
        data,
        is_read
    )
    values (
        v_trip.driver_id,
        'trip_request_received',
        'New seat request',
        format(
            'A passenger requested %s %s on your %s → %s ride.',
            coalesce(new.requested_seats, 1),
            v_seat_word,
            v_trip.origin,
            v_trip.destination
        ),
        jsonb_build_object(
            'trip_id', new.trip_id,
            'trip_request_id', new.id
        ),
        false
    );

    return new;
end;
$$;

drop trigger if exists trg_trip_request_created_notification on public.trip_requests;
create trigger trg_trip_request_created_notification
after insert on public.trip_requests
for each row
execute function public.handle_trip_request_created_notification();

-- -----------------------------------------------------------------------------
-- 2. Helper Trigger Function: Seat Request Status Updated -> Notify Counterparty
--    - pending -> declined: Notify Passenger ('trip_request_declined')
--    - pending -> cancelled: Notify Driver ('trip_request_cancelled')
--    (Note: pending -> accepted is handled inside public.confirm_trip_request
--     so that booking_id is included in notifications.data)
-- -----------------------------------------------------------------------------
create or replace function public.handle_trip_request_status_notification()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
declare
    v_trip public.trips%rowtype;
begin
    if old.status is not distinct from new.status then
        return new;
    end if;

    select *
    into v_trip
    from public.trips
    where id = new.trip_id;

    if not found then
        return new;
    end if;

    -- Driver declined a pending request -> notify passenger
    if old.status = 'pending' and new.status = 'declined' then
        insert into public.notifications (
            user_id,
            type,
            title,
            body,
            data,
            is_read
        )
        values (
            new.passenger_id,
            'trip_request_declined',
            'Seat request declined',
            format(
                'Your seat request for %s → %s was declined.',
                v_trip.origin,
                v_trip.destination
            ),
            jsonb_build_object(
                'trip_id', new.trip_id,
                'trip_request_id', new.id
            ),
            false
        );
    -- Passenger cancelled their pending request -> notify driver
    elsif old.status = 'pending' and new.status = 'cancelled' then
        insert into public.notifications (
            user_id,
            type,
            title,
            body,
            data,
            is_read
        )
        values (
            v_trip.driver_id,
            'trip_request_cancelled',
            'Seat request cancelled',
            format(
                'A passenger cancelled their seat request for %s → %s.',
                v_trip.origin,
                v_trip.destination
            ),
            jsonb_build_object(
                'trip_id', new.trip_id,
                'trip_request_id', new.id
            ),
            false
        );
    end if;

    return new;
end;
$$;

drop trigger if exists trg_trip_request_status_notification on public.trip_requests;
create trigger trg_trip_request_status_notification
after update of status on public.trip_requests
for each row
execute function public.handle_trip_request_status_notification();

-- -----------------------------------------------------------------------------
-- 3. public.confirm_trip_request(p_trip_request_id uuid)
--    Confirms a pending seat request, creates booking, decrements seats, and
--    notifies the passenger ('trip_request_accepted') with trip_id,
--    trip_request_id, and booking_id.
-- -----------------------------------------------------------------------------
create or replace function public.confirm_trip_request(p_trip_request_id uuid)
returns public.bookings
language plpgsql
security definer
set search_path = public
as $$
declare
    v_caller_id uuid := auth.uid();
    v_request public.trip_requests%rowtype;
    v_trip public.trips%rowtype;
    v_existing_booking public.bookings%rowtype;
    v_booking public.bookings%rowtype;
    v_remaining_seats integer;
    v_next_trip_status text;
    v_booking_ref text;
begin
    if v_caller_id is null then
        raise exception 'Authentication required';
    end if;

    -- Lock the trip request row
    select *
    into v_request
    from public.trip_requests
    where id = p_trip_request_id
    for update;

    if not found then
        raise exception 'Trip request not found';
    end if;

    -- Lock the parent trip row
    select *
    into v_trip
    from public.trips
    where id = v_request.trip_id
    for update;

    if not found then
        raise exception 'Associated trip not found';
    end if;

    if v_trip.driver_id <> v_caller_id then
        raise exception 'Unauthorized: only the trip driver can confirm this request';
    end if;

    if v_request.passenger_id = v_trip.driver_id then
        raise exception 'Driver cannot book their own trip';
    end if;

    -- Idempotency: if already accepted and booking exists, return existing booking without re-notifying
    if v_request.status = 'accepted' then
        select *
        into v_existing_booking
        from public.bookings
        where trip_request_id = v_request.id
        limit 1;

        if found then
            return v_existing_booking;
        end if;
    end if;

    if v_request.status <> 'pending' then
        raise exception 'Trip request is no longer pending (current status: %)', v_request.status;
    end if;

    if v_trip.trip_status <> 'published' then
        raise exception 'Trip is not open for booking (current status: %)', v_trip.trip_status;
    end if;

    if v_trip.available_seats < v_request.requested_seats then
        raise exception 'Not enough seats available';
    end if;

    v_remaining_seats := v_trip.available_seats - v_request.requested_seats;
    v_next_trip_status := case when v_remaining_seats = 0 then 'full' else v_trip.trip_status end;
    v_booking_ref := 'WAPI-' || upper(substr(replace(gen_random_uuid()::text, '-', ''), 1, 8));

    update public.trip_requests
    set status = 'accepted',
        updated_at = now()
    where id = v_request.id;

    insert into public.bookings (
        trip_id,
        passenger_id,
        driver_id,
        trip_request_id,
        seats_booked,
        pickup_stop_id,
        dropoff_stop_id,
        status,
        booking_reference,
        confirmed_at,
        created_at,
        updated_at
    )
    values (
        v_trip.id,
        v_request.passenger_id,
        v_trip.driver_id,
        v_request.id,
        v_request.requested_seats,
        v_request.pickup_stop_id,
        v_request.dropoff_stop_id,
        'confirmed',
        v_booking_ref,
        now(),
        now(),
        now()
    )
    returning * into v_booking;

    update public.trips
    set available_seats = v_remaining_seats,
        trip_status = v_next_trip_status,
        updated_at = now()
    where id = v_trip.id;

    -- Notify passenger that their seat request was accepted
    insert into public.notifications (
        user_id,
        type,
        title,
        body,
        data,
        is_read
    )
    values (
        v_request.passenger_id,
        'trip_request_accepted',
        'Seat request accepted',
        format(
            'Your seat on %s → %s is confirmed.',
            v_trip.origin,
            v_trip.destination
        ),
        jsonb_build_object(
            'trip_id', v_trip.id,
            'trip_request_id', v_request.id,
            'booking_id', v_booking.id
        ),
        false
    );

    return v_booking;
end;
$$;

-- -----------------------------------------------------------------------------
-- 4. public.start_booking(p_booking_id uuid)
--    Transitions confirmed bookings on the trip to 'in_progress' and notifies
--    each affected passenger ('trip_started') once.
-- -----------------------------------------------------------------------------
create or replace function public.start_booking(p_booking_id uuid)
returns public.bookings
language plpgsql
security definer
set search_path = public
as $$
declare
    v_caller_id uuid := auth.uid();
    v_booking public.bookings%rowtype;
    v_trip public.trips%rowtype;
    v_passenger_booking record;
begin
    if v_caller_id is null then
        raise exception 'Authentication required';
    end if;

    -- Lock the target booking row
    select *
    into v_booking
    from public.bookings
    where id = p_booking_id
    for update;

    if not found then
        raise exception 'Booking not found';
    end if;

    if v_booking.driver_id <> v_caller_id then
        raise exception 'Unauthorized: only the trip driver can start this trip';
    end if;

    -- Lock the parent trip row
    select *
    into v_trip
    from public.trips
    where id = v_booking.trip_id
    for update;

    if not found then
        raise exception 'Associated trip not found';
    end if;

    if v_trip.driver_id <> v_caller_id then
        raise exception 'Unauthorized: only the trip driver can start this trip';
    end if;

    -- Idempotency: if already in_progress, return existing state without duplicate notifications
    if v_booking.status = 'in_progress' then
        if v_trip.trip_status in ('published', 'full') then
            update public.trips
            set trip_status = 'in_progress',
                updated_at = now()
            where id = v_trip.id;
        end if;

        return v_booking;
    end if;

    if v_booking.status <> 'confirmed' then
        raise exception 'Booking cannot be started from status: %', v_booking.status;
    end if;

    if v_trip.trip_status not in ('published', 'full', 'in_progress') then
        raise exception 'Trip cannot be started from status: %', v_trip.trip_status;
    end if;

    -- Transition all confirmed bookings on this trip to in_progress and notify each passenger
    for v_passenger_booking in
        update public.bookings
        set status = 'in_progress',
            updated_at = now()
        where trip_id = v_trip.id
          and status = 'confirmed'
        returning id, passenger_id, trip_request_id
    loop
        insert into public.notifications (
            user_id,
            type,
            title,
            body,
            data,
            is_read
        )
        values (
            v_passenger_booking.passenger_id,
            'trip_started',
            'Trip started',
            format(
                'Your ride from %s to %s is now in progress.',
                v_trip.origin,
                v_trip.destination
            ),
            jsonb_strip_nulls(
                jsonb_build_object(
                    'trip_id', v_trip.id,
                    'booking_id', v_passenger_booking.id,
                    'trip_request_id', v_passenger_booking.trip_request_id
                )
            ),
            false
        );
    end loop;

    -- Transition the trip to in_progress
    update public.trips
    set trip_status = 'in_progress',
        updated_at = now()
    where id = v_trip.id;

    -- Re-read the updated target booking row
    select *
    into v_booking
    from public.bookings
    where id = p_booking_id;

    return v_booking;
end;
$$;

-- -----------------------------------------------------------------------------
-- 5. public.complete_booking(p_booking_id uuid)
--    Transitions in_progress bookings on the trip to 'completed' and notifies:
--    - Each completed passenger: 'trip_completed' and 'rating_available'
--    - The driver: 'rating_available' for each completed booking
-- -----------------------------------------------------------------------------
create or replace function public.complete_booking(p_booking_id uuid)
returns public.bookings
language plpgsql
security definer
set search_path = public
as $$
declare
    v_caller_id uuid := auth.uid();
    v_booking public.bookings%rowtype;
    v_trip public.trips%rowtype;
    v_completed_booking record;
begin
    if v_caller_id is null then
        raise exception 'Authentication required';
    end if;

    -- Lock the target booking row
    select *
    into v_booking
    from public.bookings
    where id = p_booking_id
    for update;

    if not found then
        raise exception 'Booking not found';
    end if;

    if v_booking.driver_id <> v_caller_id then
        raise exception 'Unauthorized: only the trip driver can complete this trip';
    end if;

    -- Lock the parent trip row
    select *
    into v_trip
    from public.trips
    where id = v_booking.trip_id
    for update;

    if not found then
        raise exception 'Associated trip not found';
    end if;

    if v_trip.driver_id <> v_caller_id then
        raise exception 'Unauthorized: only the trip driver can complete this trip';
    end if;

    -- Idempotency: if already completed, return existing state without duplicate notifications
    if v_booking.status = 'completed' then
        if v_trip.trip_status <> 'completed'
           and not exists (
               select 1
               from public.bookings
               where trip_id = v_trip.id
                 and status in ('confirmed', 'in_progress')
           )
        then
            update public.trips
            set trip_status = 'completed',
                updated_at = now()
            where id = v_trip.id;
        end if;

        return v_booking;
    end if;

    if v_booking.status <> 'in_progress' then
        raise exception 'Booking must be in_progress before it can be completed (current status: %)', v_booking.status;
    end if;

    if v_trip.trip_status <> 'in_progress' then
        raise exception 'Trip must be in_progress before it can be completed (current status: %)', v_trip.trip_status;
    end if;

    -- Complete all in_progress bookings on this trip and emit trip_completed + rating_available
    for v_completed_booking in
        update public.bookings
        set status = 'completed',
            completed_at = coalesce(completed_at, now()),
            updated_at = now()
        where trip_id = v_trip.id
          and status = 'in_progress'
        returning id, passenger_id, trip_request_id
    loop
        -- 1. Notify passenger: trip_completed
        insert into public.notifications (
            user_id,
            type,
            title,
            body,
            data,
            is_read
        )
        values (
            v_completed_booking.passenger_id,
            'trip_completed',
            'Trip completed',
            format(
                'Your trip from %s to %s is complete. You can now leave a rating.',
                v_trip.origin,
                v_trip.destination
            ),
            jsonb_strip_nulls(
                jsonb_build_object(
                    'trip_id', v_trip.id,
                    'booking_id', v_completed_booking.id,
                    'trip_request_id', v_completed_booking.trip_request_id
                )
            ),
            false
        );

        -- 2. Notify passenger: rating_available
        insert into public.notifications (
            user_id,
            type,
            title,
            body,
            data,
            is_read
        )
        values (
            v_completed_booking.passenger_id,
            'rating_available',
            'Rate your trip',
            format(
                'How was your %s → %s trip? Tap to leave a rating.',
                v_trip.origin,
                v_trip.destination
            ),
            jsonb_strip_nulls(
                jsonb_build_object(
                    'trip_id', v_trip.id,
                    'booking_id', v_completed_booking.id,
                    'trip_request_id', v_completed_booking.trip_request_id
                )
            ),
            false
        );

        -- 3. Notify driver: rating_available
        insert into public.notifications (
            user_id,
            type,
            title,
            body,
            data,
            is_read
        )
        values (
            v_trip.driver_id,
            'rating_available',
            'Rate your passenger',
            format(
                'Your %s → %s trip is complete. Tap to rate your passenger.',
                v_trip.origin,
                v_trip.destination
            ),
            jsonb_strip_nulls(
                jsonb_build_object(
                    'trip_id', v_trip.id,
                    'booking_id', v_completed_booking.id,
                    'trip_request_id', v_completed_booking.trip_request_id
                )
            ),
            false
        );
    end loop;

    -- If no active (confirmed or in_progress) bookings remain for this trip, mark the trip completed
    if not exists (
        select 1
        from public.bookings
        where trip_id = v_trip.id
          and status in ('confirmed', 'in_progress')
    ) then
        update public.trips
        set trip_status = 'completed',
            updated_at = now()
        where id = v_trip.id;
    end if;

    -- Re-read the updated target booking row
    select *
    into v_booking
    from public.bookings
    where id = p_booking_id;

    return v_booking;
end;
$$;

-- -----------------------------------------------------------------------------
-- 6. public.cancel_booking(p_booking_id uuid)
--    Cancels a confirmed booking, restores seats, and notifies the other party
--    ('booking_cancelled'):
--    - If passenger cancelled -> notify driver
--    - If driver cancelled -> notify passenger
-- -----------------------------------------------------------------------------
create or replace function public.cancel_booking(p_booking_id uuid)
returns public.bookings
language plpgsql
security definer
set search_path = public
as $$
declare
    v_caller_id uuid := auth.uid();
    v_booking public.bookings%rowtype;
    v_trip public.trips%rowtype;
    v_restored_seats integer;
    v_next_trip_status text;
    v_recipient_id uuid;
    v_body text;
begin
    if v_caller_id is null then
        raise exception 'Authentication required';
    end if;

    -- Lock the target booking row
    select *
    into v_booking
    from public.bookings
    where id = p_booking_id
    for update;

    if not found then
        raise exception 'Booking not found';
    end if;

    if v_booking.passenger_id <> v_caller_id and v_booking.driver_id <> v_caller_id then
        raise exception 'Unauthorized: only the booking passenger or trip driver can cancel this booking';
    end if;

    -- Lock the parent trip row
    select *
    into v_trip
    from public.trips
    where id = v_booking.trip_id
    for update;

    if not found then
        raise exception 'Associated trip not found';
    end if;

    -- Idempotency: if already cancelled, return existing state without re-restoring seats or re-notifying
    if v_booking.status = 'cancelled' then
        return v_booking;
    end if;

    if v_booking.status <> 'confirmed' then
        raise exception 'Only confirmed bookings can be cancelled before the trip starts (current status: %)', v_booking.status;
    end if;

    if v_trip.trip_status in ('in_progress', 'completed', 'cancelled') then
        raise exception 'Booking cannot be cancelled because the trip is already %', v_trip.trip_status;
    end if;

    -- 1. Cancel the booking
    update public.bookings
    set status = 'cancelled',
        cancelled_at = coalesce(cancelled_at, now()),
        updated_at = now()
    where id = v_booking.id
    returning * into v_booking;

    -- 2. Restore available seats (never exceeding total_seats) and reopen full trips to published
    v_restored_seats := least(v_trip.total_seats, v_trip.available_seats + v_booking.seats_booked);
    v_next_trip_status := case
        when v_trip.trip_status = 'full' and v_restored_seats > 0 then 'published'
        else v_trip.trip_status
    end;

    update public.trips
    set available_seats = v_restored_seats,
        trip_status = v_next_trip_status,
        updated_at = now()
    where id = v_trip.id;

    -- 3. Notify the counterparty
    if v_caller_id = v_booking.passenger_id then
        v_recipient_id := v_booking.driver_id;
        v_body := format(
            'A passenger cancelled their confirmed booking for %s → %s.',
            v_trip.origin,
            v_trip.destination
        );
    else
        v_recipient_id := v_booking.passenger_id;
        v_body := format(
            'Your confirmed booking for %s → %s was cancelled by the driver.',
            v_trip.origin,
            v_trip.destination
        );
    end if;

    insert into public.notifications (
        user_id,
        type,
        title,
        body,
        data,
        is_read
    )
    values (
        v_recipient_id,
        'booking_cancelled',
        'Booking cancelled',
        v_body,
        jsonb_strip_nulls(
            jsonb_build_object(
                'trip_id', v_trip.id,
                'booking_id', v_booking.id,
                'trip_request_id', v_booking.trip_request_id
            )
        ),
        false
    );

    return v_booking;
end;
$$;

-- Ensure execute permissions for authenticated users
grant execute on function public.confirm_trip_request(uuid) to authenticated;
grant execute on function public.start_booking(uuid) to authenticated;
grant execute on function public.complete_booking(uuid) to authenticated;
grant execute on function public.cancel_booking(uuid) to authenticated;

commit;
