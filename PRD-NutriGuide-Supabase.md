# PRD — NutriGuide + Supabase (CRUD & Sinkronisasi Admin–User)

Dokumen ini adalah Product Requirements Document (PRD) dan rencana teknis untuk
menambahkan fungsi CRUD nyata berbasis **Supabase** ke aplikasi NutriGuide,
serta membuat fitur **Admin** tersinkronisasi dengan akun **User/Member**.

- **Versi dokumen:** 1.0
- **Status:** Draft / Planning
- **Platform:** Android (Kotlin, View/XML, minSdk 24)
- **Backend:** Supabase (Auth + Postgres + RLS + Storage + Realtime)

---

## 1. Ringkasan Produk

NutriGuide adalah aplikasi pemantau gizi harian untuk mencegah stunting.
Saat ini seluruh data masih **statis** (hardcoded di layout/kode). Tahap ini
bertujuan mengubahnya menjadi data **nyata & tersimpan di cloud** menggunakan
Supabase, dengan dua peran:

- **Member (User):** mencatat makanan harian, melihat dashboard gizi, rekomendasi menu, artikel edukasi, grafik.
- **Admin:** mengelola data master (database makanan, artikel) dan memantau statistik pengguna.

Inti sinkronisasi: **Admin mengelola data master yang dibaca oleh semua user**,
sehingga setiap perubahan admin otomatis muncul di aplikasi user karena
sumber datanya satu (satu project Supabase, satu database).

---

## 2. Tujuan & Sasaran

| Tujuan | Ukuran Keberhasilan |
|--------|---------------------|
| Autentikasi nyata (bukan tombol dummy) | User bisa register/login, sesi tersimpan, peran terdeteksi otomatis |
| CRUD data makanan oleh admin | Admin bisa tambah/edit/hapus makanan; perubahan tampil di user |
| Pencatatan makanan oleh user | User bisa simpan log makanan, tersimpan per akun |
| Pemisahan hak akses aman | User tidak bisa mengubah data master; data pribadi user tidak bocor ke user lain |
| Sinkronisasi data master | Data makanan/artikel dari admin konsisten di semua perangkat user |

---

## 3. Kondisi Saat Ini (Baseline)

Sudah ada (UI lengkap, data statis):
- Entry: `WelcomeActivity`, `AuthActivity` (tombol User/Admin masih dummy → langsung pindah halaman)
- User (`ui/user/...`): Dashboard, Log, Menu, Recommendation, Article, Profile, Chart
- Admin (`ui/admin/...`): AdminDashboard, ManageFood (+form), AdminSettings
- Navigasi: `MainNav` (user) & `AdminNav` (admin)
- Widget grafik: `ui/shared/chart/...`

Belum ada: backend, autentikasi, penyimpanan data, dependency jaringan.

---

## 4. Lingkup

### Termasuk (In Scope)
- Integrasi Supabase Auth (email/password)
- Tabel: `profiles`, `foods`, `food_logs`, `articles`
- RLS (Row Level Security) untuk keamanan per peran
- CRUD makanan & artikel (admin), CRUD log makanan & profil (user)
- Penentuan peran admin/member via kolom `role`
- Menampilkan data master ke user (read-only)

### Tidak Termasuk (Out of Scope) — tahap ini
- Pembayaran / langganan
- Notifikasi push
- Login sosial (Google/Apple)
- Rekomendasi menu berbasis AI (tetap rule/statis dulu)
- Verifikasi email wajib (opsional, bisa diaktifkan nanti)

---

## 5. Peran Pengguna & Strategi Sinkronisasi (REKOMENDASI)

### 5.1 Dua peran dalam satu sistem auth
Gunakan **satu project Supabase** dan **satu sistem Auth** untuk admin & user.
Peran disimpan di tabel `profiles.role` dengan nilai `'admin'` atau `'member'`.

> Saat login, aplikasi membaca `profiles.role` milik user yang login, lalu
> mengarahkan ke `AdminDashboardActivity` (admin) atau `DashboardActivity` (member).
> Ini menggantikan tombol "Masuk sebagai Admin/User" yang sekarang masih dummy.

### 5.2 Pembagian data: Master vs Milik User
Ini kunci agar fitur admin **sinkron** dengan akun user:

