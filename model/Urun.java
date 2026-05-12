package com.hediye.model;

public class Urun implements Comparable<Urun> {
    private int id;
    private String urunAdi;
    private String kategori;
    private String ilgiAlani;
    private String marka;
    private double fiyat;
    private String yasGrubu;
    private double populerlikPuan;

    public Urun(int id, String urunAdi, String kategori, String ilgiAlani,
                String marka, double fiyat, String yasGrubu, double populerlikPuan) {
        this.id = id;
        this.urunAdi = urunAdi;
        this.kategori = kategori;
        this.ilgiAlani = ilgiAlani;
        this.marka = marka;
        this.fiyat = fiyat;
        this.yasGrubu = yasGrubu;
        this.populerlikPuan = populerlikPuan;
    }

    @Override
    public int compareTo(Urun other) {
        return Double.compare(this.fiyat, other.fiyat);
    }

    public int getId() { return id; }
    public String getUrunAdi() { return urunAdi; }
    public String getKategori() { return kategori; }
    public String getIlgiAlani() { return ilgiAlani; }
    public String getMarka() { return marka; }
    public double getFiyat() { return fiyat; }
    public String getYasGrubu() { return yasGrubu; }
    public double getPopulerlikPuan() { return populerlikPuan; }
    public void setPopulerlikPuan(double puan) { this.populerlikPuan = Math.max(0, Math.min(100, puan)); }

    @Override
    public String toString() {
        return String.format("%s | %s | %.2f TL | Puan: %.1f", urunAdi, marka, fiyat, populerlikPuan);
    }
}
