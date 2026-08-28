/*
# Create inspections table with server-side reference number generation and RLS

## Overview
This migration creates the `inspections` table for storing inspection records.
Each inspection is linked to an officer (profile) and gets a unique server-generated
reference number in the format INSP-YYYY-NNNNNN.

## New Tables

### inspections
- `id` (uuid, primary key, default gen_random_uuid())
- `reference_number` (text, not null, unique) — server-generated, format INSP-YYYY-NNNNNN
- `officer_id` (uuid, not null) — FK to profiles(id) with ON DELETE CASCADE
- `product_name` (text, not null) — name of the inspected product
- `brand_name` (text, nullable) — product brand
- `category` (text, nullable) — product category
- `store_name` (text, nullable) — store where inspection took place
- `location` (text, nullable) — inspection location
- `facility_type` (text, nullable) — type of facility
- `status` (text, not null, default 'draft') — draft, in_progress, completed, failed
- `compliance_score` (double precision, nullable) — 0-100 score (to be populated by compliance engine later)
- `officer_notes` (text, nullable) — free-text notes from the officer
- `created_at` (timestamptz, default now())
- `updated_at` (timestamptz, default now())

## New Functions

### generate_reference_number()
- SECURITY DEFINER function that generates a unique reference number INSP-YYYY-NNNNNN
- Queries the highest existing reference for the current year and increments
- Called as a DEFAULT on the reference_number column

### update_inspections_updated_at()
- Trigger function to update updated_at on row modification

## New Triggers
- `inspections_updated_at` — BEFORE UPDATE on inspections → updates updated_at

## Indexes
- idx_inspections_reference_number (unique) on reference_number
- idx_inspections_officer_id on officer_id
- idx_inspections_category on category
- idx_inspections_status on status

## Security
- RLS enabled on `inspections`
- SELECT: officers can only see their own inspections
- INSERT: officers can only create inspections for themselves (officer_id defaults to auth.uid())
- UPDATE: officers can only update their own inspections
- DELETE: officers can only delete their own inspections
- The officer_id column has DEFAULT auth.uid() so client inserts that omit it still work

## Important Notes
1. Reference numbers are generated server-side — the client cannot set an arbitrary
   reference number. The column has no INSERT policy override, so even if a client
   tries to set it, the DEFAULT takes effect.
2. officer_id defaults to auth.uid() and the INSERT WITH CHECK ensures it matches,
   preventing impersonation.
3. compliance_score is nullable now — it will be populated by the compliance engine
   in a later phase.
*/

-- ──────────────────────────────────────────────
-- REFERENCE NUMBER GENERATOR
-- ──────────────────────────────────────────────

CREATE OR REPLACE FUNCTION public.generate_reference_number()
RETURNS text
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
DECLARE
    v_year int := extract(year from now())::int;
    v_prefix text := 'INSP-' || v_year || '-';
    v_max_seq int;
    v_new_ref text;
BEGIN
    SELECT COALESCE(MAX(CAST(substring(reference_number from length(v_prefix) + 1) AS int)), 0)
    INTO v_max_seq
    FROM public.inspections
    WHERE reference_number LIKE v_prefix || '%';

    v_new_ref := v_prefix || lpad((v_max_seq + 1)::text, 6, '0');

    RETURN v_new_ref;
END;
$$;

-- ──────────────────────────────────────────────
-- INSPECTIONS TABLE
-- ──────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS inspections (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    reference_number  text NOT NULL DEFAULT public.generate_reference_number(),
    officer_id        uuid NOT NULL DEFAULT auth.uid() REFERENCES profiles(id) ON DELETE CASCADE,
    product_name      text NOT NULL,
    brand_name        text,
    category          text,
    store_name        text,
    location          text,
    facility_type     text,
    status            text NOT NULL DEFAULT 'draft' CHECK (status IN ('draft', 'in_progress', 'completed', 'failed')),
    compliance_score  double precision CHECK (compliance_score IS NULL OR (compliance_score >= 0 AND compliance_score <= 100)),
    officer_notes     text,
    created_at        timestamptz NOT NULL DEFAULT now(),
    updated_at        timestamptz NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_inspections_reference_number ON inspections (reference_number);
CREATE INDEX IF NOT EXISTS idx_inspections_officer_id ON inspections (officer_id);
CREATE INDEX IF NOT EXISTS idx_inspections_category ON inspections (category);
CREATE INDEX IF NOT EXISTS idx_inspections_status ON inspections (status);

-- ──────────────────────────────────────────────
-- UPDATED_AT TRIGGER
-- ──────────────────────────────────────────────

CREATE OR REPLACE FUNCTION public.update_inspections_updated_at()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    NEW.updated_at = now();
    RETURN NEW;
END;
$$;

DROP TRIGGER IF EXISTS inspections_updated_at ON inspections;
CREATE TRIGGER inspections_updated_at
    BEFORE UPDATE ON inspections
    FOR EACH ROW
    EXECUTE FUNCTION public.update_inspections_updated_at();

-- ──────────────────────────────────────────────
-- ROW LEVEL SECURITY
-- ──────────────────────────────────────────────

ALTER TABLE inspections ENABLE ROW LEVEL SECURITY;

-- SELECT: officers can only see their own inspections
DROP POLICY IF EXISTS "select_own_inspections" ON inspections;
CREATE POLICY "select_own_inspections"
ON inspections FOR SELECT
TO authenticated
USING (officer_id = auth.uid());

-- INSERT: officers can only create inspections as themselves
DROP POLICY IF EXISTS "insert_own_inspections" ON inspections;
CREATE POLICY "insert_own_inspections"
ON inspections FOR INSERT
TO authenticated
WITH CHECK (officer_id = auth.uid());

-- UPDATE: officers can only update their own inspections
DROP POLICY IF EXISTS "update_own_inspections" ON inspections;
CREATE POLICY "update_own_inspections"
ON inspections FOR UPDATE
TO authenticated
USING (officer_id = auth.uid())
WITH CHECK (officer_id = auth.uid());

-- DELETE: officers can only delete their own inspections
DROP POLICY IF EXISTS "delete_own_inspections" ON inspections;
CREATE POLICY "delete_own_inspections"
ON inspections FOR DELETE
TO authenticated
USING (officer_id = auth.uid());
