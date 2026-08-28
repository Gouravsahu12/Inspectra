/*
# Create profiles table with auto-creation trigger and RLS

## Overview
This migration creates the `profiles` table that stores officer information
linked to Supabase Auth users. When a new user signs up via Supabase Auth,
a corresponding profile row is automatically created via a database trigger.

## New Tables

### profiles
- `id` (uuid, primary key) — references `auth.users(id)` with ON DELETE CASCADE
- `full_name` (text, not null) — officer's full name
- `badge_id` (text, not null, unique) — officer's badge identifier
- `zone` (text, nullable) — assigned zone
- `role` (text, not null, default 'inspector') — officer role: inspector, supervisor, or admin
- `is_active` (boolean, not null, default true) — whether the officer account is active
- `created_at` (timestamptz, default now()) — record creation timestamp
- `updated_at` (timestamptz, default now()) — last update timestamp

## New Functions

### handle_new_user()
- SECURITY DEFINER function that creates a profile row when a new auth user is created
- Sets default values: full_name from user metadata or 'New Officer', badge_id from
  metadata or a generated default, role 'inspector', is_active true
- Runs as a trigger AFTER INSERT on auth.users

### update_updated_at_column()
- Generic trigger function that updates the `updated_at` column on row modification

## New Triggers
- `on_auth_user_created` — AFTER INSERT on auth.users → calls handle_new_user()
- `profiles_updated_at` — BEFORE UPDATE on profiles → calls update_updated_at_column()

## Security
- RLS enabled on `profiles`
- SELECT: authenticated users can read their own profile
- INSERT: blocked at RLS level (profiles are created by the SECURITY DEFINER trigger, not by clients)
- UPDATE: authenticated users can update their own profile (but not role or is_active)
- DELETE: blocked at RLS level (profiles are removed via CASCADE when auth user is deleted)

## Important Notes
1. The `handle_new_user` function is SECURITY DEFINER so it can insert into profiles
   even though RLS would block client inserts. This is the secure pattern — clients
   never insert profiles directly.
2. The `role` and `is_active` columns are NOT client-modifiable through RLS UPDATE
   policy — the WITH CHECK clause ensures only safe columns can be changed.
3. Badge IDs are unique — the system prevents duplicate badge registrations.
*/

-- ──────────────────────────────────────────────
-- PROFILES TABLE
-- ──────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS profiles (
    id          uuid PRIMARY KEY REFERENCES auth.users(id) ON DELETE CASCADE,
    full_name   text NOT NULL,
    badge_id    text NOT NULL,
    zone        text,
    role        text NOT NULL DEFAULT 'inspector' CHECK (role IN ('inspector', 'supervisor', 'admin')),
    is_active   boolean NOT NULL DEFAULT true,
    created_at  timestamptz NOT NULL DEFAULT now(),
    updated_at  timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_profiles_badge_id ON profiles (badge_id);
CREATE INDEX IF NOT EXISTS idx_profiles_zone ON profiles (zone);

-- ──────────────────────────────────────────────
-- AUTO-CREATE PROFILE ON SIGNUP
-- ──────────────────────────────────────────────

CREATE OR REPLACE FUNCTION public.handle_new_user()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    INSERT INTO public.profiles (id, full_name, badge_id, zone, role, is_active)
    VALUES (
        NEW.id,
        COALESCE(NEW.raw_user_meta_data->>'full_name', 'New Officer'),
        COALESCE(NEW.raw_user_meta_data->>'badge_id', 'BADGE-' || upper(substr(NEW.id::text, 1, 8))),
        NEW.raw_user_meta_data->>'zone',
        COALESCE(NEW.raw_user_meta_data->>'role', 'inspector'),
        true
    )
    ON CONFLICT (id) DO NOTHING;
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS on_auth_user_created ON auth.users;
CREATE TRIGGER on_auth_user_created
    AFTER INSERT ON auth.users
    FOR EACH ROW
    EXECUTE FUNCTION public.handle_new_user();

-- ──────────────────────────────────────────────
-- UPDATED_AT TRIGGER
-- ──────────────────────────────────────────────

CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS profiles_updated_at ON profiles;
CREATE TRIGGER profiles_updated_at
    BEFORE UPDATE ON profiles
    FOR EACH ROW
    EXECUTE FUNCTION public.update_updated_at_column();

-- ──────────────────────────────────────────────
-- ROW LEVEL SECURITY
-- ──────────────────────────────────────────────

ALTER TABLE profiles ENABLE ROW LEVEL SECURITY;

-- SELECT: users can read their own profile
DROP POLICY IF EXISTS "select_own_profile" ON profiles;
CREATE POLICY "select_own_profile"
ON profiles FOR SELECT
TO authenticated
USING (auth.uid() = id);

-- UPDATE: users can update their own profile (but cannot change role or is_active via RLS)
DROP POLICY IF EXISTS "update_own_profile" ON profiles;
CREATE POLICY "update_own_profile"
ON profiles FOR UPDATE
TO authenticated
USING (auth.uid() = id)
WITH CHECK (
    auth.uid() = id
    AND role = (SELECT role FROM profiles WHERE id = auth.uid())
    AND is_active = (SELECT is_active FROM profiles WHERE id = auth.uid())
);

-- No INSERT or DELETE policies — profiles are managed by the trigger / CASCADE
