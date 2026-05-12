package com.hediye.service;

import com.hediye.database.VeritabaniYoneticisi;
import com.hediye.datastructures.*;
import com.hediye.model.Urun;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Veri Yükleyici - CSV'den ürünleri okur ve tüm veri yapılarına yükler.
 * Dataset dosyası yoksa oluşturur ama halihazırda varsa yeniden oluşturmaz.
 */
public class VeriYukleyici {

    private static final String DOSYA_ADI = "data/akilli_hediyeler_genis_veriseti.csv";
    private static final int HEDEF_SATIR = 10000;

    //Array veri yapısı
    private Urun[] tumUrunlerDizi;
    private int urunSayisi;

    // Fiyata göre BST
    private final UrunBST urunBST;

    // HashMap kategori → ürünler, marka → ürünler
    private final UrunHashMap kategoriMap;
    private final UrunHashMap markaMap;

    // TreeMap - kategorilere göre sıralı marka kümeleri
    private final TreeMap<String, TreeSet<String>> kategoriMarkaSeti;

    // Graf yapısı
    private final KategoriGraph kategoriGraph;

    // Singleton erişim
    private static VeriYukleyici ornek;

    private VeriYukleyici() {
        urunBST = new UrunBST();
        kategoriMap = new UrunHashMap();
        markaMap = new UrunHashMap();
        kategoriMarkaSeti = new TreeMap<>();
        kategoriGraph = new KategoriGraph();
        tumUrunlerDizi = new Urun[HEDEF_SATIR + 10];
        urunSayisi = 0;
    }

    public static VeriYukleyici getOrnek() {
        if (ornek == null) ornek = new VeriYukleyici();
        return ornek;
    }

    /** CSV yoksa üretir ardından yükler */
    public void yukle() {
        veriSetiniOlusturEgerYoksa();
        csvOku();
        System.out.println("Yüklenen ürün sayısı: " + urunSayisi);
    }

    //VERİ SETİ OLUŞTURMA

