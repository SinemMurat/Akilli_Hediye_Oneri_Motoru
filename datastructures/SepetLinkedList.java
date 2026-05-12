package com.hediye.datastructures;

import com.hediye.model.SepetUrun;
import java.util.ArrayList;
import java.util.List;

/**
 * Bağlı Liste (Linked List) - Alışveriş sepeti için dinamik ekleme/silme sağlar.
 * Sepete ürün ekleme ve çıkarma O(1) / O(n) işlemidir.
 */
public class SepetLinkedList {

    private static class Dugum {
        SepetUrun veri;
        Dugum sonraki;

        Dugum(SepetUrun veri) {
            this.veri = veri;
            this.sonraki = null;
        }
    }

    private Dugum bas;
    private int boyut;

    public SepetLinkedList() {
        this.bas = null;
        this.boyut = 0;
    }

    /** Başa ekler - O(1) */
    public void basaEkle(SepetUrun urun) {
        Dugum yeni = new Dugum(urun);
        yeni.sonraki = bas;
        bas = yeni;
        boyut++;
    }

    /** Sona ekler - O(n) */
    public void sonaEkle(SepetUrun urun) {
        Dugum yeni = new Dugum(urun);
        if (bas == null) {
            bas = yeni;
        } else {
            Dugum mevcut = bas;
            while (mevcut.sonraki != null) mevcut = mevcut.sonraki;
            mevcut.sonraki = yeni;
        }
        boyut++;
    }

    /** Ürün ID'sine göre siler - O(n) */
    public boolean sil(int urunId) {
        if (bas == null) return false;
        if (bas.veri.getUrun().getId() == urunId) {
            bas = bas.sonraki;
            boyut--;
            return true;
        }
        Dugum mevcut = bas;
        while (mevcut.sonraki != null) {
            if (mevcut.sonraki.veri.getUrun().getId() == urunId) {
                mevcut.sonraki = mevcut.sonraki.sonraki;
                boyut--;
                return true;
            }
            mevcut = mevcut.sonraki;
        }
        return false;
    }

    /** Tüm sepeti temizler */
    public void temizle() {
        bas = null;
        boyut = 0;
    }

    /** Toplam fiyatı hesaplar - O(n) */
    public double toplamFiyat() {
        double toplam = 0;
        Dugum mevcut = bas;
        while (mevcut != null) {
            toplam += mevcut.veri.getUrun().getFiyat();
            mevcut = mevcut.sonraki;
        }
        return toplam;
    }

    /** Listeye dönüştürür */
    public List<SepetUrun> listeOlarakGetir() {
        List<SepetUrun> liste = new ArrayList<>();
        Dugum mevcut = bas;
        while (mevcut != null) {
            liste.add(mevcut.veri);
            mevcut = mevcut.sonraki;
        }
        return liste;
    }

    /** Ürün sepette olduğunun kontrolü yapılır */
    public boolean iceriyor(int urunId) {
        Dugum mevcut = bas;
        while (mevcut != null) {
            if (mevcut.veri.getUrun().getId() == urunId) return true;
            mevcut = mevcut.sonraki;
        }
        return false;
    }

    /** Belirli indeksteki elemanı getirir - O(n) */
    public SepetUrun getir(int index) {
        if (index < 0 || index >= boyut) return null;
        Dugum mevcut = bas;
        for (int i = 0; i < index; i++) mevcut = mevcut.sonraki;
        return mevcut.veri;
    }

    public int getBoyut() { return boyut; }
    public boolean bosmu() { return boyut == 0; }
}
