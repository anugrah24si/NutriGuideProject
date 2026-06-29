-- =====================================================================
-- 07_seed_foods.sql — Data awal makanan (opsional)
-- Hanya mengisi bila tabel foods masih kosong (aman dijalankan ulang).
-- Prasyarat: 01_profiles.sql, 02_foods.sql, 05_seed_accounts.sql.
-- =====================================================================

insert into public.foods (name, category, serving, calories, protein_g, carbs_g, fat_g, fiber_g, created_by)
select v.name, v.category, v.serving, v.calories, v.protein_g, v.carbs_g, v.fat_g, v.fiber_g,
       '11111111-1111-1111-1111-111111111111'
from (values
    ('Nasi Putih',   'Karbohidrat', '1 porsi (150g)',  180,  4.0, 40.0, 0.3, 0.6),
    ('Ayam Goreng',  'Protein',     '1 potong (100g)', 250, 25.0,  8.0, 14.0, 0.0),
    ('Telur Rebus',  'Protein',     '1 butir',          78,  6.0,  0.6, 5.0, 0.0),
    ('Bayam',        'Sayuran',     '1 mangkok',         23,  3.0,  4.0, 0.3, 2.2),
    ('Tempe Goreng', 'Protein',     '2 potong',         150, 14.0,  9.0, 8.0, 3.0),
    ('Pisang',       'Buah',        '1 buah',           105,  1.3, 27.0, 0.4, 3.1)
) as v(name, category, serving, calories, protein_g, carbs_g, fat_g, fiber_g)
where not exists (select 1 from public.foods);