| Jenis Data | Tabel | Pemilik | Admin | Member |
|-----------|-------|---------|-------|--------|
| Data makanan (master) | `foods` | Bersama | CRUD penuh | Hanya baca |
| Artikel edukasi (master) | `articles` | Bersama | CRUD penuh | Baca yang `published` |
| Profil & data kesehatan | `profiles` | Per user | Lihat semua (statistik) | CRUD milik sendiri |
| Log makanan harian | `food_logs` | Per user | Lihat semua (statistik) | CRUD milik sendiri |

**Kenapa bagus begini?**
- Data master (makanan, artikel) **satu sumber** → admin ubah sekali, semua user langsung dapat data sama. Inilah "sinkron" yang diminta.
- Data pribadi user (log, profil) **terisolasi per akun** → aman, tiap user hanya melihat miliknya.
- Admin tetap bisa melihat **agregat** (jumlah user, total log) untuk statistik dashboard, tanpa mengubah data pribadi user.
- (Opsional) **Supabase Realtime**: saat admin menambah makanan, perubahan bisa langsung muncul di layar user tanpa refresh manual.

### 5.3 Cara membuat seseorang jadi admin
- Default semua pendaftar = `member`.
- Admin pertama di-set manual lewat dashboard Supabase (ubah `role` jadi `admin`).
- (Opsional lanjutan) Admin bisa menaikkan peran user lain dari fitur "Daftar User".

---

## 6. Arsitektur Supabase

```
+----------------------------+
|        Supabase            |
|  ------------------------  |
|  Auth (email/password)     |  <-- identitas & sesi
|  Postgres (tabel data)     |  <-- profiles, foods, food_logs, articles
|  RLS (Row Level Security)  |  <-- aturan akses per baris/peran
|  Storage (opsional)        |  <-- gambar artikel / foto makanan
|  Realtime (opsional)       |  <-- update langsung ke user
+----------------------------+
            ^
            | HTTPS (supabase-kt)
            |
   +-------------------+
   |  Android App      |
   |  Repository layer |
   |  Activities/UI    |
   +-------------------+
```

Komponen Supabase yang dipakai:
- **Auth (GoTrue):** register, login, session, logout.
- **Database (PostgREST):** query/insert/update/delete tabel.
- **RLS:** keamanan tingkat baris (wajib aktif di produksi).
- **Storage (opsional):** simpan gambar.
- **Realtime (opsional):** stream perubahan data master.

---

## 7. Skema Database

> Catatan: `auth.users` adalah tabel bawaan Supabase. Tabel `profiles` terhubung 1:1 dengannya.

### 7.1 `profiles`
| Kolom | Tipe | Keterangan |
|-------|------|-----------|
| id | uuid (PK) | sama dengan `auth.users.id` |
| full_name | text | nama lengkap |
| email | text | email |
| role | text | `'admin'` / `'member'` (default `'member'`) |
| age | int | umur (tahun) |
| weight_kg | numeric | berat badan |
| height_cm | numeric | tinggi badan |
| activity_level | text | tingkat aktivitas |
| goal | text | tujuan (mis. cegah stunting) |
| daily_calorie_target | int | target kalori harian |
| created_at | timestamptz | default now() |

### 7.2 `foods` (master, dikelola admin)
| Kolom | Tipe | Keterangan |
|-------|------|-----------|
| id | uuid (PK) | default gen_random_uuid() |
| name | text | nama makanan |
| category | text | Karbohidrat/Protein/Sayuran/Buah/Lemak |
| serving | text | mis. "1 porsi (150g)" |
| calories | int | kkal |
| protein_g | numeric | gram |
| carbs_g | numeric | gram |
| fat_g | numeric | gram |
| fiber_g | numeric | gram |
| created_by | uuid | FK profiles.id |
| created_at | timestamptz | default now() |
| updated_at | timestamptz | |

### 7.3 `food_logs` (milik user)
| Kolom | Tipe | Keterangan |
|-------|------|-----------|
| id | uuid (PK) | |
| user_id | uuid | FK profiles.id (pemilik) |
| food_id | uuid (nullable) | FK foods.id (jika dari database) |
| meal_type | text | sarapan/siang/malam/snack |
| name | text | nama makanan (untuk input manual) |
| portion | text | porsi |
| calories | int | |
| protein_g | numeric | |
| carbs_g | numeric | |
| fat_g | numeric | |
| logged_at | timestamptz | waktu makan |
| created_at | timestamptz | default now() |

