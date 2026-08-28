/*
# Fix: Use BEFORE INSERT trigger for reference number generation

## Problem
The previous migration (0005_security_hardening) revoked EXECUTE on
generate_reference_number() from all client roles to prevent REST API abuse.
However, the reference_number column had DEFAULT generate_reference_number(),
which requires the inserting role to have EXECUTE permission on the function.
This caused "permission denied for function generate_reference_number" when
authenticated users tried to insert inspections.

## Fix
1. Remove the DEFAULT from the reference_number column
2. Create a BEFORE INSERT trigger that calls generate_reference_number()
   only when reference_number is NULL
3. The trigger function is SECURITY DEFINER, so it runs with owner privileges
   regardless of the inserting role's permissions
4. EXECUTE remains revoked on generate_reference_number() — clients cannot
   call it directly via REST API, but the trigger fires automatically

## Security Impact
- Clients still cannot call generate_reference_number() via /rest/v1/rpc/
- The trigger fires server-side during INSERT, generating the reference number
- No change to RLS policies — ownership checks remain intact
*/

-- Remove the DEFAULT that required client-side EXECUTE permission
ALTER TABLE inspections ALTER COLUMN reference_number DROP DEFAULT;

-- Create a BEFORE INSERT trigger function
CREATE OR REPLACE FUNCTION public.set_reference_number()
RETURNS trigger
LANGUAGE plpgsql
SECURITY DEFINER
SET search_path = public
AS $$
BEGIN
    IF NEW.reference_number IS NULL THEN
        NEW.reference_number := public.generate_reference_number();
    END IF;
    RETURN NEW;
END;
$$;

-- Revoke EXECUTE on the trigger function from client roles
REVOKE EXECUTE ON FUNCTION public.set_reference_number() FROM anon, authenticated, public;

-- Create the trigger
DROP TRIGGER IF EXISTS inspections_set_reference_number ON inspections;
CREATE TRIGGER inspections_set_reference_number
    BEFORE INSERT ON inspections
    FOR EACH ROW
    EXECUTE FUNCTION public.set_reference_number();
