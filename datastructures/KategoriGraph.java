package com.hediye.datastructures;

import java.util.*;

/**
 * Graf (Graph) - Kategoriler arası ilişkileri modelleyen komşuluk listesi.
 * BFS ile benzer kategorideki yan ürünler önerilir.
 */
public class KategoriGraph {

    private final Map<String, List<String>> komsulukListesi;

    public KategoriGraph() {
        komsulukListesi = new LinkedHashMap<>();
        grafKur();
    }

    /** Kategori ilişkilerini tanımlar */
    private void grafKur() {
        // Tüm kategorileri ekler
        String[] kategoriler = {
            "Teknoloji", "Moda", "Muzik", "Spor", "Kitap",
            "Oyun", "Ev-Yasam", "Kozmetik", "Hobi-Sanat", "Otomobil-Aksesuar", "Oyuncak"
        };
        for (String k : kategoriler) komsulukListesi.put(k, new ArrayList<>());

        // İlişkiler (çift yönlü kenarlar)
        kenarEkle("Teknoloji", "Oyun");
        kenarEkle("Teknoloji", "Muzik");
        kenarEkle("Teknoloji", "Otomobil-Aksesuar");
        kenarEkle("Moda", "Kozmetik");
        kenarEkle("Ev-Yasam", "Hobi-Sanat");
        kenarEkle("Kitap", "Hobi-Sanat");
        kenarEkle("Oyun", "Oyuncak");
    }

    private void kenarEkle(String a, String b) {
        komsulukListesi.computeIfAbsent(a, k -> new ArrayList<>()).add(b);
        komsulukListesi.computeIfAbsent(b, k -> new ArrayList<>()).add(a);
    }

    /** BFS ile verilen kategorilere komşu olan ilgili kategorileri bulur */
    public List<String> ilgiliKategorileriBul(List<String> baslangicKategoriler, int derinlik) {
        Set<String> ziyaretEdilen = new LinkedHashSet<>(baslangicKategoriler);
        Queue<String> kuyruk = new LinkedList<>(baslangicKategoriler);
        List<String> ilgiliKategoriler = new ArrayList<>();
        int adim = 0;

        while (!kuyruk.isEmpty() && adim < derinlik) {
            int seviyeBoyutu = kuyruk.size();
            for (int i = 0; i < seviyeBoyutu; i++) {
                String mevcut = kuyruk.poll();
                List<String> komsular = komsulukListesi.getOrDefault(mevcut, new ArrayList<>());
                for (String komsu : komsular) {
                    if (!ziyaretEdilen.contains(komsu)) {
                        ziyaretEdilen.add(komsu);
                        kuyruk.add(komsu);
                        if (!baslangicKategoriler.contains(komsu)) {
                            ilgiliKategoriler.add(komsu);
                        }
                    }
                }
            }
            adim++;
        }
        return ilgiliKategoriler;
    }

    /** DFS ile derinlemesine ilgili kategoriler */
    public List<String> dfsIlgiliKategoriler(String baslangic) {
        List<String> ziyaretEdilen = new ArrayList<>();
        Set<String> ziyaretSet = new HashSet<>();
        dfs(baslangic, ziyaretSet, ziyaretEdilen);
        ziyaretEdilen.remove(baslangic);
        return ziyaretEdilen;
    }

    private void dfs(String dugum, Set<String> ziyaretSet, List<String> sonuc) {
        ziyaretSet.add(dugum);
        sonuc.add(dugum);
        for (String komsu : komsulukListesi.getOrDefault(dugum, new ArrayList<>())) {
            if (!ziyaretSet.contains(komsu)) dfs(komsu, ziyaretSet, sonuc);
        }
    }

    /** Kategorinin doğrudan komşularını döndürür */
    public List<String> dogrudanKomsular(String kategori) {
        return komsulukListesi.getOrDefault(kategori, new ArrayList<>());
    }

    public Map<String, List<String>> getKomsulukListesi() { return komsulukListesi; }
}
