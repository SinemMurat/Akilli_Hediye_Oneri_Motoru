package com.hediye.database;

import com.hediye.model.Kisi;
import com.hediye.model.TakvimEtiketi;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Veritabanı Yöneticisi
 */
public class VeritabaniYoneticisi {

    // Yerel veritabanı bağlantı yolu
    private static final String SQLITE_URL = "jdbc:sqlite:hediye_sistemi.db";

    private static VeritabaniYoneticisi ornek;
    private Connection baglanti;

    private VeritabaniYoneticisi() {
        baglan();
        tablolariOlustur();
    }

    public static VeritabaniYoneticisi getOrnek() {
        if (ornek == null) ornek = new VeritabaniYoneticisi();
        return ornek;
    }

    private void baglan() {
        try {
            baglanti = DriverManager.getConnection(SQLITE_URL);
            try (Statement s = baglanti.createStatement()) {
                s.execute("PRAGMA journal_mode=WAL;");
                s.execute("PRAGMA foreign_keys = ON;");
            }
            System.out.println("SQLite veritabanı bağlantısı başarılı.");
        } catch (SQLException e) {
            throw new RuntimeException("Veritabanı bağlantısı kurulamadı: " + e.getMessage());
        }
    }

    private void tablolariOlustur() {
        String[] tablolar = {
                """
            CREATE TABLE IF NOT EXISTS kullanici (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                kullanici_adi TEXT UNIQUE NOT NULL,
                sifre TEXT NOT NULL,
                ad TEXT NOT NULL
            )""",
                """
            CREATE TABLE IF NOT EXISTS kisi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                kullanici_id INTEGER NOT NULL,
                ad TEXT NOT NULL,
                yas_grubu TEXT DEFAULT '',
                ilgi_alanlari TEXT DEFAULT '',
                renk TEXT DEFAULT '#4A90E2',
                FOREIGN KEY(kullanici_id) REFERENCES kullanici(id)
            )""",
                """
            CREATE TABLE IF NOT EXISTS takvim_etiketi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                kullanici_id INTEGER NOT NULL,
                tarih TEXT NOT NULL,
                kisi_id INTEGER NOT NULL,
                kisi_adi TEXT NOT NULL,
                kisi_renk TEXT NOT NULL,
                metin TEXT DEFAULT '',
                tamamlandi INTEGER DEFAULT 0,
                FOREIGN KEY(kullanici_id) REFERENCES kullanici(id)
            )""",
                """
            CREATE TABLE IF NOT EXISTS kayitli_hediye (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                kullanici_id INTEGER NOT NULL,
                kisi_id INTEGER NOT NULL,
                urun_id INTEGER NOT NULL,
                urun_adi TEXT NOT NULL,
                urun_fiyat REAL NOT NULL,
                urun_kategori TEXT NOT NULL,
                tamamlandi INTEGER DEFAULT 0,
                FOREIGN KEY(kullanici_id) REFERENCES kullanici(id),
                FOREIGN KEY(kisi_id) REFERENCES kisi(id)
            )""",
                """
            CREATE TABLE IF NOT EXISTS urun_puan (
                urun_id INTEGER PRIMARY KEY,
                populerlik_puan REAL NOT NULL
            )"""
        };

        try (Statement stmt = baglanti.createStatement()) {
            for (String sql : tablolar) {
                stmt.execute(sql);
            }
        } catch (SQLException e) {
            System.err.println("Tablo oluşturma hatası: " + e.getMessage());
        }
    }

    // KULLANICI İŞLEMLERİ

