-- =====================================================================
-- 00_extensions.sql — Ekstensi database yang dibutuhkan
-- Jalankan PALING AWAL.
-- =====================================================================

-- pgcrypto: untuk crypt()/gen_salt() (hash password saat seed akun)
-- sekaligus menyediakan gen_random_uuid()
create extension if not exists pgcrypto;
