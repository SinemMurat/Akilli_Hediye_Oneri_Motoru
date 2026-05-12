package com.hediye.service;

import com.hediye.datastructures.*;
import com.hediye.database.VeritabaniYoneticisi;
import com.hediye.model.Urun;

import java.util.*;

/**
 * Hediye Öneri Servisi - Filtreleme, sıralama ve öneri motoru için en önemli sınıftır.
 * Tüm veri yapılarını koordineli biçimde kullanır.
 */
public class HediyeServisi {

    private static HediyeServisi ornek;

    private final VeriYukleyici veriYukleyici;
    private final KategoriGraph kategoriGraph;
    // Stack: son filtre geçmişi
    private final IslemStack<FiltreParametresi> filtreGecmisi;
    // Kuyruk: filtre isteklerini işleme sırası
    private final FiltreKuyrugu<FiltreParametresi> filtreKuyrugu;

    public static class FiltreParametresi {
        public List<String> seciliKategoriler;
        public String yasGrubu;
        public double maxFiyat;
        public String marka;
        public String siralama;
        public boolean akilliOneri;

        public FiltreParametresi(List<String> kategoriler, String yasGrubu,
                                  double maxFiyat, String marka, String siralama, boolean akilliOneri) {
            this.seciliKategoriler = kategoriler;
            this.yasGrubu = yasGrubu;
            this.maxFiyat = maxFiyat;
            this.marka = marka;
            this.siralama = siralama;
            this.akilliOneri = akilliOneri;
        }
    }

    private HediyeServisi() {
        this.veriYukleyici = VeriYukleyici.getOrnek();
        this.kategoriGraph  = new KategoriGraph();
        this.filtreGecmisi  = new IslemStack<>(20);
        this.filtreKuyrugu  = new FiltreKuyrugu<>();
    }

    public static HediyeServisi getOrnek() {
        if (ornek == null) ornek = new HediyeServisi();
        return ornek;
    }

    /**
     * Ana filtreleme metodu. Tüm veri yapılarını sırayla kullanır:
     * 1. BST  → fiyat aralığı filtresi
     * 2. HashMap → kategori/marka filtresi
     * 3. Graph   → ilgili kategori önerileri
     * 4. Heap/Priority Queue → sıralama
     * 5. Array   → yaş grubu filtresi
     * 6. Stack   → filtre geçmişi
     * 7. Queue   → istek işleme sırası
     */
    public List<Urun> filtrele(FiltreParametresi params) {
        // Kuyruğa ekler
        filtreKuyrugu.ekle(params);
        FiltreParametresi istek = filtreKuyrugu.cek();

        // Geçmişe kaydeder(stack kullanılır)
        filtreGecmisi.it(istek);

        //BST ile fiyat aralığı filtresi
        List<Urun> fiyatFiltreli = veriYukleyici.getUrunBST()
                .aralikSorgula(0, istek.maxFiyat);

        //Kategori + Marka filtresi (HashMap ile O(1) erişim sağlanır)
        Set<Integer> kategoriUrunIdleri = new HashSet<>();

        if (istek.seciliKategoriler == null || istek.seciliKategoriler.isEmpty()) {
            // Tüm ürünler - Array ile index erişimi sağlanır
            for (int i = 0; i < veriYukleyici.getUrunSayisi(); i++) {
                kategoriUrunIdleri.add(veriYukleyici.getTumUrunlerDizi()[i].getId());
            }
        } else {
            // HashMap'ten seçili kategorilerin ürünleri
            for (String kat : istek.seciliKategoriler) {
                for (Urun u : veriYukleyici.getKategoriMap().getir(kat)) {
                    kategoriUrunIdleri.add(u.getId());
                }
            }
            // Graph ile ilgili kategorileri de yan ürün önerisi olarak ekler
            // Eğer Akıllı Öneri seçiliyse yan ürünleri getirir aksi takdirde getirmez.
            if (istek.akilliOneri) {
                List<String> ilgili = kategoriGraph.ilgiliKategorileriBul(istek.seciliKategoriler, 1);
                for (String ilgiliKat : ilgili) {
                    List<Urun> ilgiliUrunler = veriYukleyici.getKategoriMap().getir(ilgiliKat);
                    // İlgili kategoriden sadece en iyi 50 ürünü ekler
                    ilgiliUrunler.stream()
                        .sorted((a, b) -> Double.compare(b.getPopulerlikPuan(), a.getPopulerlikPuan()))
                        .limit(50)
                        .forEach(u -> kategoriUrunIdleri.add(u.getId()));
                }
            }
        }

        //Kesişim - BST + HashMap filtreleri birleştirilir
        List<Urun> araFiltre = new ArrayList<>();
        for (Urun u : fiyatFiltreli) {
            if (!kategoriUrunIdleri.contains(u.getId())) continue;

            // Yaş grubu filtresi(Array index erişimi kullanılır)
            if (istek.yasGrubu != null && !istek.yasGrubu.equals("Tumu")) {
                if (!u.getYasGrubu().equalsIgnoreCase(istek.yasGrubu)) continue;
            }

            // Marka filtresi (HashMap kullanılır)
            if (istek.marka != null && !istek.marka.equals("Tumu") && !istek.marka.isEmpty()) {
                if (!u.getMarka().equalsIgnoreCase(istek.marka)) continue;
            }

            araFiltre.add(u);
        }

        // Sıralama (Heap / Priority Queue kullanılır)
        return sirala(araFiltre, istek.siralama);
    }