### 7.4 `articles` (master, dikelola admin)
| Kolom | Tipe | Keterangan |
|-------|------|-----------|
| id | uuid (PK) | |
| title | text | judul |
| category | text | mis. Nutrisi |
| content | text | isi artikel |
| read_time | text | mis. "5 min read" |
| author | text | penulis |
| status | text | `'draft'` / `'published'` |
| created_by | uuid | FK profiles.id |
| created_at | timestamptz | |
| updated_at | timestamptz | |

### 7.5 Relasi
```
auth.users 1───1 profiles 1───* food_logs *───1 foods
                         \
                          *── (created_by) ──* foods, articles
```

---

## 8. RLS Policies (Ringkasan Aturan Akses)

Aktifkan RLS di semua tabel. Buat helper untuk cek admin:

```sql
-- fungsi bantu: apakah user yang login adalah admin
create or replace function public.is_admin()
returns boolean language sql stable as $$
  select exists(
    select 1 from public.profiles
    where id = auth.uid() and role = 'admin'
  );
$$;
```

Ringkasan kebijakan:

| Tabel | SELECT | INSERT/UPDATE/DELETE |
|-------|--------|----------------------|
| `profiles` | pemilik (`id = auth.uid()`) atau admin | pemilik untuk update sendiri; admin boleh semua |
| `foods` | semua user login | hanya admin (`is_admin()`) |
| `articles` | `status='published'` untuk semua; admin lihat semua | hanya admin |
| `food_logs` | pemilik (`user_id = auth.uid()`) atau admin | pemilik saja |

Contoh policy `foods` (baca semua, tulis admin):
```sql
alter table public.foods enable row level security;

create policy "foods_select_all_authenticated"
  on public.foods for select to authenticated using (true);

create policy "foods_admin_write"
  on public.foods for all to authenticated
  using (public.is_admin()) with check (public.is_admin());
```

Contoh policy `food_logs` (hanya milik sendiri):
```sql
alter table public.food_logs enable row level security;

create policy "logs_owner_rw"
  on public.food_logs for all to authenticated
  using (user_id = auth.uid()) with check (user_id = auth.uid());
```

---

## 9. Pemetaan Fitur ↔ CRUD ↔ Peran

| Layar | Operasi | Tabel | Peran |
|-------|---------|-------|-------|
| Auth (register) | Create profile | `profiles` | publik → member |
| Auth (login) | Read role | `profiles` | semua |
| Admin · Kelola Data Makanan | Create/Read/Update/Delete | `foods` | admin |
| Admin · Buat Artikel | Create/Read/Update/Delete | `articles` | admin |
| Admin · Daftar User | Read (+ ubah role opsional) | `profiles` | admin |
| Admin · Dashboard | Read agregat (count) | `profiles`,`food_logs`,`foods`,`articles` | admin |
| User · Log Makanan | Read `foods` + Create/Delete `food_logs` | `foods`,`food_logs` | member |
| User · Dashboard | Read ringkasan log hari ini | `food_logs` | member |
| User · Rekomendasi | Read `foods` (+ hitung) | `foods` | member |
| User · Artikel | Read published | `articles` | member |
| User · Profil | Read/Update | `profiles` | member |
| User · Grafik | Read agregat log sendiri | `food_logs` | member |

---

## 10. Integrasi Android

### 10.1 Dependencies (Gradle)
Gunakan klien Kotlin resmi komunitas **supabase-kt** + Ktor + kotlinx-serialization.
> Versi harus diverifikasi ke versi terbaru saat implementasi.

`gradle/libs.versions.toml` (tambahan, contoh):
```toml
[versions]
supabase = "<versi-terbaru>"   # BOM supabase-kt
ktor = "<versi-terbaru>"
kotlinSerialization = "<versi-kompatibel-kotlin>"

[libraries]
supabase-bom        = { group = "io.github.jan-tennert.supabase", name = "bom", version.ref = "supabase" }
supabase-postgrest  = { group = "io.github.jan-tennert.supabase", name = "postgrest-kt" }
supabase-auth       = { group = "io.github.jan-tennert.supabase", name = "auth-kt" }
supabase-realtime   = { group = "io.github.jan-tennert.supabase", name = "realtime-kt" }
ktor-client-android = { group = "io.ktor", name = "ktor-client-android", version.ref = "ktor" }
```

