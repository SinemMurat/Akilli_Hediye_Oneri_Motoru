package com.hediye.datastructures;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Kuyruk - FIFO yapısı.
 * Filtre isteklerini sırayla işlemek için kullanılır.
 */
public class FiltreKuyrugu<T> {

    private static class KuyrukDugum<T> {
        T veri;
        KuyrukDugum<T> sonraki;

        KuyrukDugum(T veri) {
            this.veri = veri;
            this.sonraki = null;
        }
    }

    private KuyrukDugum<T> on;   // çıkış tarafı
    private KuyrukDugum<T> arka; // giriş tarafı
    private int boyut;

    public FiltreKuyrugu() {
        this.on = null;
        this.arka = null;
        this.boyut = 0;
    }

    /** Kuyruğa ekler- O(1) */
    public void ekle(T veri) {
        KuyrukDugum<T> yeni = new KuyrukDugum<>(veri);
        if (arka != null) arka.sonraki = yeni;
        arka = yeni;
        if (on == null) on = yeni;
        boyut++;
    }

    /** Kuyruktan çıkarır - O(1) */
    public T cek() {
        if (bosmu()) throw new RuntimeException("Kuyruk boş!");
        T veri = on.veri;
        on = on.sonraki;
        if (on == null) arka = null;
        boyut--;
        return veri;
    }

    /** Öne bakar - O(1) */
    public T one_bak() {
        if (bosmu()) return null;
        return on.veri;
    }

    public boolean bosmu() { return on == null; }
    public int getBoyut() { return boyut; }
}
