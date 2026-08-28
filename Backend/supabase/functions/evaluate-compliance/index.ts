import { createClient } from "npm:@supabase/supabase-js@2";

const corsHeaders = {
  "Access-Control-Allow-Origin": "*",
  "Access-Control-Allow-Methods": "GET, POST, PUT, DELETE, OPTIONS",
  "Access-Control-Allow-Headers": "Content-Type, Authorization, X-Client-Info, Apikey",
};

Deno.serve(async (req: Request) => {
  if (req.method === "OPTIONS") {
    return new Response(null, { status: 200, headers: corsHeaders });
  }

  try {
    const authHeader = req.headers.get("Authorization");
    if (!authHeader) {
      return new Response(
        JSON.stringify({ error: "Missing authorization header" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const supabaseUrl = Deno.env.get("SUPABASE_URL")!;
    const supabaseAnonKey = Deno.env.get("SUPABASE_ANON_KEY")!;
    const supabase = createClient(supabaseUrl, supabaseAnonKey, {
      global: { headers: { Authorization: authHeader } },
    });

    const { data: { user }, error: authError } = await supabase.auth.getUser();
    if (authError || !user) {
      return new Response(
        JSON.stringify({ error: "Invalid or expired token" }),
        { status: 401, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    const body = await req.json();
    const inspectionId = body?.inspection_id;

    if (!inspectionId) {
      return new Response(
        JSON.stringify({ error: "inspection_id is required" }),
        { status: 400, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    // Verify the inspection belongs to the authenticated user
    const { data: inspection, error: inspectionError } = await supabase
      .from("inspections")
      .select("id, officer_id, status")
      .eq("id", inspectionId)
      .maybeSingle();

    if (inspectionError || !inspection) {
      return new Response(
        JSON.stringify({ error: "Inspection not found" }),
        { status: 404, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    if (inspection.officer_id !== user.id) {
      return new Response(
        JSON.stringify({ error: "Unauthorized access to this inspection" }),
        { status: 403, headers: { ...corsHeaders, "Content-Type": "application/json" } },
      );
    }

    // ── Placeholder: LMPC compliance evaluation will be implemented in the next phase ──
    // This function will evaluate extracted product data against LMPC rules
    // and update the inspection's compliance_score and status.

    return new Response(
      JSON.stringify({
        status: "not_implemented",
        message: "Compliance evaluation has not been implemented yet. This is a secure skeleton.",
        inspection_id: inspectionId,
        inspection_status: inspection.status,
      }),
      {
        status: 200,
        headers: { ...corsHeaders, "Content-Type": "application/json" },
      },
    );
  } catch (err) {
    return new Response(
      JSON.stringify({ error: err.message || "Internal server error" }),
      { status: 500, headers: { ...corsHeaders, "Content-Type": "application/json" } },
    );
  }
});
