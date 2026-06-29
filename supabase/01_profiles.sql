-- =====================================================================
-- 01_profiles.sql — Profil pengguna + peran (admin/member)
-- Berisi: tabel profiles, trigger auto-create profil saat daftar,
--         fungsi is_admin(), dan RLS.
-- Prasyarat: 00_extensions.sql.
-- =====================================================================

-- Tabel profil (1:1 dengan auth.users)
create table if not exists public.profiles (
    id                   uuid primary key references auth.users(id) on delete cascade,
    full_name            text,
    email                text,
    role                 text not null default 'member' check (role in ('admin','member')),
    age                  int,
    weight_kg            numeric,
    height_cm            numeric,
    activity_level       text,
    goal                 text,
    daily_calorie_target int,
    created_at           timestamptz not null default now()
);

-- Auto-create profil ketika user baru mendaftar di auth.users
create or replace function public.handle_new_user()
returns trigger
language plpgsql
security definer
set search_path = public
as $$
begin
    insert into public.profiles (id, full_name, email)
    values (new.id, coalesce(new.raw_user_meta_data->>'full_name', ''), new.email)
    on conflict (id) do nothing;
    return new;
end;
$$;

drop trigger if exists on_auth_user_created on auth.users;
create trigger on_auth_user_created
    after insert on auth.users
    for each row execute function public.handle_new_user();

-- Helper: apakah user yang login adalah admin?
-- security definer + search_path agar tidak terkena RLS (hindari rekursi policy).
create or replace function public.is_admin()
returns boolean
language sql
stable
security definer
set search_path = public
as $$
    select exists(
        select 1 from public.profiles
        where id = auth.uid() and role = 'admin'
    );
$$;

-- RLS
alter table public.profiles enable row level security;

create policy "profiles_select_self_or_admin"
    on public.profiles for select to authenticated
    using (id = auth.uid() or public.is_admin());

create policy "profiles_insert_self"
    on public.profiles for insert to authenticated
    with check (id = auth.uid());

create policy "profiles_update_self"
    on public.profiles for update to authenticated
    using (id = auth.uid()) with check (id = auth.uid());

create policy "profiles_admin_update_all"
    on public.profiles for update to authenticated
    using (public.is_admin()) with check (public.is_admin());