    /**
     * Heap tabanlı sıralama
     */
    public List<Urun> sirala(List<Urun> urunler, String siralama) {
        if (urunler.isEmpty()) return urunler;

        PopulerlikMaxHeap heap;
        switch (siralama == null ? "Populerlik" : siralama) {
            case "Fiyat Artan"  -> heap = PopulerlikMaxHeap.fiyatMinHeap();
            case "Fiyat Azalan" -> heap = PopulerlikMaxHeap.fiyatHeap();
            default             -> heap = PopulerlikMaxHeap.populerlikHeap();
        }
        heap.topluEkle(new ArrayList<>(urunler));
        return heap.siraliGetir();
    }

    /**
     * Belirli bir kategoriye göre markaları döndürür.
     * TreeSet üzerinden sıralı ve tekrarsız liste verilir.
     */
    public List<String> markalariGetir(List<String> kategoriler) {
        TreeMap<String, TreeSet<String>> katMarka = veriYukleyici.getKategoriMarkaSeti();
        TreeSet<String> markalar = new TreeSet<>();
        if (kategoriler == null || kategoriler.isEmpty()) {
            katMarka.values().forEach(markalar::addAll);
        } else {
            for (String kat : kategoriler) {
                TreeSet<String> set = katMarka.get(kat);
                if (set != null) markalar.addAll(set);
            }
        }
        List<String> liste = new ArrayList<>();
        liste.add("Tumu");
        liste.addAll(markalar);
        return liste;
    }

    /**
     * Yıldız değerlendirmesine göre popülerlik puanını günceller.
     * 5 yıldız → +%2, 4 yıldız → +%1, 3 yıldız→ değişmez,
     * 2 yıldız → -%1, 1 yıldız → -%2, 0 yıldız → -%3
     */
    public double puanHesapla(double mevcutPuan, int yildiz) {
        double degisim = switch (yildiz) {
            case 5 ->  mevcutPuan * 0.02;
            case 4 ->  mevcutPuan * 0.01;
            case 3 ->  0.0;
            case 2 -> -mevcutPuan * 0.01;
            case 1 -> -mevcutPuan * 0.02;
            case 0 -> -mevcutPuan * 0.03;
            default -> 0.0;
        };
        double yeniPuan = mevcutPuan + degisim;
        return Math.max(0, Math.min(100, yeniPuan));
    }

    /**
     * Ürünün popülerlik puanını günceller ve database'e kaydeder.
     */
    public void puanGuncelle(Urun urun, int yildiz) {
        double yeniPuan = puanHesapla(urun.getPopulerlikPuan(), yildiz);
        urun.setPopulerlikPuan(yeniPuan);
        // BST'de günceller
        veriYukleyici.getUrunBST().puanGuncelle(urun.getId(), yeniPuan);
        // DB'ye kaydeder
        VeritabaniYoneticisi.getOrnek().puanKaydet(urun.getId(), yeniPuan);
    }

    /** Son filtre parametrelerine geri döner (Stack ile geri alma işlemi) */
    public FiltreParametresi oncekiFiltreye() {
        if (!filtreGecmisi.bosmu()) filtreGecmisi.cek();
        return filtreGecmisi.tepeye_bak();
    }

    public KategoriGraph getKategoriGraph() { return kategoriGraph; }

    public List<String> tumKategoriler() {
        return new ArrayList<>(veriYukleyici.getKategoriMarkaSeti().keySet());
    }
}
