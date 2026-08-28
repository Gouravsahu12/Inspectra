/*
# Security hardening: revoke EXECUTE on internal functions and fix search paths

## Overview
This migration addresses security advisor warnings about SECURITY DEFINER
functions being callable by anon/authenticated roles via the REST API, and
functions with mutable search paths.

## Changes

### 1. Revoke EXECUTE on internal functions
- `handle_new_user()` — should only be called by the auth trigger, never by clients
- `generate_reference_number()` — should only be called as a column DEFAULT, never by clients
- `update_updated_at_column()` — internal trigger function
- `update_inspections_updated_at()` — internal trigger function

All four functions have EXECUTE revoked from anon, authenticated, and public roles.
They remain callable by the trigger mechanism (which runs with sufficient privileges).

### 2. Fix search_path on trigger functions
- `update_updated_at_column()` — added SET search_path = public
- `update_inspections_updated_at()` — added SET search_path = public

## Security Impact
- Clients can no longer call `generate_reference_number()` or `handle_new_user()`
  via the Supabase REST API (`/rest/v1/rpc/...`)
- This prevents reference number pre-generation attacks and user creation abuse
- The functions still work correctly as trigger functions and column defaults
*/

-- ──────────────────────────────────────────────
-- REVOKE EXECUTE ON INTERNAL FUNCTIONS
-- ──────────────────────────────────────────────

REVOKE EXECUTE ON FUNCTION public.handle_new_user() FROM anon, authenticated, public;
REVOKE EXECUTE ON FUNCTION public.generate_reference_number() FROM anon, authenticated, public;
REVOKE EXECUTE ON FUNCTION public.update_updated_at_column() FROM anon, authenticated, public;
REVOKE EXECUTE ON FUNCTION public.update_inspections_updated_at() FROM anon, authenticated, public;

-- ──────────────────────────────────────────────
-- FIX SEARCH_PATH ON TRIGGER FUNCTIONS
-- ──────────────────────────────────────────────

CREATE OR REPLACE FUNCTION public.update_updated_at_column()
RETURNS trigger
LANGUAGE plpgsql
SET search_path = public
AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

CREATE OR REPLACE FUNCTION public.update_inspections_updated_at()
RETURNS trigger
LANGUAGE plpgsql
SET search_path = public
AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

-- Re-apply revokes after function recreation
REVOKE EXECUTE ON FUNCTION public.update_updated_at_column() FROM anon, authenticated, public;
REVOKE EXECUTE ON FUNCTION public.update_inspections_updated_at() FROM anon, authenticated, public;