`app/build.gradle.kts` (tambahan):
```kotlin
plugins {
    alias(libs.plugins.android.application)
    kotlin("plugin.serialization") version "<versi-kotlin>"  // untuk @Serializable
}

dependencies {
    implementation(platform(libs.supabase.bom))
    implementation(libs.supabase.postgrest)
    implementation(libs.supabase.auth)
    implementation(libs.supabase.realtime)
    implementation(libs.ktor.client.android)
    // coroutines untuk async
}
```

### 10.2 Izin internet
`AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.INTERNET" />
```

### 10.3 Struktur kode yang diusulkan (rapi, lanjutan dari pemisahan user/admin)
```
data/
├── remote/
│   └── SupabaseClient.kt        // inisialisasi client (URL + anon key)
├── model/                        // data class @Serializable
│   ├── Profile.kt
│   ├── Food.kt
│   ├── FoodLog.kt
│   └── Article.kt
└── repository/
    ├── AuthRepository.kt         // register, login, logout, currentRole
    ├── FoodRepository.kt         // CRUD foods
    ├── FoodLogRepository.kt      // CRUD food_logs
    └── ArticleRepository.kt      // CRUD articles
ui/ ... (seperti sekarang; activity memanggil repository)
```

> Kunci & URL Supabase (anon key) disimpan di `local.properties`/`BuildConfig`,
> jangan di-hardcode di repo publik. Gunakan **anon key** di app (aman dipakai
> bersama RLS), bukan service key.

### 10.4 Pola pemakaian (contoh konsep, bukan final)
```kotlin
// Inisialisasi (sekali)
val supabase = createSupabaseClient(BuildConfig.SUPABASE_URL, BuildConfig.SUPABASE_ANON_KEY) {
    install(Auth); install(Postgrest); install(Realtime)
}

// Admin tambah makanan
suspend fun addFood(food: Food) {
    supabase.from("foods").insert(food)   // RLS otomatis menolak jika bukan admin
}

// User baca daftar makanan
suspend fun getFoods(): List<Food> =
    supabase.from("foods").select().decodeList()
```

---

## 11. Alur Utama (Flow)

### 11.1 Register/Login
```
User isi email+password → AuthRepository.signUp()/signIn()
 → buat/baca row profiles → baca role
 → role == 'admin' ? AdminDashboardActivity : DashboardActivity
```

### 11.2 Admin tambah makanan (sinkron ke user)
```
Admin isi form → FoodRepository.insert(food)
 → tersimpan di tabel foods (sumber tunggal)
 → User buka Log/Recommendation → FoodRepository.getFoods()
 → makanan baru otomatis muncul (atau realtime push)
```

### 11.3 User catat makanan
```
User pilih makanan/isi manual → FoodLogRepository.insert(log dengan user_id)
 → tersimpan per akun → Dashboard & Grafik membaca log milik user
```

---

## 12. Rencana Bertahap (Milestones)

### Fase 0 — Persiapan (fondasi) — ✅ SELESAI (sisi kode)
- [ ] Buat project Supabase, catat URL & anon key  *(aksi kamu)*
- [x] Tambah dependency (OkHttp) + izin INTERNET + `SupabaseClient.kt`
- [x] Simpan kredensial via `local.properties` → `BuildConfig`
> Catatan: memakai **OkHttp + org.json** (bukan supabase-kt) agar aman di setup AGP 9 (Kotlin bawaan) tanpa plugin serialization.

### Fase 1 — Autentikasi & Peran — ✅ SELESAI (sisi kode)
- [x] Buat tabel `profiles` + trigger auto-create profile saat sign up  *(SQL: `01_profiles.sql`)*
- [x] `AuthRepository` (register, login, getRole) + `SessionManager`
- [x] Ganti tombol dummy di `AuthActivity` → login nyata + routing per role
- [x] Logout nyata di Profile/AdminSettings (hapus sesi)

