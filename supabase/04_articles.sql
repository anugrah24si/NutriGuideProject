-- =====================================================================
-- 04_articles.sql — Artikel edukasi (dikelola admin, dibaca member)
-- Berisi: tabel articles + RLS.
-- Prasyarat: 01_profiles.sql (butuh public.is_admin()).
-- =====================================================================

create table if not exists public.articles (
    id         uuid primary key default gen_random_uuid(),
    title      text not null,
    category   text,
    content    text,
    read_time  text,                         -- contoh: "5 min read"
    author     text,
    status     text not null default 'draft' check (status in ('draft','published')),
    created_by uuid references public.profiles(id),
    created_at timestamptz not null default now(),
    updated_at timestamptz
);

alter table public.articles enable row level security;

-- Member hanya melihat artikel published; admin melihat semua (termasuk draft).
create policy "articles_select_published_or_admin"
    on public.articles for select to authenticated
    using (status = 'published' or public.is_admin());

-- Hanya admin yang boleh tambah/ubah/hapus.
create policy "articles_admin_write"
    on public.articles for all to authenticated
    using (public.is_admin()) with check (public.is_admin());
