# INSPECTRA — Supabase Backend

## Architecture

```
ANDROID APP
     |
     | Supabase Kotlin SDK / HTTPS
     |
     +-------------------------+
     |                         |
     v                         v
SUPABASE AUTH             SUPABASE STORAGE
     |
     v
SUPABASE POSTGRESQL
     |
     v
ROW LEVEL SECURITY
     |
     v
SUPABASE EDGE FUNCTIONS
     |
     +--------------------+
     |                    |
     v                    v
   GEMINI            COMPLIANCE ENGINE
 (future)                |
                         v
                    LMPC RULES (future)
```

## Supabase Services In Use

1. **Auth** — email/password authentication, session management, JWT tokens
2. **PostgreSQL** — profiles, inspections, inspection_images tables
3. **Storage** — private `inspection-images` bucket for image files
4. **Row Level Security** — all tables locked to owner-scoped access
5. **Edge Functions** — secure skeleton functions for future AI/OCR/compliance/reporting

## Database Schema

### profiles
Linked to `auth.users(id)` via FK with CASCADE delete. Auto-created on signup via trigger.

| Column | Type | Notes |
|--------|------|-------|
| id | UUID | PK, FK → auth.users(id) |
| full_name | text | NOT NULL |
| badge_id | text | NOT NULL, UNIQUE |
| zone | text | nullable |
| role | text | NOT NULL, default 'inspector' (inspector/supervisor/admin) |
| is_active | boolean | NOT NULL, default true |
| created_at | timestamptz | default now() |
| updated_at | timestamptz | auto-updated |

### inspections
| Column | Type | Notes |
|--------|------|-------|
| id | UUID | PK, default gen_random_uuid() |
| reference_number | text | NOT NULL, UNIQUE, server-generated (INSP-YYYY-NNNNNN) |
| officer_id | UUID | NOT NULL, FK → profiles(id), default auth.uid() |
| product_name | text | NOT NULL |
| brand_name | text | nullable |
| category | text | nullable |
| store_name | text | nullable |
| location | text | nullable |
| facility_type | text | nullable |
| status | text | NOT NULL, default 'draft' (draft/in_progress/completed/failed) |
| compliance_score | float | nullable, 0-100 |
| officer_notes | text | nullable |
| created_at | timestamptz | default now() |
| updated_at | timestamptz | auto-updated |

### inspection_images
| Column | Type | Notes |
|--------|------|-------|
| id | UUID | PK, default gen_random_uuid() |
| inspection_id | UUID | NOT NULL, FK → inspections(id), CASCADE |
| storage_path | text | NOT NULL (path in Supabase Storage) |
| original_filename | text | nullable |
| side | text | NOT NULL, default 'OTHER' (FRONT/BACK/SIDE/OTHER) |
| sha256_hash | text | nullable |
| captured_at | timestamptz | nullable |
| created_at | timestamptz | default now() |

## RLS Policies

### profiles
- SELECT: user can read own profile only
- UPDATE: user can update own profile (cannot change role or is_active)
- No INSERT/DELETE — managed by trigger/CASCADE

### inspections
- SELECT/INSERT/UPDATE/DELETE: officer_id = auth.uid() only

### inspection_images
- SELECT/INSERT/UPDATE/DELETE: only if parent inspection belongs to auth.uid()

### Storage (inspection-images bucket)
- SELECT/INSERT/UPDATE/DELETE: only if path starts with inspection_id owned by auth.uid()

## Edge Functions

| Function | Purpose | Status |
|----------|---------|--------|
| analyze-inspection | Gemini AI/OCR image analysis | Skeleton (auth verified) |
| evaluate-compliance | LMPC rule compliance evaluation | Skeleton (auth verified) |
| generate-report | PDF inspection report generation | Skeleton (auth verified) |

All edge functions verify JWT authentication before performing any operations.
The GEMINI_API_KEY will be stored as a Supabase secret when AI is implemented.

## API Contract Mapping (FastAPI → Supabase)

| Old FastAPI Endpoint | New Supabase Operation |
|---|---|
| POST /api/v1/auth/login | `supabase.auth.signInWithPassword({ email, password })` |
| GET /api/v1/officers/me | `supabase.from('profiles').select('*').eq('id', user.id).maybeSingle()` |
| POST /api/v1/inspections | `supabase.from('inspections').insert({ product_name, ... })` |
| GET /api/v1/inspections | `supabase.from('inspections').select('*').order('created_at', { ascending: false })` |
| GET /api/v1/inspections/{id} | `supabase.from('inspections').select('*').eq('id', id).maybeSingle()` |
| Image upload | `supabase.storage.from('inspection-images').upload(path, file)` |

## Migrations Applied

1. `0001_create_profiles_table` — profiles table, auto-creation trigger, RLS
2. `0002_create_inspections_table` — inspections table, reference number generator, RLS
3. `0003_create_inspection_images_table` — inspection_images table, RLS
4. `0004_create_storage_bucket` — private storage bucket, storage RLS policies
5. `0005_security_hardening` — revoke EXECUTE on internal functions, fix search paths

## Environment Variables

Pre-populated in `.env` (do not configure manually):
- `VITE_SUPABASE_URL` — Supabase project URL
- `VITE_SUPABASE_ANON_KEY` — Public/publishable key (safe for Android client)

Server-side (managed by Supabase, not exposed to Android):
- `SUPABASE_SERVICE_ROLE_KEY` — service role key (edge functions only)
- `GEMINI_API_KEY` — to be added as Supabase secret when AI is implemented

## Security Posture

- Security advisor: 0 warnings
- All tables: RLS enabled
- All policies: use auth.uid() for ownership
- No public/anonymous access to any table
- Storage bucket: private
- Internal functions: EXECUTE revoked from anon/authenticated/public
