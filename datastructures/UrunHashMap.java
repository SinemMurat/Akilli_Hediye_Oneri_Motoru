package com.hediye.datastructures;

import com.hediye.model.Urun;
import java.util.*;

/**
 * Hash Tablo / Hash Map - Kategori ve marka bazlı O(1) erişim.
 * Anahtar: Kategori veya Marka adı → Değer: Ürün listesi
 */
public class UrunHashMap {

    private static final int VARSAYILAN_KAPASITE = 64;
    private static final double YUK_FAKTORU = 0.75;

    private static class Giris {
        String anahtar;
        List<Urun> deger;
        Giris sonraki;

        Giris(String anahtar, List<Urun> deger) {
            this.anahtar = anahtar;
            this.deger = deger;
            this.sonraki = null;
        }
    }

    private Giris[] tablo;
    private int boyut;
    private int kapasite;

    public UrunHashMap() {
        this.kapasite = VARSAYILAN_KAPASITE;
        this.tablo = new Giris[kapasite];
        this.boyut = 0;
    }

    private int hashFonksiyonu(String anahtar) {
        int hash = 0;
        for (char c : anahtar.toCharArray()) {
            hash = (hash * 31 + c) % kapasite;
        }
        return Math.abs(hash) % kapasite;
    }

    /** Ürünü ilgili anahtara (kategori/marka) ekler */
    public void ekle(String anahtar, Urun urun) {
        int idx = hashFonksiyonu(anahtar);
        Giris mevcut = tablo[idx];
        while (mevcut != null) {
            if (mevcut.anahtar.equals(anahtar)) {
                mevcut.deger.add(urun);
                return;
            }
            mevcut = mevcut.sonraki;
        }
        // Yeni giriş oluşturur
        Giris yeni = new Giris(anahtar, new ArrayList<>());
        yeni.deger.add(urun);
        yeni.sonraki = tablo[idx];
        tablo[idx] = yeni;
        boyut++;

        // Yük faktörü aşıldıysa yeniden boyutlandırır
        if ((double) boyut / kapasite > YUK_FAKTORU) {
            yenidenBoyutlandir();
        }
    }

    /** Anahtara göre ürün listesi döndürür - O(1) ortalama */
    public List<Urun> getir(String anahtar) {
        if (anahtar == null) return new ArrayList<>();
        int idx = hashFonksiyonu(anahtar);
        Giris mevcut = tablo[idx];
        while (mevcut != null) {
            if (mevcut.anahtar.equals(anahtar)) return mevcut.deger;
            mevcut = mevcut.sonraki;
        }
        return new ArrayList<>();
    }

    /** Anahtar kontorlü yapılır */
    public boolean iceriyor(String anahtar) {
        return !getir(anahtar).isEmpty();
    }

    /** Tüm anahtarları döndürür */
    public Set<String> anahtarlar() {
        Set<String> set = new LinkedHashSet<>();
        for (Giris giris : tablo) {
            Giris mevcut = giris;
            while (mevcut != null) {
                set.add(mevcut.anahtar);
                mevcut = mevcut.sonraki;
            }
        }
        return set;
    }

    /** Birden fazla kategorideki ürünleri birleştirir */
    public List<Urun> cokluGetir(List<String> anahtarListesi) {
        List<Urun> sonuc = new ArrayList<>();
        Set<Integer> eklenenIdler = new HashSet<>();
        for (String anahtar : anahtarListesi) {
            for (Urun u : getir(anahtar)) {
                if (eklenenIdler.add(u.getId())) {
                    sonuc.add(u);
                }
            }
        }
        return sonuc;
    }

    private void yenidenBoyutlandir() {
        int yeniKapasite = kapasite * 2;
        Giris[] yeniTablo = new Giris[yeniKapasite];
        int eskiKapasite = kapasite;
        kapasite = yeniKapasite;
        for (int i = 0; i < eskiKapasite; i++) {
            Giris mevcut = tablo[i];
            while (mevcut != null) {
                int yeniIdx = hashFonksiyonu(mevcut.anahtar);
                Giris kopya = new Giris(mevcut.anahtar, mevcut.deger);
                kopya.sonraki = yeniTablo[yeniIdx];
                yeniTablo[yeniIdx] = kopya;
                mevcut = mevcut.sonraki;
            }
        }
        tablo = yeniTablo;
    }

    public int getBoyut() { return boyut; }
}