### Fase 2 — Master Data Makanan (Admin) + tampil di User — 🔄 BERJALAN
- [x] Tabel `foods` + RLS  *(SQL: `02_foods.sql`)* + seed `07_seed_foods.sql`
- [x] `FoodRepository` (CRUD) — diuji live ke Supabase (insert/list/delete OK)
- [x] `ManageFoodActivity`: daftar dinamis (RecyclerView) + form Simpan/Edit/Hapus nyata
- [x] `LogActivity` membaca `foods` dari Supabase (RecyclerView + pencarian)  *(Recommendation tetap kurasi statis)*
- [x] Realtime: daftar makanan ter-update otomatis (admin & user) via `RealtimeTable`

> **Fase 2 SELESAI.** Catatan: jalankan `06_realtime.sql` agar tabel `foods` masuk publication `supabase_realtime`.

### Fase 3 — Log Makanan (User) — ✅ SELESAI
- [x] Tabel `food_logs` + RLS  *(SQL: `03_food_logs.sql`)*
- [x] `FoodLogRepository` (add + getTodayLogs) — diuji live
- [x] `LogActivity`: tombol "+" & input manual menyimpan ke `food_logs`
- [x] `DashboardActivity`: ringkasan kalori/gizi + daftar "Log Makanan Hari Ini" dari data nyata

### Fase 4 — Artikel (Admin) + tampil di User — ✅ SELESAI
- [x] Tabel `articles` + RLS  *(SQL: `04_articles.sql`)*
- [x] `ArticleRepository` (CRUD + getPublished) — diuji live
- [x] Layar admin **Kelola Artikel** (`ManageArticleActivity`): tambah/edit/hapus + status draft/published
- [x] Home menampilkan artikel published terbaru; `ArticleActivity` menampilkan isi artikel dari DB

### Fase 5 — Statistik & Penyempurnaan — ✅ SELESAI
- [x] Admin Dashboard: angka nyata (count user, makanan, artikel, log hari ini) via `AdminRepository`
- [x] Layar baru admin: **Daftar User** (`UserListActivity`) menampilkan semua pengguna + role
- [x] Layar baru admin: **Kelola Artikel** (sudah di Fase 4)
- [ ] (Opsional) Storage gambar, auto-refresh token

> **SEMUA FASE INTI (0–5) SELESAI.** 5 fitur admin lengkap; sinkronisasi admin→member berjalan dengan Realtime.

---

## 13. Acceptance Criteria (Kriteria Selesai)

- [ ] User baru bisa register dan otomatis jadi `member`.
- [ ] Login mengarahkan ke halaman sesuai peran (admin/member).
- [ ] Admin menambah makanan → muncul di daftar admin DAN di halaman user.
- [ ] Member TIDAK bisa menambah/menghapus makanan (ditolak RLS).
- [ ] User mencatat makanan → hanya muncul di akun user tersebut.
- [ ] User A tidak bisa melihat log milik User B.
- [ ] Logout menghapus sesi; halaman terproteksi tidak bisa diakses tanpa login.

---

## 14. Risiko & Mitigasi

| Risiko | Dampak | Mitigasi |
|--------|--------|----------|
| RLS salah konfigurasi | Data bocor / akses ditolak | Uji policy per peran sebelum rilis |
| Anon key tersebar | — (aman bila RLS benar) | Wajib RLS aktif; jangan pakai service key di app |
| Operasi jaringan blok UI | App lag/ANR | Pakai coroutine (Dispatchers.IO), loading state |
| Skema berubah | Bug parsing | Versi-kan migrasi SQL, model `@Serializable` selaras kolom |
| Tanpa internet | Fitur gagal | Tampilkan pesan error & state kosong yang rapi |

---

## 15. Pertanyaan Terbuka (perlu keputusan)

1. Verifikasi email saat register: wajib atau tidak? (default: tidak, demi kemudahan demo)
2. Apakah admin perlu fitur menaikkan member jadi admin dari aplikasi, atau cukup lewat dashboard Supabase?
3. ~~Realtime sync: dipakai sekarang atau cukup refresh manual dulu?~~ **KEPUTUSAN: Pakai Realtime** (lihat Bagian 16).
4. Penyimpanan gambar (artikel/makanan): pakai Supabase Storage atau URL eksternal?
5. Target kalori harian: dihitung otomatis (BMR) atau diisi manual di profil?

