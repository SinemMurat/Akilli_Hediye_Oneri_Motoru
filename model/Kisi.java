package com.hediye.model;

import java.util.ArrayList;
import java.util.List;

public class Kisi {
    private int id;
    private int kullaniciId;
    private String ad;
    private String yasGrubu;
    private String ilgiAlanlari;// virgülle ayrılmış
    private String renk;
    private List<String> kaydedilenHediyeler; // hediye ürün adları

    public Kisi(int id, int kullaniciId, String ad, String yasGrubu,
                String ilgiAlanlari, String renk) {
        this.id = id;
        this.kullaniciId = kullaniciId;
        this.ad = ad;
        this.yasGrubu = yasGrubu;
        this.ilgiAlanlari = ilgiAlanlari;
        this.renk = renk;
        this.kaydedilenHediyeler = new ArrayList<>();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getKullaniciId() { return kullaniciId; }
    public String getAd() { return ad; }
    public void setAd(String ad) { this.ad = ad; }
    public String getYasGrubu() { return yasGrubu; }
    public void setYasGrubu(String yasGrubu) { this.yasGrubu = yasGrubu; }
    public String getIlgiAlanlari() { return ilgiAlanlari; }
    public void setIlgiAlanlari(String ilgiAlanlari) { this.ilgiAlanlari = ilgiAlanlari; }
    public String getRenk() { return renk; }
    public void setRenk(String renk) { this.renk = renk; }
    public List<String> getKaydedilenHediyeler() { return kaydedilenHediyeler; }
    public void setKaydedilenHediyeler(List<String> list) { this.kaydedilenHediyeler = list; }

    @Override
    public String toString() { return ad; }
}
