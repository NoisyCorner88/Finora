# 📌 Finora Milestone & Progress Roadmap

Dokumen ini mencatat pencapaian fitur yang telah diselesaikan berdasarkan desain pada folder `FinoraUI New`, integrasi fitur **Scan Struk (OCR) dengan Google ML Kit & Import Galeri**, serta daftar milestone untuk progres pengembangan lanjutan yang belum terselesaikan.

---

## 🚀 Milestone 1: Core Design & Fitur Scan Struk OCR (SELESAI ✅)

### 1. Integrasi Google ML Kit & CameraX
- [x] **Dependensi ML Kit**: Penambahan library Google ML Kit Text Recognition (`com.google.mlkit:text-recognition:16.0.1`) pada `libs.versions.toml` dan `build.gradle.kts`.
- [x] **Dependensi CameraX**: Penambahan CameraX (`camera-core`, `camera-camera2`, `camera-lifecycle`, `camera-view:1.4.1`) untuk live camera preview dan capture receipt.
- [x] **Izin & Hardware Camera**: Deklarasi `CAMERA`, `READ_MEDIA_IMAGES`, dan `READ_EXTERNAL_STORAGE` di `AndroidManifest.xml`.

### 2. Fitur Scan Struk & Import Galeri (`camera.png` & `Struk Scan.png`)
- [x] **Kamera Viewfinder & Deteksi Real-Time (`ScanReceiptActivity`)**:
  - Tampilan rounded frame camera preview dengan guideline pemindaian struk.
  - Indikator status sensor OCR aktif dan toggle flash / senter.
  - Preview deteksi nama merchant dan estimasi total pembayaran otomatis.
  - Tombol **GALERI**: Memungkinkan pengguna mengimpor gambar struk langsung dari galeri ponsel via `ActivityResultContracts.GetContent`.
  - Tombol **KAMERA**: Beralih antara kamera belakang dan kamera depan.
  - Tombol **Shutter / Struk Otomatis**: Menangkap gambar dan memproses teks struk menggunakan ML Kit.
- [x] **Mesin Parser Struk Pintar (`ReceiptOcrParser`)**:
  - Ekstraksi otomatis nama toko / merchant (Indomaret, Alfamart, Kopi Kenangan, dll.).
  - Ekstraksi tanggal & waktu transaksi.
  - Pemisahan item barang, jumlah (qty), dan harga satuan.
  - Ekstraksi subtotal, diskon / promo toko, potongan pajak (PB1/PPN), dan total pembayaran.
  - Prediksi otomatis kategori transaksi (Makanan & Minuman, Belanja, Transportasi, Tagihan).
- [x] **Halaman Detail Struk Transaksi (`ReceiptDetailActivity`)**:
  - Thumbnail struk belanja fisik dengan tombol **Perbesar Struk** (tampilan zoom struk).
  - Badge *OCR Terverifikasi*.
  - Verifikasi dan edit data merchant, tanggal, dan kategori.
  - Daftar item belanja terbaca: dukungan tambah item baru, edit item, dan hapus item dengan auto-recalculate total.
  - Ringkasan biaya lengkap: Subtotal, Diskon, Pajak, dan Total Pembayaran.
  - Pilihan sumber dana / rekening untuk memotong saldo.
  - Tombol **Konfirmasi & Simpan ke Saldo**: Menyimpan data struk ke Room Database (`ReceiptEntity` + `ReceiptItemEntity` + `TransactionEntity`), otomatis memotong saldo rekening terpilih, dan membuat riwayat notifikasi.
  - Tombol **Foto Ulang Struk**: Kembali ke kamera pemindaian.

### 3. Penyesuaian Desain & Fitur Berdasarkan `FinoraUI New`
- [x] **Beranda (`HomeFragment`)**:
  - Sapaan nama pengguna dan avatar inisial dinamis.
  - Kartu saldo aktif dengan toggle visibility (sembunyikan / tampilkan saldo).
  - 4 Menu Aksi Cepat: **Scan** (membuka kamera OCR), **Kartu** (membuka manajemen kartu), **Analisis** (beralih ke tab Statistik), **Transaksi** (tambah transaksi manual).
  - Tampilan kartu bank/e-wallet interaktif dengan indikator nomor rekening bertopeng dan nama bank.
  - Tombol tengah kamera pada Bottom Navigation langsung membuka fitur Scan Struk.
- [x] **Tambah Transaksi (`AddTransactionActivity`)**:
  - Tab switcher: **Keluar** (merah aktif) dan **Masuk** (hijau aktif).
  - Nominal display besar dengan quick chips nominal: `+Rp10.000`, `+Rp50.000`, `+Rp100.000`.
  - Judul transaksi dengan counter karakter (0/40) dan ikon pensil.
  - Detail (opsional) dengan ikon dokumen.
  - Pemilihan kategori horizontal dan pemilihan sumber dana.
