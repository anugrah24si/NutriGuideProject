-- =====================================================================
-- 03_food_logs.sql — Catatan makanan harian milik user
-- Berisi: tabel food_logs + RLS.
-- Prasyarat: 01_profiles.sql, 02_foods.sql.
--
-- Catatan desain: kolom nilai gizi disimpan sebagai SNAPSHOT saat dicatat,
-- sehingga perubahan data master (foods) tidak mengubah histori log.
-- =====================================================================

create table if not exists public.food_logs (
    id         uuid primary key default gen_random_uuid(),
    user_id    uuid not null references public.profiles(id) on delete cascade,
    food_id    uuid references public.foods(id) on delete set null,
    meal_type  text,                          -- sarapan/siang/malam/snack
    name       text,
    portion    text,
    calories   int     default 0,
    protein_g  numeric default 0,
    carbs_g    numeric default 0,
    fat_g      numeric default 0,
    logged_at  timestamptz not null default now(),
    created_at timestamptz not null default now()
);

alter table public.food_logs enable row level security;

-- Pemilik bisa CRUD log miliknya sendiri.
create policy "logs_owner_rw"
    on public.food_logs for all to authenticated
    using (user_id = auth.uid()) with check (user_id = auth.uid());

-- Admin boleh membaca semua log (untuk statistik), tidak mengubah.
create policy "logs_admin_read"
    on public.food_logs for select to authenticated
    using (public.is_admin());
