-- =====================================================================
-- 06_realtime.sql — Aktifkan Realtime untuk data master
-- Agar perubahan admin pada foods & articles langsung diterima app member.
-- Aman dijalankan ulang (cek dulu sebelum menambah ke publication).
-- =====================================================================

do $$
begin
    if not exists (
        select 1 from pg_publication_tables
        where pubname = 'supabase_realtime' and schemaname = 'public' and tablename = 'foods'
    ) then
        alter publication supabase_realtime add table public.foods;
    end if;

    if not exists (
        select 1 from pg_publication_tables
        where pubname = 'supabase_realtime' and schemaname = 'public' and tablename = 'articles'
    ) then
        alter publication supabase_realtime add table public.articles;
    end if;
end $$;