- [x] **Kartu Saya (`CardActivity` & `dialog_edit_card.xml`)**:
  - Filter kategori kartu: *Semua*, *Debit*, *E-Wallet*, dan *Cash*.
  - Preview kartu besar dengan warna kartu dinamis, chip, nomor kartu, dan saldo.
  - Bagian Informasi Rekening dengan tombol **Edit Kartu**.
  - Modal dialog **Edit Kartu**:
    - Input nama bank / e-wallet.
    - Pilihan palet warna kartu (Biru, Oranye, Hijau, Ungu, Pink, Dark).
    - Input saldo dan 3 digit terakhir nomor rekening.
    - Tombol simpan perubahan dan hapus kartu.
  - Tombol tambah kartu baru (+).
- [x] **Notifikasi (`NotificationActivity` & `item_notification.xml`)**:
  - Switch toggle *Izinkan Notifikasi*.
  - Riwayat notifikasi transaksi keluar dan masuk dengan badge unread dan penanda nominal.
- [x] **Profil (`ProfileFragment`)**:
  - Kartu profil dengan avatar dan tombol **Edit Profil**.
  - 3 Kartu statistik: **Total Saldo**, **Total Transaksi**, dan **Total Scan (Struk Tervalidasi)**.
  - Menu interaktif lengkap:
    - *Kartu Saya* -> Beralih ke `CardActivity`.
    - *Ekspor Data Keuangan* -> Dialog pilihan format CSV / PDF.
    - *Ubah kata sandi* -> Dialog ganti kata sandi dengan verifikasi dan minimal 8 karakter.
    - *Kelola Notifikasi* -> Beralih ke `NotificationActivity`.
    - *Hapus Akun* -> Dialog konfirmasi penghapusan akun permanen dengan input kata sandi.
    - *Keluar* -> Dialog konfirmasi logout.
- [x] **Statistik & Aktivitas (`StatsFragment` & `ActivityFragment`)**:
  - Filter periode (Bulan Ini, Bulan Lalu, Semua Waktu).
  - Batas anggaran dengan progress bar dan persentase pemakaian.
  - Visualisasi tren mingguan (M1, M2, M3, M4).
  - Kartu ringkasan Pemasukan dan Surplus Bersih.
  - Kalender aktivitas dengan penanda transaksi.

---

## 📋 Milestone Lanjutan (To-Do / Progress Belum Terselesaikan)

Berikut adalah milestone fitur tambahan untuk penyempurnaan aplikasi di tahap berikutnya:

### 🟡 Milestone 2: Background Notifications & Smart Reminders
- [ ] **Daily Expense Reminder**: Menggunakan Android `WorkManager` untuk menjadwalkan notifikasi harian pukul 20.00 WIB guna mengingatkan pengguna mencatat transaksi atau memindai struk hari itu.
- [ ] **Budget Limit Alert**: Notifikasi peringatan otomatis ketika total pengeluaran bulanan telah mencapai >= 80% dari batas anggaran yang ditentukan.

### 🟡 Milestone 3: Real PDF & CSV Engine Generation
- [ ] **Generasi Dokumen PDF Fisik**: Mengimplementasikan `android.graphics.pdf.PdfDocument` untuk merender laporan keuangan lengkap berformat PDF tabel bertanda tangan digital.
- [ ] **File Export & Sharing**: Menyimpan file CSV/PDF ke direktori `Downloads` publik atau membagikannya ke aplikasi lain (WhatsApp, Gmail, Drive) via `FileProvider` dan `Intent.ACTION_SEND`.

### 🟡 Milestone 4: Custom Avatar Profil & Foto Asli Pengguna
- [ ] **Upload Foto Profil**: Fitur pemilihan foto profil dari galeri atau kamera pada dialog *Edit Profil* (`Edit Profil.png`).
- [ ] **Penyimpanan Gambar Profil Lokal**: Kompresi dan penyimpanan foto profil ke direktori internal aplikasi dengan persistensi URI pada `UserEntity`.

### 🟡 Milestone 5: Keamanan Biometrik (Fingerprint & Face Unlock)
- [ ] **AndroidX Biometric Integration**: Mengintegrasikan `androidx.biometric:biometric` untuk login cepat menggunakan sidik jari atau pemindai wajah saat membuka aplikasi jika sesi masih aktif.

### 🟡 Milestone 6: Dark Mode Parity & Unit / UI Testing
- [ ] **Tema Gelap Konsisten**: Penyesuaian seluruh warna background dan teks pada file `res/values-night/themes.xml` dan drawable agar mendukung mode gelap secara penuh sesuai estetika Finora.
- [ ] **Unit Test Room & OCR Parser**: Pengujian otomatis untuk parsing berbagai variasi format struk belanja (Indomaret, Alfamart, SPBU, Cafe) menggunakan JUnit.
