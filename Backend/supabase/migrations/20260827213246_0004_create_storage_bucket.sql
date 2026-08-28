/*
# Create inspection-images storage bucket with security policies

## Overview
This migration creates a PRIVATE Supabase Storage bucket named `inspection-images`
and configures Storage RLS policies so that authenticated officers can only
upload, read, and delete images within their own inspection paths.

## Storage Bucket
- Name: `inspection-images`
- Public: NO (private — requires authentication)
- Recommended object structure: `inspection-images/{inspection_id}/{image_id}/original.jpg`

## Storage Policies (RLS on storage.objects)

### SELECT (read/download)
- Authenticated users can read objects in `inspection-images` bucket only if
  the object path starts with an inspection_id that belongs to them

### INSERT (upload)
- Authenticated users can upload to `inspection-images` bucket only if
  the path starts with an inspection_id that belongs to them

### UPDATE (overwrite)
- Authenticated users can update objects in `inspection-images` bucket only if
  the path starts with an inspection_id that belongs to them

### DELETE
- Authenticated users can delete objects in `inspection-images` bucket only if
  the path starts with an inspection_id that belongs to them

## Important Notes
1. The bucket is PRIVATE — no public/anonymous access.
2. Policies check ownership by extracting the inspection_id from the file path
   (first path segment) and verifying it belongs to the authenticated user.
3. This prevents officers from writing to another officer's inspection folder.
4. The storage path convention is: `{inspection_id}/{image_id}/original.jpg`
   where the first segment is the inspection UUID.
*/

-- ──────────────────────────────────────────────
-- CREATE PRIVATE STORAGE BUCKET
-- ──────────────────────────────────────────────

INSERT INTO storage.buckets (id, name, public)
VALUES ('inspection-images', 'inspection-images', false)
ON CONFLICT (id) DO NOTHING;

-- ──────────────────────────────────────────────
-- STORAGE POLICIES
-- ──────────────────────────────────────────────

-- SELECT: officers can read images for their own inspections
DROP POLICY IF EXISTS "select_own_inspection_images_storage" ON storage.objects;
CREATE POLICY "select_own_inspection_images_storage"
ON storage.objects FOR SELECT
TO authenticated
USING (
    bucket_id = 'inspection-images'
    AND EXISTS (
        SELECT 1 FROM inspections
        WHERE inspections.id::text = (storage.foldername(name))[1]
        AND inspections.officer_id = auth.uid()
    )
);

-- INSERT: officers can upload images for their own inspections
DROP POLICY IF EXISTS "insert_own_inspection_images_storage" ON storage.objects;
CREATE POLICY "insert_own_inspection_images_storage"
ON storage.objects FOR INSERT
TO authenticated
WITH CHECK (
    bucket_id = 'inspection-images'
    AND EXISTS (
        SELECT 1 FROM inspections
        WHERE inspections.id::text = (storage.foldername(name))[1]
        AND inspections.officer_id = auth.uid()
    )
);

-- UPDATE: officers can update images for their own inspections
DROP POLICY IF EXISTS "update_own_inspection_images_storage" ON storage.objects;
CREATE POLICY "update_own_inspection_images_storage"
ON storage.objects FOR UPDATE
TO authenticated
USING (
    bucket_id = 'inspection-images'
    AND EXISTS (
        SELECT 1 FROM inspections
        WHERE inspections.id::text = (storage.foldername(name))[1]
        AND inspections.officer_id = auth.uid()
    )
)
WITH CHECK (
    bucket_id = 'inspection-images'
    AND EXISTS (
        SELECT 1 FROM inspections
        WHERE inspections.id::text = (storage.foldername(name))[1]
        AND inspections.officer_id = auth.uid()
    )
);

-- DELETE: officers can delete images for their own inspections
DROP POLICY IF EXISTS "delete_own_inspection_images_storage" ON storage.objects;
CREATE POLICY "delete_own_inspection_images_storage"
ON storage.objects FOR DELETE
TO authenticated
USING (
    bucket_id = 'inspection-images'
    AND EXISTS (
        SELECT 1 FROM inspections
        WHERE inspections.id::text = (storage.foldername(name))[1]
        AND inspections.officer_id = auth.uid()
    )
);
