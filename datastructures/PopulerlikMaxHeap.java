package com.hediye.datastructures;

import com.hediye.model.Urun;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * MaxHeap (Öncelikli Kuyruk) - Popülerlik veya fiyata göre sıralama.
 * En yüksek değer her zaman kökte bulunur. O(log n) ekle/çıkar.
 */
public class PopulerlikMaxHeap {

    private final List<Urun> heap;
    private final Comparator<Urun> karsilastirici;

    public PopulerlikMaxHeap(Comparator<Urun> karsilastirici) {
        this.heap = new ArrayList<>();
        this.karsilastirici = karsilastirici;
    }

    /** Popülerliğe göre MaxHeap */
    public static PopulerlikMaxHeap populerlikHeap() {
        return new PopulerlikMaxHeap(
            (a, b) -> Double.compare(b.getPopulerlikPuan(), a.getPopulerlikPuan())
        );
    }

    /** Fiyata göre MaxHeap */
    public static PopulerlikMaxHeap fiyatHeap() {
        return new PopulerlikMaxHeap(
            (a, b) -> Double.compare(b.getFiyat(), a.getFiyat())
        );
    }

    /** Fiyata göre MinHeap (önce en düşük fiyat olacak şekilde) */
    public static PopulerlikMaxHeap fiyatMinHeap() {
        return new PopulerlikMaxHeap(
            (a, b) -> Double.compare(a.getFiyat(), b.getFiyat())
        );
    }

    /** Ürünü heap'e ekler - O(log n) */
    public void ekle(Urun urun) {
        heap.add(urun);
        yukariKaydir(heap.size() - 1);
    }

    /** En öncelikli ürünü çıkarır - O(log n) */
    public Urun cek() {
        if (bosmu()) return null;
        Urun tepe = heap.get(0);
        int sonIdx = heap.size() - 1;
        heap.set(0, heap.get(sonIdx));
        heap.remove(sonIdx);
        if (!bosmu()) asagiKaydir(0);
        return tepe;
    }

    /** En öncelikli ürüne bakar - O(1) */
    public Urun tepe() {
        return bosmu() ? null : heap.get(0);
    }

    /** Tüm ürünleri sıralı döndürür (heap'i boşaltır) */
    public List<Urun> siraliGetir() {
        List<Urun> sonuc = new ArrayList<>();
        PopulerlikMaxHeap kopya = new PopulerlikMaxHeap(karsilastirici);
        kopya.heap.addAll(this.heap);
        // Heap property'yi sağla
        for (int i = kopya.heap.size() / 2 - 1; i >= 0; i--) {
            kopya.asagiKaydir(i);
        }
        while (!kopya.bosmu()) {
            sonuc.add(kopya.cek());
        }
        return sonuc;
    }

    /** Toplu ekleme ve heap oluşturma - O(n) */
    public void topluEkle(List<Urun> urunler) {
        heap.addAll(urunler);
        // Heapify - O(n)
        for (int i = heap.size() / 2 - 1; i >= 0; i--) {
            asagiKaydir(i);
        }
    }

    private void yukariKaydir(int idx) {
        while (idx > 0) {
            int ebeveyn = (idx - 1) / 2;
            if (karsilastirici.compare(heap.get(idx), heap.get(ebeveyn)) < 0) {
                swap(idx, ebeveyn);
                idx = ebeveyn;
            } else break;
        }
    }

    private void asagiKaydir(int idx) {
        int n = heap.size();
        while (true) {
            int en_iyi = idx;
            int sol = 2 * idx + 1;
            int sag = 2 * idx + 2;
            if (sol < n && karsilastirici.compare(heap.get(sol), heap.get(en_iyi)) < 0) en_iyi = sol;
            if (sag < n && karsilastirici.compare(heap.get(sag), heap.get(en_iyi)) < 0) en_iyi = sag;
            if (en_iyi != idx) {
                swap(idx, en_iyi);
                idx = en_iyi;
            } else break;
        }
    }

    private void swap(int i, int j) {
        Urun tmp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, tmp);
    }

    public boolean bosmu() { return heap.isEmpty(); }
    public int getBoyut() { return heap.size(); }
    public void temizle() { heap.clear(); }
}
