package com.hediye.datastructures;

import com.hediye.model.Urun;
import java.util.ArrayList;
import java.util.List;

/**
 * İkili Arama Ağacı (BST) - Ürünleri fiyata göre saklar.
 * Fiyat aralığı sorguları O(log N) hızında çalışır.
 */
public class UrunBST {

    private static class BSTDugum {
        Urun urun;
        BSTDugum sol, sag;

        BSTDugum(Urun urun) {
            this.urun = urun;
            this.sol = null;
            this.sag = null;
        }
    }

    private BSTDugum kok;
    private int boyut;

    public UrunBST() {
        this.kok = null;
        this.boyut = 0;
    }

    /** Ürünü BST'ye ekler (fiyat anahtarı) */
    public void ekle(Urun urun) {
        kok = ekleYardimci(kok, urun);
        boyut++;
    }

    private BSTDugum ekleYardimci(BSTDugum dugum, Urun urun) {
        if (dugum == null) return new BSTDugum(urun);
        if (urun.getFiyat() <= dugum.urun.getFiyat()) {
            dugum.sol = ekleYardimci(dugum.sol, urun);
        } else {
            dugum.sag = ekleYardimci(dugum.sag, urun);
        }
        return dugum;
    }

    /** Belirtilen fiyat aralığındaki ürünleri döndürür */
    public List<Urun> aralikSorgula(double minFiyat, double maxFiyat) {
        List<Urun> sonuc = new ArrayList<>();
        aralikSorgulaYardimci(kok, minFiyat, maxFiyat, sonuc);
        return sonuc;
    }

    private void aralikSorgulaYardimci(BSTDugum dugum, double min, double max, List<Urun> sonuc) {
        if (dugum == null) return;
        if (dugum.urun.getFiyat() >= min && dugum.urun.getFiyat() <= max) {
            sonuc.add(dugum.urun);
            aralikSorgulaYardimci(dugum.sol, min, max, sonuc);
            aralikSorgulaYardimci(dugum.sag, min, max, sonuc);
        } else if (dugum.urun.getFiyat() < min) {
            aralikSorgulaYardimci(dugum.sag, min, max, sonuc);
        } else {
            aralikSorgulaYardimci(dugum.sol, min, max, sonuc);
        }
    }

    /** Tüm ürünleri sıralı döndürür */
    public List<Urun> siraliGetir() {
        List<Urun> liste = new ArrayList<>();
        inOrder(kok, liste);
        return liste;
    }

    private void inOrder(BSTDugum dugum, List<Urun> liste) {
        if (dugum == null) return;
        inOrder(dugum.sol, liste);
        liste.add(dugum.urun);
        inOrder(dugum.sag, liste);
    }

    public int getBoyut() { return boyut; }

    /** Belirli bir ürünün popülerlik puanını günceller */
    public void puanGuncelle(int urunId, double yeniPuan) {
        puanGuncelleYardimci(kok, urunId, yeniPuan);
    }

    private void puanGuncelleYardimci(BSTDugum dugum, int urunId, double yeniPuan) {
        if (dugum == null) return;
        if (dugum.urun.getId() == urunId) {
            dugum.urun.setPopulerlikPuan(yeniPuan);
            return;
        }
        puanGuncelleYardimci(dugum.sol, urunId, yeniPuan);
        puanGuncelleYardimci(dugum.sag, urunId, yeniPuan);
    }
}
