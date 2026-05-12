package com.hediye.model;

import java.time.LocalDate;

public class TakvimEtiketi {
    private int id;
    private int kullaniciId;
    private LocalDate tarih;
    private int kisiId;
    private String kisiAdi;
    private String kisiRenk;
    private String metin;
    private boolean tamamlandi;

    public TakvimEtiketi(int id, int kullaniciId, LocalDate tarih,
                          int kisiId, String kisiAdi, String kisiRenk, String metin) {
        this.id = id;
        this.kullaniciId = kullaniciId;
        this.tarih = tarih;
        this.kisiId = kisiId;
        this.kisiAdi = kisiAdi;
        this.kisiRenk = kisiRenk;
        this.metin = metin;
        this.tamamlandi = false;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getKullaniciId() { return kullaniciId; }
    public LocalDate getTarih() { return tarih; }
    public int getKisiId() { return kisiId; }
    public String getKisiAdi() { return kisiAdi; }
    public String getKisiRenk() { return kisiRenk; }
    public String getMetin() { return metin; }
    public void setMetin(String metin) { this.metin = metin; }
    public boolean isTamamlandi() { return tamamlandi; }
    public void setTamamlandi(boolean tamamlandi) { this.tamamlandi = tamamlandi; }
}
