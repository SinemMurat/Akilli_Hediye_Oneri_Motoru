package com.hediye.datastructures;

import com.hediye.model.Urun;
import java.util.ArrayList;
import java.util.List;

/**
 * Yığın (Stack) - LIFO yapısı.
 * Navigasyon geçmişi ve geri alma işlemleri için kullanılır.
 */
public class IslemStack<T> {

    private static class YiginDugum<T> {
        T veri;
        YiginDugum<T> altindaki;

        YiginDugum(T veri) {
            this.veri = veri;
            this.altindaki = null;
        }
    }

    private YiginDugum<T> tepe;
    private int boyut;
    private final int maxBoyut;

    public IslemStack(int maxBoyut) {
        this.tepe = null;
        this.boyut = 0;
        this.maxBoyut = maxBoyut;
    }

    /** Yığına iter- O(1) */
    public void it(T veri) {
        if (boyut >= maxBoyut) {
            // En alttaki elemanı çıkararak yer açar
            altinciKaldir();
        }
        YiginDugum<T> yeni = new YiginDugum<>(veri);
        yeni.altindaki = tepe;
        tepe = yeni;
        boyut++;
    }

    /** Tepeden çıkarır- O(1) */
    public T cek() {
        if (bosmu()) throw new RuntimeException("Yığın boş!");
        T veri = tepe.veri;
        tepe = tepe.altindaki;
        boyut--;
        return veri;
    }

    /** Tepeye bakar - O(1) */
    public T tepeye_bak() {
        if (bosmu()) return null;
        return tepe.veri;
    }

    /** En alttaki elemanı kaldırır */
    private void altinciKaldir() {
        if (tepe == null || tepe.altindaki == null) {
            tepe = null;
            boyut = 0;
            return;
        }
        YiginDugum<T> mevcut = tepe;
        while (mevcut.altindaki.altindaki != null) {
            mevcut = mevcut.altindaki;
        }
        mevcut.altindaki = null;
        boyut--;
    }

    public boolean bosmu() { return tepe == null; }
    public int getBoyut() { return boyut; }

    public List<T> listeOlarakGetir() {
        List<T> liste = new ArrayList<>();
        YiginDugum<T> mevcut = tepe;
        while (mevcut != null) {
            liste.add(mevcut.veri);
            mevcut = mevcut.altindaki;
        }
        return liste;
    }
}
