-- =====================================================================
-- 02_foods.sql — Data master makanan (dikelola admin, dibaca semua user)
-- Berisi: tabel foods + RLS.
-- Prasyarat: 01_profiles.sql (butuh fungsi public.is_admin()).
-- =====================================================================

create table if not exists public.foods (
    id         uuid primary key default gen_random_uuid(),
    name       text not null,
    category   text,                       -- Karbohidrat/Protein/Sayuran/Buah/Lemak
    serving    text,                        -- contoh: "1 porsi (150g)"
    calories   int     default 0,
    protein_g  numeric default 0,
    carbs_g    numeric default 0,
    fat_g      numeric default 0,
    fiber_g    numeric default 0,
    created_by uuid references public.profiles(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz
);

alter table public.foods enable row level security;

-- Semua user login boleh membaca data makanan.
create policy "foods_select_all"
    on public.foods for select to authenticated
    using (true);

-- Hanya admin yang boleh menambah/mengubah/menghapus.
create policy "foods_admin_write"
    on public.foods for all to authenticated
    using (public.is_admin()) with check (public.is_admin());