---

## 16. Keputusan: Realtime & Perubahan/Tambahan di Sisi Admin

**Keputusan:** Aplikasi memakai **Supabase Realtime** untuk data master
(`foods`, `articles`) sehingga perubahan admin langsung tampil di member
(dan di admin lain) tanpa refresh manual.

### 16.1 Yang WAJIB dirubah di fitur admin
| Bagian | Kondisi sekarang | Perubahan untuk CRUD + Realtime |
|--------|------------------|---------------------------------|
| Kelola Data Makanan — daftar | 6 kartu hardcoded di XML | Ganti ke **RecyclerView + Adapter**, isi dari tabel `foods` |
| Kelola Data Makanan — tambah | Form Simpan → Toast saja | **Insert** ke `foods` |
| Kelola Data Makanan — edit | Tombol edit → Toast | **Mode edit**: form terisi data lama → **Update** |
| Kelola Data Makanan — hapus | Dialog → Toast | **Delete** baris di `foods` |
| Hitungan "(6 item)" | Statis | **Dinamis** dari jumlah baris |
| Login admin | Tombol "Masuk sebagai Admin" (dummy) | Login nyata → baca `profiles.role` → arahkan ke Admin Dashboard |
| Admin Dashboard — statistik | Angka statis (410, 1.250, dst.) | **Count nyata** dari DB |

### 16.2 Yang perlu DITAMBAH (layar baru)
- **Kelola Artikel Edukasi** (`ui/admin/ManageArticleActivity`): CRUD `articles`.
- **Daftar User** (`ui/admin/UserListActivity`): lihat member, (opsional) ubah `role`.

### 16.3 Teknis pendukung Realtime
- Kelola lifecycle channel Realtime: **subscribe** di `onStart`, **unsubscribe** di `onStop` → cegah memory leak/koneksi nyangkut.
- Aktifkan Realtime pada tabel terkait di dashboard Supabase (Replication/Realtime).
- Tambahkan **state UI**: loading, kosong (empty), dan error jaringan.
- Realtime memerlukan modul `realtime-kt` (sudah dicantum di Bagian 10.1).

### 16.4 Catatan: snapshot vs master data (penting)
`food_logs` menyimpan **salinan nilai gizi saat dicatat** (snapshot). Jadi bila
admin mengubah/menghapus makanan di `foods`, **log lama member tidak berubah**
(histori tetap akurat). Realtime hanya memengaruhi tampilan **data master**
(daftar makanan & artikel), bukan histori log.

---

## 17. Daftar Resmi Fitur Admin (Disetujui)

Total **5 fitur admin**: 3 fitur yang sudah ada (diaktifkan dengan Supabase) + 2 fitur baru.

| # | Fitur Admin | Status sekarang | Yang dikerjakan | Tabel terkait | CRUD |
|---|-------------|-----------------|-----------------|---------------|------|
| 1 | **Admin Dashboard** | Ada (statis) | Statistik jadi angka nyata + realtime count | `profiles`,`foods`,`food_logs`,`articles` | Read (agregat) |
| 2 | **Kelola Data Makanan** | Ada (statis) | Daftar dinamis (RecyclerView), Tambah/Edit/Hapus nyata, **Realtime** | `foods` | Create, Read, Update, Delete |
| 3 | **Pengaturan Admin** | Ada (statis) | Profil admin + logout nyata | `profiles` | Read, Update |
| 4 | **Kelola Artikel Edukasi** | ❌ Baru | Layar CRUD artikel | `articles` | Create, Read, Update, Delete |
| 5 | **Daftar User** | ❌ Baru | Lihat member, (opsional) ubah role | `profiles` | Read, (Update role opsional) |

**Fondasi bersama (wajib, bukan khusus admin):** Auth nyata (login/register + deteksi role),
`SupabaseClient`, model data, dan repository (`AuthRepository`, `FoodRepository`,
`FoodLogRepository`, `ArticleRepository`).