    private void veriSetiniOlusturEgerYoksa() {
        File dosya = new File(DOSYA_ADI);
        if (dosya.exists()) return;
        new File("data").mkdirs();

        Random rnd = new Random(42); //Her çalıştırmada aynı veri sağlanır

        String[] kategoriler = {
            "Teknoloji", "Moda", "Muzik", "Spor", "Kitap",
            "Oyun", "Ev-Yasam", "Kozmetik", "Hobi-Sanat", "Otomobil-Aksesuar", "Oyuncak"
        };
        String[][] markalar = {
            {"Apple","Samsung","Xiaomi","Huawei","Sony","Asus","Lenovo","HP","Dell","LG"},
            {"Nike","Zara","Adidas","H&M","Lacoste","Prada","Gucci","Levi's","Puma","Burberry"},
            {"Yamaha","Sennheiser","Fender","Gibson","Marshall","Audio-Technica","Bose","JBL","Roland","Casio"},
            {"Decathlon","Wilson","Spalding","North Face","Columbia","Under Armour","Reebok","Mizuno","Castelli","Salomon"},
            {"Can Yayinlari","Is Bankasi Yayinlari","Pegasus","Alfa Yayinlari","Ithaki","Dogan Kitap","Yapi Kredi","Metis","Everest","Iletisim"},
            {"Razer","Logitech","MSI","SteelSeries","Nintendo","Nvidia","Corsair","Gigabyte","Zowie","HyperX"},
            {"Ikea","Karaca","Philips","Tefal","Arzum","Pasabahce","English Home","Madame Coco","Bosch","Beko"},
            {"LOreal","MAC","Estee Lauder","Sephora","Nivea","Clinique","Maybelline","Chanel","Dior","Lancome"},
            {"Faber-Castell","Winsor Newton","Prismacolor","Reeves","Daler Rowney","Liquitex","Canson","Pebeo","Derwent","Moleskine"},
            {"Michelin","Pioneer","Bosch","Castrol","Bridgestone","Meguiars","Sparco","3M","Continental","Goodyear"},
            {"LEGO","Mattel","Hasbro","Fisher-Price","Hot Wheels","Playmobil","VTech","Melissa Doug","Spin Master","MGA Entertainment"}
        };
        String[][] ilgiAlanlari = {
            {"Mobil Yasam","Yazilim","Ev Ofis"},
            {"Sokak Stili","Klasik","Aksesuar"},
            {"Analog","Studyo","Sahne"},
            {"Fitness","Outdoor","Ekipman"},
            {"Edebiyat","Kisisel Gelisim","Tarih"},
            {"E-Spor","Retro","Yayincilik"},
            {"Mutfak","Dekorasyon","Konfor"},
            {"Bakim","Parfum","Makyaj"},
            {"Resim","El Sanatlari","Kirtasiye"},
            {"Arac Bakimi","Konfor","Guvenlik"},
            {"Egitici","Aksiyon","Klasik"}
        };
        String[] yaslar = {"Cocuk","Genc","Yetiskin","Yasli"};

        try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(
                new FileOutputStream(DOSYA_ADI), StandardCharsets.UTF_8))) {
            w.write("ID;UrunAdi;Kategori;IlgiAlani;Marka;Fiyat;YasGrubu;PopulerlikPuan\n");
            for (int i = 1; i <= HEDEF_SATIR; i++) {
                int k = rnd.nextInt(kategoriler.length);
                String kat = kategoriler[k];
                String marka = markalar[k][rnd.nextInt(markalar[k].length)];
                String ilgi  = ilgiAlanlari[k][rnd.nextInt(ilgiAlanlari[k].length)];
                
                String ad = marka + " " + ilgi;
                double tabanFiyat = 100;
                double tavanFiyat = 5000;
                
                switch(kat) {
                    case "Teknoloji": 
                        String[] tekTipler = {"Bilgisayar", "Tablet", "Akıllı Telefon", "Kulaklık", "Akıllı Saat"};
                        ad = marka + " " + ilgi + " " + tekTipler[rnd.nextInt(tekTipler.length)];
                        tabanFiyat=2000; tavanFiyat=45000; break;
                    case "Kitap": 
                        ad = marka + " " + ilgi + " Romanı/Kitabı";
                        tabanFiyat=50; tavanFiyat=500; break;
                    case "Spor": 
                        String[] sporTipler = {"Koşu Ayakkabısı", "Spor Çantası", "Eşofman Takımı", "Antrenman Ekipmanı"};
                        ad = marka + " " + ilgi + " " + sporTipler[rnd.nextInt(sporTipler.length)];
                        tabanFiyat=500; tavanFiyat=8000; break;
                    case "Muzik": 
                        String[] muzikTipleri = {"Enstrüman", "Amfi", "DJ Ekipmanı", "Pikap"};
                        ad = marka + " " + ilgi + " " + muzikTipleri[rnd.nextInt(muzikTipleri.length)];
                        tabanFiyat=1000; tavanFiyat=25000; break;
                    case "Otomobil-Aksesuar": 
                        ad = marka + " " + ilgi + " Aksesuarı";
                        tabanFiyat=200; tavanFiyat=15000; break;
                    case "Moda": 
                        String[] modaTipler = {"Ceket", "Gözlük", "Çanta", "Saat", "Parfüm"};
                        ad = marka + " " + ilgi + " " + modaTipler[rnd.nextInt(modaTipler.length)];
                        tabanFiyat=300; tavanFiyat=6000; break;
                    case "Kozmetik":
                        String[] kozTipler = {"Nemlendirici", "Cilt Serumu", "Göz Kalemi", "Ruj"};
                        ad = marka + " " + ilgi + " " + kozTipler[rnd.nextInt(kozTipler.length)];
                        tabanFiyat=150; tavanFiyat=2000; break;
                    case "Ev-Yasam":
                        String[] evTipler = {"Kahve Makinesi", "Nevresim Takımı", "Dekoratif Lamba", "Robot Süpürge"};
                        ad = marka + " " + ilgi + " " + evTipler[rnd.nextInt(evTipler.length)];
                        tabanFiyat=400; tavanFiyat=12000; break;
                    default:
                        String[] genel = {"Seti", "Paketi", "Koleksiyonu"};
                        ad = marka + " " + ilgi + " " + genel[rnd.nextInt(genel.length)];
                        tabanFiyat=100; tavanFiyat=5000; break;
                }
                
                double fiyat = tabanFiyat + (tavanFiyat - tabanFiyat) * rnd.nextDouble();
                
                int pop = 1 + rnd.nextInt(100);
                String yas = yaslar[rnd.nextInt(4)];
                
                String satir = String.format(Locale.US, "%d;%s;%s;%s;%s;%.2f;%s;%d\n",
                    i, ad, kat, ilgi, marka, fiyat, yas, pop);
                w.write(satir);
            }
            System.out.println("Veri seti oluşturuldu: " + DOSYA_ADI);
        } catch (Exception e) { e.printStackTrace(); }
    }

    //CSV OKUMA VE VERİ YAPILARINA YÜKLEME

    private void csvOku() {
        VeritabaniYoneticisi db = VeritabaniYoneticisi.getOrnek();

        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(DOSYA_ADI), StandardCharsets.UTF_8))) {
            br.readLine();
            String satir;
            while ((satir = br.readLine()) != null) {
                satir = satir.trim();
                if (satir.isEmpty()) continue;
                String[] p = satir.split(";", -1);
                if (p.length < 8) continue;
                try {
                    int id       = Integer.parseInt(p[0].trim());
                    String ad    = p[1].trim();
                    String kat   = p[2].trim();
                    String ilgi  = p[3].trim();
                    String marka = p[4].trim();
                    double fiyat = Double.parseDouble(p[5].trim());
                    String yas   = p[6].trim();
                    double pop   = Double.parseDouble(p[7].trim());

                    //Veri tabanından override edilmiş puan kontrolü yapılır
                    pop = db.puanGetir(id, pop);

                    Urun urun = new Urun(id, ad, kat, ilgi, marka, fiyat, yas, pop);

                    //Dizi
                    if (urunSayisi < tumUrunlerDizi.length)
                        tumUrunlerDizi[urunSayisi++] = urun;

                    // Fiyata göre BST
                    // Veriler rastgele fiyatlarla yüklendiği için ağaç
                    // doğal olarak dengeli oluşur ve O(log N) performansı korunur.
                    urunBST.ekle(urun);

                    // Kategori için hashMap
                    kategoriMap.ekle(kat, urun);

                    // Marka için hashmap
                    markaMap.ekle(marka, urun);

                    //TreeMap<String, TreeSet<String>>
                    kategoriMarkaSeti.computeIfAbsent(kat, x -> new TreeSet<>()).add(marka);

                } catch (NumberFormatException ignored) {}
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    //ERİŞİM METODLARI

    public Urun[] getTumUrunlerDizi()   { return tumUrunlerDizi; }
    public int    getUrunSayisi()       { return urunSayisi; }
    public UrunBST getUrunBST()         { return urunBST; }
    public UrunHashMap getKategoriMap() { return kategoriMap; }
    public UrunHashMap getMarkaMap()    { return markaMap; }
    public TreeMap<String, TreeSet<String>> getKategoriMarkaSeti() { return kategoriMarkaSeti; }
    public KategoriGraph getKategoriGraph() { return kategoriGraph; }

    public double getMinFiyat() {
        double min = Double.MAX_VALUE;
        for (int i = 0; i < urunSayisi; i++)
            if (tumUrunlerDizi[i].getFiyat() < min) min = tumUrunlerDizi[i].getFiyat();
        return min;
    }

    public double getMaxFiyat() {
        double max = 0;
        for (int i = 0; i < urunSayisi; i++)
            if (tumUrunlerDizi[i].getFiyat() > max) max = tumUrunlerDizi[i].getFiyat();
        return max;
    }

    public int getKategoriSayisi() { return kategoriMarkaSeti.size(); }
}
