/*
# Create inspection_images table with RLS

## Overview
This migration creates the `inspection_images` table that tracks metadata for
images uploaded to Supabase Storage. Images themselves are NOT stored as binary
blobs in the database — only metadata (path, filename, side, hash, timestamps).

## New Tables

### inspection_images
- `id` (uuid, primary key, default gen_random_uuid())
- `inspection_id` (uuid, not null) — FK to inspections(id) with ON DELETE CASCADE
- `storage_path` (text, not null) — path in Supabase Storage bucket
- `original_filename` (text, nullable) — original filename from the device
- `side` (text, not null, default 'OTHER') — FRONT, BACK, SIDE, OTHER
- `sha256_hash` (text, nullable) — SHA-256 hash of the image file
- `captured_at` (timestamptz, nullable) — when the photo was taken (device timestamp)
- `created_at` (timestamptz, default now()) — when the record was created

## Indexes
- idx_inspection_images_inspection_id on inspection_id
- idx_inspection_images_sha256_hash on sha256_hash

## Security
- RLS enabled on `inspection_images`
- SELECT: officers can only see images for inspections they own
- INSERT: officers can only add images to inspections they own
- UPDATE: officers can only update images for inspections they own
- DELETE: officers can only delete images for inspections they own
- Ownership is checked via EXISTS subquery against inspections table

## Important Notes
1. Image files are stored in Supabase Storage bucket `inspection-images`, not in
   the database. This table only stores metadata.
2. The sha256_hash field allows future server-side verification — the hash should
   be computed client-side before upload and can be verified server-side later.
3. The side field uses the values FRONT, BACK, SIDE, OTHER as specified in the
   requirements (simplified from the FastAPI model which had more granular sides).
*/

-- ──────────────────────────────────────────────
-- INSPECTION IMAGES TABLE
-- ──────────────────────────────────────────────

CREATE TABLE IF NOT EXISTS inspection_images (
    id                uuid PRIMARY KEY DEFAULT gen_random_uuid(),
    inspection_id     uuid NOT NULL REFERENCES inspections(id) ON DELETE CASCADE,
    storage_path      text NOT NULL,
    original_filename text,
    side              text NOT NULL DEFAULT 'OTHER' CHECK (side IN ('FRONT', 'BACK', 'SIDE', 'OTHER')),
    sha256_hash       text,
    captured_at       timestamptz,
    created_at        timestamptz NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_inspection_images_inspection_id ON inspection_images (inspection_id);
CREATE INDEX IF NOT EXISTS idx_inspection_images_sha256_hash ON inspection_images (sha256_hash);

-- ──────────────────────────────────────────────
-- ROW LEVEL SECURITY
-- ──────────────────────────────────────────────

ALTER TABLE inspection_images ENABLE ROW LEVEL SECURITY;

-- SELECT: officers can only see images for their own inspections
DROP POLICY IF EXISTS "select_own_inspection_images" ON inspection_images;
CREATE POLICY "select_own_inspection_images"
ON inspection_images FOR SELECT
TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM inspections
        WHERE inspections.id = inspection_images.inspection_id
        AND inspections.officer_id = auth.uid()
    )
);

-- INSERT: officers can only add images to their own inspections
DROP POLICY IF EXISTS "insert_own_inspection_images" ON inspection_images;
CREATE POLICY "insert_own_inspection_images"
ON inspection_images FOR INSERT
TO authenticated
WITH CHECK (
    EXISTS (
        SELECT 1 FROM inspections
        WHERE inspections.id = inspection_images.inspection_id
        AND inspections.officer_id = auth.uid()
    )
);

-- UPDATE: officers can only update images for their own inspections
DROP POLICY IF EXISTS "update_own_inspection_images" ON inspection_images;
CREATE POLICY "update_own_inspection_images"
ON inspection_images FOR UPDATE
TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM inspections
        WHERE inspections.id = inspection_images.inspection_id
        AND inspections.officer_id = auth.uid()
    )
)
WITH CHECK (
    EXISTS (
        SELECT 1 FROM inspections
        WHERE inspections.id = inspection_images.inspection_id
        AND inspections.officer_id = auth.uid()
    )
);

-- DELETE: officers can only delete images for their own inspections
DROP POLICY IF EXISTS "delete_own_inspection_images" ON inspection_images;
CREATE POLICY "delete_own_inspection_images"
ON inspection_images FOR DELETE
TO authenticated
USING (
    EXISTS (
        SELECT 1 FROM inspections
        WHERE inspections.id = inspection_images.inspection_id
        AND inspections.officer_id = auth.uid()
    )
);