**Prioritas pengerjaan fitur admin:** #2 (inti sinkronisasi) → #1 → #3 → #4 → #5
(#5 boleh paling akhir / opsional karena tidak wajib untuk alur inti).

---

## Lampiran A — Urutan Pengerjaan yang Disarankan
1. Fase 0 → 1 (fondasi + auth) dulu, karena semua fitur bergantung pada identitas & peran.
2. Lanjut Fase 2 (foods) karena ini inti "sinkron admin → user".
3. Baru Fase 3–5.

> Setelah PRD ini disetujui, langkah berikutnya: setup project Supabase &
> Fase 0 (dependency + SupabaseClient). Beri tahu jika ingin saya mulai dari sana.

---

## Lampiran B — Akun Demo (Seed untuk Testing Login)

Akun bawaan untuk pengujian login (admin & member). File SQL lengkap ada di
repo: `supabase/05_seed_accounts.sql`. Jalankan di Supabase → **SQL Editor** → Run.

| Peran | Email | Password |
|-------|-------|----------|
| Admin | `tra@gmail.com` | `Admin123!` |
| Member | `defani@gmail.com` | `Member123!` |

**Prasyarat:** tabel `public.profiles` sudah dibuat (Bagian 7.1). UUID dibuat tetap:
admin `1111…`, member `2222…`.

```sql
-- Ekstensi untuk hashing password
create extension if not exists pgcrypto;

-- 1) Buat user di auth.users (password di-hash, email langsung terkonfirmasi)
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

-- 2) Buat identity provider email (wajib agar login email/password jalan)
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

-- 3) Set role di public.profiles (admin / member)
insert into public.profiles (
    id, full_name, email, role,
    age, weight_kg, height_cm, activity_level, goal, daily_calorie_target
) values
('11111111-1111-1111-1111-111111111111','Admin NutriGuide','tra@gmail.com','admin',
 30,70,172,'Sedang','Menjaga Berat Badan',2200),
('22222222-2222-2222-2222-222222222222','Defani','defani@gmail.com','member',
 25,65,165,'Sedang','Cegah Stunting',2000)
on conflict (id) do update
set full_name = excluded.full_name, email = excluded.email, role = excluded.role;
```

**Catatan:**
- Cara paling aman membuat akun: Dashboard → Authentication → **Add user**
  (auto-confirm), lalu cukup jalankan **langkah 3** untuk set role.
- Struktur kolom schema `auth` bisa berbeda antar versi Supabase; jika ada error
  kolom saat Run, sesuaikan skrip (mis. kolom `provider_id`).
- Ganti password default setelah testing.

---

## Kesimpulan

NutriGuide akan diubah dari aplikasi berdata statis menjadi aplikasi berdata
nyata dengan **Supabase** (Auth + Postgres + RLS + **Realtime**), dalam satu
backend yang sama untuk admin dan member.

**Inti sinkronisasi:** admin dan member memakai **sumber data yang sama**.
Data master (`foods`, `articles`) dikelola admin dan dibaca semua member,
sehingga setiap perubahan admin **otomatis sinkron** ke semua akun member —
dan dengan **Realtime**, perubahan tampil **langsung tanpa refresh**. Data
pribadi member (`food_logs`, `profiles`) tetap terisolasi per akun dan dijaga
oleh **RLS**. Histori log aman karena memakai **snapshot** nilai gizi saat dicatat.

**Cakupan fitur admin: 5 fitur** — Admin Dashboard, Kelola Data Makanan,
Pengaturan Admin (3 diaktifkan), serta Kelola Artikel Edukasi dan Daftar User
(2 baru). Ditopang fondasi bersama: autentikasi nyata, `SupabaseClient`, model,
dan repository.

**Rencana eksekusi:** dikerjakan bertahap **Fase 0 → 5**, dimulai dari fondasi
(setup + auth), lalu fitur inti makanan (sinkronisasi admin→member), kemudian
log makanan, artikel, dan terakhir statistik + Realtime + penyempurnaan.

**Manfaat akhir:** satu sumber data yang konsisten, aman per peran, real-time,
dan mudah dikembangkan karena struktur kode sudah dipisah rapi (`ui/user`,
`ui/admin`, `ui/shared`, `data/...`).

**Langkah berikutnya:** mulai **Fase 0** setelah kredensial Supabase (URL & anon
key) tersedia dan Pertanyaan Terbuka #1, #2, #4, #5 diputuskan.
