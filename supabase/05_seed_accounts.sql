-- =====================================================================
-- 05_seed_accounts.sql — Seed Akun Admin & Member (untuk testing login)
-- Jalankan SETELAH 00..04.
--   ADMIN  : tra@gmail.com     / password: Admin123!
--   MEMBER : defani@gmail.com  / password: Member123!
--
-- Alternatif paling aman: buat user lewat Dashboard → Authentication →
-- Add user (auto-confirm), lalu jalankan HANYA BAGIAN 3 (set role).
-- =====================================================================

-- ---------------------------------------------------------------------
-- BAGIAN 1 — Buat user di auth.users (UUID dibuat tetap)
-- ---------------------------------------------------------------------
insert into auth.users (
    instance_id, id, aud, role, email,
    encrypted_password, email_confirmed_at,
    created_at, updated_at,
    raw_app_meta_data, raw_user_meta_data,
    confirmation_token, recovery_token, email_change_token_new, email_change
) values
('00000000-0000-0000-0000-000000000000','11111111-1111-1111-1111-111111111111',
 'authenticated','authenticated','tra@gmail.com',
 crypt('Admin123!', gen_salt('bf')), now(), now(), now(),
 '{"provider":"email","providers":["email"]}','{"full_name":"Admin NutriGuide"}','','','',''),
('00000000-0000-0000-0000-000000000000','22222222-2222-2222-2222-222222222222',
 'authenticated','authenticated','defani@gmail.com',
 crypt('Member123!', gen_salt('bf')), now(), now(), now(),
 '{"provider":"email","providers":["email"]}','{"full_name":"Defani"}','','','','')
on conflict (id) do nothing;

-- ---------------------------------------------------------------------
-- BAGIAN 2 — Identity provider email (wajib agar login email/password jalan)
-- ---------------------------------------------------------------------
insert into auth.identities (
    id, user_id, provider_id, identity_data, provider,
    last_sign_in_at, created_at, updated_at
) values
(gen_random_uuid(),'11111111-1111-1111-1111-111111111111','11111111-1111-1111-1111-111111111111',
 '{"sub":"11111111-1111-1111-1111-111111111111","email":"tra@gmail.com","email_verified":true}',
 'email', now(), now(), now()),
(gen_random_uuid(),'22222222-2222-2222-2222-222222222222','22222222-2222-2222-2222-222222222222',
 '{"sub":"22222222-2222-2222-2222-222222222222","email":"defani@gmail.com","email_verified":true}',
 'email', now(), now(), now())
on conflict do nothing;

-- ---------------------------------------------------------------------
-- BAGIAN 3 — Set data profil + role di public.profiles
-- (Aman dijalankan ulang; meng-update role bila profil sudah dibuat trigger.)
-- ---------------------------------------------------------------------
insert into public.profiles (
    id, full_name, email, role,
    age, weight_kg, height_cm, activity_level, goal, daily_calorie_target
) values
('11111111-1111-1111-1111-111111111111','Admin NutriGuide','tra@gmail.com','admin',
 30,70,172,'Sedang','Menjaga Berat Badan',2200),
('22222222-2222-2222-2222-222222222222','Defani','defani@gmail.com','member',
 25,65,165,'Sedang','Cegah Stunting',2000)
on conflict (id) do update
set full_name = excluded.full_name,
    email     = excluded.email,
    role      = excluded.role;

-- ---------------------------------------------------------------------
-- VERIFIKASI (opsional)
-- ---------------------------------------------------------------------
-- select u.email, p.role
-- from auth.users u join public.profiles p on p.id = u.id
-- where u.email in ('tra@gmail.com','defani@gmail.com');