    public boolean kullaniciKaydet(String kullaniciAdi, String sifre, String ad) {
        String sql = "INSERT INTO kullanici(kullanici_adi, sifre, ad) VALUES(?,?,?)";
        try (PreparedStatement ps = baglanti.prepareStatement(sql)) {
            ps.setString(1, kullaniciAdi);
            ps.setString(2, sifre);
            ps.setString(3, ad);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public int[] girisYap(String kullaniciAdi, String sifre) {
        String sql = "SELECT id FROM kullanici WHERE kullanici_adi=? AND sifre=?";
        try (PreparedStatement ps = baglanti.prepareStatement(sql)) {
            ps.setString(1, kullaniciAdi);
            ps.setString(2, sifre);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return new int[]{rs.getInt("id")};
        } catch (SQLException e) { e.printStackTrace(); }
        return new int[]{-1};
    }

    public String kullaniciAdiGetir(int kullaniciId) {
        try (PreparedStatement ps = baglanti.prepareStatement("SELECT ad FROM kullanici WHERE id=?")) {
            ps.setInt(1, kullaniciId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getString("ad");
        } catch (SQLException e) { e.printStackTrace(); }
        return "Kullanıcı";
    }

    // KİŞİ İŞLEMLERİ

    public int kisiEkle(int kullaniciId, String ad, String yasGrubu, String ilgiAlanlari, String renk) {
        String sql = "INSERT INTO kisi(kullanici_id, ad, yas_grubu, ilgi_alanlari, renk) VALUES(?,?,?,?,?)";
        try (PreparedStatement ps = baglanti.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, kullaniciId);
            ps.setString(2, ad);
            ps.setString(3, yasGrubu);
            ps.setString(4, ilgiAlanlari);
            ps.setString(5, renk);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public List<Kisi> kisileriGetir(int kullaniciId) {
        List<Kisi> liste = new ArrayList<>();
        String sql = "SELECT * FROM kisi WHERE kullanici_id=? ORDER BY ad";
        try (PreparedStatement ps = baglanti.prepareStatement(sql)) {
            ps.setInt(1, kullaniciId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new Kisi(
                        rs.getInt("id"), kullaniciId,
                        rs.getString("ad"), rs.getString("yas_grubu"),
                        rs.getString("ilgi_alanlari"), rs.getString("renk")
                ));
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public void kisiSil(int kisiId) {
        try {
            // Önce kişiye ait takvim etiketlerini ve kayıtlı hediyeleri siler
            try (PreparedStatement ps = baglanti.prepareStatement("DELETE FROM takvim_etiketi WHERE kisi_id=?")) {
                ps.setInt(1, kisiId);
                ps.executeUpdate();
            }
            try (PreparedStatement ps = baglanti.prepareStatement("DELETE FROM kayitli_hediye WHERE kisi_id=?")) {
                ps.setInt(1, kisiId);
                ps.executeUpdate();
            }
            // Sonra da kişiyi siler
            try (PreparedStatement ps = baglanti.prepareStatement("DELETE FROM kisi WHERE id=?")) {
                ps.setInt(1, kisiId);
                ps.executeUpdate();
            }
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void kisiIlgiAlanlariGuncelle(int kisiId, String yeniIlgiAlanlari) {
        String sql = "UPDATE kisi SET ilgi_alanlari=? WHERE id=?";
        try (PreparedStatement ps = baglanti.prepareStatement(sql)) {
            ps.setString(1, yeniIlgiAlanlari);
            ps.setInt(2, kisiId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void kisiBilgileriniGuncelle(int kisiId, String yeniYas, String yeniIlgi) {
        String sql = "UPDATE kisi SET yas_grubu=?, ilgi_alanlari=? WHERE id=?";
        try (PreparedStatement ps = baglanti.prepareStatement(sql)) {
            ps.setString(1, yeniYas);
            ps.setString(2, yeniIlgi);
            ps.setInt(3, kisiId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }
    //TAKVİM ETİKET İŞLEMLERİ

    public int etiketEkle(int kullaniciId, LocalDate tarih, int kisiId,
                          String kisiAdi, String kisiRenk, String metin) {
        String sql = "INSERT INTO takvim_etiketi(kullanici_id,tarih,kisi_id,kisi_adi,kisi_renk,metin) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = baglanti.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, kullaniciId);
            ps.setString(2, tarih.toString());
            ps.setInt(3, kisiId);
            ps.setString(4, kisiAdi);
            ps.setString(5, kisiRenk);
            ps.setString(6, metin);
            ps.executeUpdate();
            ResultSet rs = ps.getGeneratedKeys();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return -1;
    }

    public void etiketGuncelle(int etiketId, String metin) {
        try (PreparedStatement ps = baglanti.prepareStatement("UPDATE takvim_etiketi SET metin=? WHERE id=?")) {
            ps.setString(1, metin);
            ps.setInt(2, etiketId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void etiketSil(int etiketId) {
        try (PreparedStatement ps = baglanti.prepareStatement("DELETE FROM takvim_etiketi WHERE id=?")) {
            ps.setInt(1, etiketId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<TakvimEtiketi> etiketleriGetir(int kullaniciId) {
        List<TakvimEtiketi> liste = new ArrayList<>();
        String sql = "SELECT * FROM takvim_etiketi WHERE kullanici_id=?";
        try (PreparedStatement ps = baglanti.prepareStatement(sql)) {
            ps.setInt(1, kullaniciId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TakvimEtiketi e = new TakvimEtiketi(
                        rs.getInt("id"), kullaniciId,
                        LocalDate.parse(rs.getString("tarih")),
                        rs.getInt("kisi_id"), rs.getString("kisi_adi"),
                        rs.getString("kisi_renk"), rs.getString("metin")
                );
                e.setTamamlandi(rs.getInt("tamamlandi") == 1);
                liste.add(e);
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    //KAYITLI HEDİYE İŞLEMLERİ

    public void hediyeKaydet(int kullaniciId, int kisiId, int urunId, String urunAdi, double fiyat, String kategori) {
        String sql = "INSERT INTO kayitli_hediye(kullanici_id,kisi_id,urun_id,urun_adi,urun_fiyat,urun_kategori) VALUES(?,?,?,?,?,?)";
        try (PreparedStatement ps = baglanti.prepareStatement(sql)) {
            ps.setInt(1, kullaniciId);
            ps.setInt(2, kisiId);
            ps.setInt(3, urunId);
            ps.setString(4, urunAdi);
            ps.setDouble(5, fiyat);
            ps.setString(6, kategori);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public List<String[]> hediyeleriGetirDetay(int kisiId) {
        List<String[]> liste = new ArrayList<>();
        String sql = "SELECT id, urun_adi, urun_fiyat, urun_kategori, tamamlandi FROM kayitli_hediye WHERE kisi_id=?";
        try (PreparedStatement ps = baglanti.prepareStatement(sql)) {
            ps.setInt(1, kisiId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                liste.add(new String[]{
                        String.valueOf(rs.getInt("id")), rs.getString("urun_adi"),
                        String.valueOf(rs.getDouble("urun_fiyat")), rs.getString("urun_kategori"),
                        String.valueOf(rs.getInt("tamamlandi"))
                });
            }
        } catch (SQLException e) { e.printStackTrace(); }
        return liste;
    }

    public void hediyeleriTamamlandiGuncelle(int hediyeId, boolean tamamlandi) {
        try (PreparedStatement ps = baglanti.prepareStatement("UPDATE kayitli_hediye SET tamamlandi=? WHERE id=?")) {
            ps.setInt(1, tamamlandi ? 1 : 0);
            ps.setInt(2, hediyeId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public void hediyeSil(int hediyeId) {
        try (PreparedStatement ps = baglanti.prepareStatement("DELETE FROM kayitli_hediye WHERE id=?")) {
            ps.setInt(1, hediyeId);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    //ÜRÜN PUAN İŞLEMLERİ

    public void puanKaydet(int urunId, double puan) {
        String sql = "INSERT OR REPLACE INTO urun_puan(urun_id, populerlik_puan) VALUES(?,?)";
        try (PreparedStatement ps = baglanti.prepareStatement(sql)) {
            ps.setInt(1, urunId);
            ps.setDouble(2, puan);
            ps.executeUpdate();
        } catch (SQLException e) { e.printStackTrace(); }
    }

    public double puanGetir(int urunId, double varsayilan) {
        try (PreparedStatement ps = baglanti.prepareStatement("SELECT populerlik_puan FROM urun_puan WHERE urun_id=?")) {
            ps.setInt(1, urunId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getDouble("populerlik_puan");
        } catch (SQLException e) { tablolariOlustur(); }
        return varsayilan;
    }

    public void baglantiKapat() {
        try { if (baglanti != null && !baglanti.isClosed()) baglanti.close(); }
        catch (SQLException e) { e.printStackTrace(); }
    }
}