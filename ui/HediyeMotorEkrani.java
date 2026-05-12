package com.hediye.ui;

import com.hediye.database.VeritabaniYoneticisi;
import com.hediye.model.Kisi;
import com.hediye.model.SepetUrun;
import com.hediye.model.Urun;
import com.hediye.datastructures.SepetLinkedList;
import com.hediye.service.HediyeServisi;
import com.hediye.service.VeriYukleyici;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.util.*;

public class HediyeMotorEkrani extends BorderPane {

    private final int kullaniciId;
    private final VeritabaniYoneticisi db;
    private final HediyeServisi servis;
    private final VeriYukleyici veri;

    private final Map<String, CheckBox> kategoriCheckboxlar = new LinkedHashMap<>();
    private TextField maxFiyatField;
    private ComboBox<Kisi> kisiCombo;
    private ComboBox<String> siralamaCombo;
    private ComboBox<String> markaCombo;
    private ComboBox<String> yasCombo;

    private final SepetLinkedList sepet = new SepetLinkedList();
    private VBox urunListesiKutu;
    private VBox sepetListesi;
    private Label toplamLabel;
    private CheckBox akilliOneriCb;
    private Runnable anaSayfaYenileCallback;

    // Döngü engelleme
    private boolean isUpdatingUI = false;

    private static final String GRAD_PEARL = "linear-gradient(to bottom right, #ffffff, #f0f4f7, #e0eafc)";
    private static final String GRAD_VOGUE_PINK = "linear-gradient(to right, #ff9a9e, #fecfef)";
    private static final String GRAD_LUXURY_TEAL = "linear-gradient(to right, #a1c4fd, #c2e9fb)";

    public HediyeMotorEkrani(int kullaniciId) {
        this.kullaniciId = kullaniciId;
        this.db = VeritabaniYoneticisi.getOrnek();
        this.servis = HediyeServisi.getOrnek();
        this.veri = VeriYukleyici.getOrnek();

        setStyle("-fx-background-color: " + GRAD_PEARL + ";");
        buildUI();
    }

    public void setKisiler(List<Kisi> kisiler) {
        if (kisiCombo != null) {
            kisiCombo.getItems().setAll(kisiler);
            if (!kisiler.isEmpty()) kisiCombo.getSelectionModel().selectFirst();
        }
    }

    private void buildUI() {
        HBox top = new HBox(20);
        top.setPadding(new Insets(20));
        top.setAlignment(Pos.CENTER_LEFT);
        top.setStyle("-fx-background-color: white; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        Label b = new Label("🎁 Akıllı Hediye Seçici 🎁");
        b.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: black;");
        top.getChildren().add(b);
        setTop(top);

        HBox center = new HBox(24);
        center.setPadding(new Insets(24));

        VBox fPanel = buildFiltrePanel();
        fPanel.setPrefWidth(300);

        VBox uPanel = buildUrunPanel();
        HBox.setHgrow(uPanel, Priority.ALWAYS);

        VBox sPanel = buildSepetPanel();
        sPanel.setPrefWidth(350);

        center.getChildren().addAll(fPanel, uPanel, sPanel);
        setCenter(center);
    }

    private VBox buildFiltrePanel() {
        VBox v = card();
        v.setSpacing(12);

        v.getChildren().add(labelL("Kime Hediye Alıyoruz?"));
        kisiCombo = new ComboBox<>();
        kisiCombo.setMaxWidth(Double.MAX_VALUE);
        v.getChildren().add(kisiCombo);

        v.getChildren().add(labelL("Kategoriler"));
        VBox kBox = new VBox(5);
        String[] kats = {"Teknoloji","Moda","Muzik","Spor","Kitap","Oyun","Ev-Yasam","Kozmetik","Hobi-Sanat","Otomobil-Aksesuar","Oyuncak"};
        for (String k : kats) {
            CheckBox cb = new CheckBox(k);
            cb.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
            kBox.getChildren().add(cb);
            kategoriCheckboxlar.put(k, cb);
            cb.setOnAction(e -> {
                markalariguncelle();
                kisiProfiliniGuncelle(); // Dinamik Güncelleme
            });
        }
        v.getChildren().add(kBox);

        v.getChildren().add(labelL("Yaş Grubu"));
        yasCombo = new ComboBox<>();
        yasCombo.getItems().addAll("Tumu", "Çocuk", "Genç", "Yetişkin", "Yaşlı");
        yasCombo.setValue("Tumu");
        yasCombo.setMaxWidth(Double.MAX_VALUE);
        yasCombo.setOnAction(e -> kisiProfiliniGuncelle()); // Dinamik Güncelleme
        v.getChildren().add(yasCombo);

        v.getChildren().add(labelL("Marka"));
        markaCombo = new ComboBox<>();
        markaCombo.setMaxWidth(Double.MAX_VALUE);
        markaCombo.getItems().add("Tumu");
        markaCombo.setValue("Tumu");
        v.getChildren().add(markaCombo);

        v.getChildren().add(labelL("Maksimum Bütçe (TL)"));
        maxFiyatField = new TextField("50000");
        v.getChildren().add(maxFiyatField);

        akilliOneriCb = new CheckBox("Akıllı Öneri (Yan Ürünler)");
        akilliOneriCb.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");
        v.getChildren().add(akilliOneriCb);

        // Kişi seçildiğinde profil bilgilerini arayüze yansıtır
        kisiCombo.setOnAction(e -> {
            Kisi secili = kisiCombo.getValue();
            if (secili != null) {
                isUpdatingUI = true; // Dinamik güncellemeyi geçici olarak durdurur

                if (secili.getYasGrubu() != null && !secili.getYasGrubu().isEmpty()) {
                    yasCombo.setValue(secili.getYasGrubu());
                } else {
                    yasCombo.setValue("Tumu");
                }

                String[] ilgiler = secili.getIlgiAlanlari().split("[,\\s]+");
                kategoriCheckboxlar.forEach((k, cb) -> cb.setSelected(false));
                for (String ilgi : ilgiler) {
                    if (kategoriCheckboxlar.containsKey(ilgi.trim())) {
                        kategoriCheckboxlar.get(ilgi.trim()).setSelected(true);
                    }
                }
                markalariguncelle();

                isUpdatingUI = false;
            }
        });

        HBox btnBox = new HBox(10);
        Button sBtn = new Button("Listele");
        sBtn.setPrefWidth(120);
        sBtn.setStyle("-fx-background-color: " + GRAD_VOGUE_PINK + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 10;");
        sBtn.setOnAction(e -> filtrele());

        Button rBtn = new Button("Temizle");
        rBtn.setPrefWidth(120);
        rBtn.setStyle("-fx-background-color: #f1f5f9; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-padding: 10; -fx-background-radius: 10; -fx-border-color: #e2e8f0;");
        rBtn.setOnAction(e -> filtreleriTemizle());

        btnBox.getChildren().addAll(sBtn, rBtn);
        v.getChildren().add(btnBox);

        return v;
    }

    private void kisiProfiliniGuncelle() {
        if (isUpdatingUI) return; // UI yüklenirken tetiklenme sağlanır

        Kisi secili = kisiCombo.getValue();
        if (secili != null) {
            List<String> sKat = new ArrayList<>();
            kategoriCheckboxlar.forEach((k, cb) -> { if (cb.isSelected()) sKat.add(k); });
            String yeniIlgi = String.join(", ", sKat);
            String yeniYas = yasCombo.getValue();

            secili.setIlgiAlanlari(yeniIlgi);
            secili.setYasGrubu(yeniYas);

            // DB'ye kaydeder
            db.kisiBilgileriniGuncelle(secili.getId(), yeniYas, yeniIlgi);

            // Ana sayfadaki kartları günceller
            if (anaSayfaYenileCallback != null) anaSayfaYenileCallback.run();
        }
    }

    private void markalariguncelle() {
        List<String> sKat = new ArrayList<>();
        kategoriCheckboxlar.forEach((k, box) -> { if (box.isSelected()) sKat.add(k); });
        List<String> markalar = servis.markalariGetir(sKat);
        markaCombo.getItems().setAll(markalar);
        markaCombo.setValue("Tumu");
    }

    private void filtreleriTemizle() {
        isUpdatingUI = true;
        kategoriCheckboxlar.forEach((k, cb) -> cb.setSelected(false));
        yasCombo.setValue("Tumu");
        markaCombo.setValue("Tumu");
        maxFiyatField.setText("50000");
        akilliOneriCb.setSelected(false);
        urunListesiKutu.getChildren().clear();
        isUpdatingUI = false;
    }

    private void filtrele() {
        List<String> sKat = new ArrayList<>();
        kategoriCheckboxlar.forEach((k, cb) -> { if (cb.isSelected()) sKat.add(k); });

        double max = 50000;
        try { max = Double.parseDouble(maxFiyatField.getText()); } catch (Exception e) {}

        String mapYas = yasCombo.getValue();
        if ("Tumu".equals(mapYas)) mapYas = null;
        else if ("Çocuk".equals(mapYas)) mapYas = "Cocuk";
        else if ("Genç".equals(mapYas)) mapYas = "Genc";
        else if ("Yetişkin".equals(mapYas)) mapYas = "Yetiskin";
        else if ("Yaşlı".equals(mapYas)) mapYas = "Yasli";

        HediyeServisi.FiltreParametresi p = new HediyeServisi.FiltreParametresi(
                sKat, mapYas, max, markaCombo.getValue().equals("Tumu") ? null : markaCombo.getValue(),
                siralamaCombo.getValue(), akilliOneriCb.isSelected());

        List<Urun> list = servis.filtrele(p);
        urunListesiKutu.getChildren().clear();
        for (Urun u : list) urunListesiKutu.getChildren().add(buildUrunKart(u));
    }

    private VBox buildUrunPanel() {
        VBox v = new VBox(15);
        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);
        Label l = new Label("Önerilen Ürünler");
        l.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: black;");

        Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);

        siralamaCombo = new ComboBox<>();
        siralamaCombo.getItems().addAll("Popülerlik", "Fiyat Artan", "Fiyat Azalan");
        siralamaCombo.setValue("Popülerlik");
        siralamaCombo.setOnAction(e -> filtrele());

        h.getChildren().addAll(l, s, siralamaCombo);

        urunListesiKutu = new VBox(10);
        ScrollPane sp = new ScrollPane(urunListesiKutu);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        VBox.setVgrow(sp, Priority.ALWAYS);
        v.getChildren().addAll(h, sp);
        return v;
    }

    private VBox buildSepetPanel() {
        VBox v = card();
        v.setSpacing(15);
        Label l = new Label("Hediye Sepetim");
        l.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: black;");

        sepetListesi = new VBox(10);
        ScrollPane sp = new ScrollPane(sepetListesi);
        sp.setFitToWidth(true);
        VBox.setVgrow(sp, Priority.ALWAYS);

        toplamLabel = new Label("Toplam: 0,00 TL");
        toplamLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #764ba2;");

        Button kBtn = new Button("Seçilenleri Kaydet");
        kBtn.setMaxWidth(Double.MAX_VALUE);
        kBtn.setStyle("-fx-background-color: " + GRAD_LUXURY_TEAL + "; -fx-text-fill: #1b263b; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 15;");
        kBtn.setOnAction(e -> sepetKaydet());

        v.getChildren().addAll(l, sp, toplamLabel, kBtn);
        return v;
    }

    private HBox buildUrunKart(Urun u) {
        HBox h = new HBox(15);
        h.setPadding(new Insets(15));
        h.setAlignment(Pos.CENTER_LEFT);
        h.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 2);");

        CheckBox cb = new CheckBox();
        cb.setSelected(sepet.iceriyor(u.getId()));
        cb.setOnAction(e -> {
            if (cb.isSelected()) sepet.sonaEkle(new SepetUrun(u));
            else sepet.sil(u.getId());
            sepetGuncelle();
        });

        VBox v = new VBox(4);
        Label ad = new Label(u.getUrunAdi());
        ad.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: black;");
        Label det = new Label(u.getMarka() + " | " + String.format("%.2f TL", u.getFiyat()) + " | Puan: " + String.format("%.1f", u.getPopulerlikPuan()));
        det.setStyle("-fx-text-fill: #475569; -fx-font-weight: bold;");
        v.getChildren().addAll(ad, det);

        h.getChildren().addAll(cb, v);
        return h;
    }

    private void sepetGuncelle() {
        sepetListesi.getChildren().clear();
        for (SepetUrun su : sepet.listeOlarakGetir()) {
            VBox v = new VBox(5);
            v.setPadding(new Insets(10));
            v.setStyle("-fx-background-color: #f0f4f7; -fx-background-radius: 10;");

            HBox top = new HBox(10);
            Label ad = new Label(su.getUrun().getUrunAdi());
            ad.setStyle("-fx-font-weight: bold; -fx-font-size: 12px; -fx-text-fill: black;");

            Region s = new Region();
            HBox.setHgrow(s, Priority.ALWAYS);

            Button d = new Button("✕");
            d.setStyle("-fx-background-color: transparent; -fx-text-fill: red; -fx-font-weight: bold;");
            d.setOnAction(e -> { sepet.sil(su.getUrun().getId()); sepetGuncelle(); });

            top.getChildren().addAll(ad, s, d);

            HBox stars = new HBox(3);
            stars.setAlignment(Pos.CENTER_LEFT);
            for (int i = 1; i <= 5; i++) {
                final int idx = i;
                Label star = new Label(i <= su.getYildiz() ? "★" : "☆");
                star.setStyle("-fx-text-fill: #bf953f; -fx-font-size: 18px; -fx-cursor: hand;");
                star.setOnMouseClicked(e -> { su.setYildiz(idx); sepetGuncelle(); });
                stars.getChildren().add(star);
            }

            Button guncelleBtn = new Button("Güncelle");
            guncelleBtn.setStyle("-fx-background-color: #4ca1af; -fx-text-fill: white; -fx-padding: 3 8; -fx-font-size: 10px; -fx-background-radius: 5;");
            guncelleBtn.setOnAction(e -> {
                if (su.getYildiz() > 0) {
                    servis.puanGuncelle(su.getUrun(), su.getYildiz());
                    su.setYildiz(0);
                    sepetGuncelle();
                    filtrele();
                }
            });

            stars.getChildren().addAll(new Label("  "), guncelleBtn);
            v.getChildren().addAll(top, stars);
            sepetListesi.getChildren().add(v);
        }
        toplamLabel.setText(String.format("Toplam: %.2f TL", sepet.toplamFiyat()));
    }

    private void sepetKaydet() {
        Kisi k = kisiCombo.getValue();
        if (k == null) return;
        for (SepetUrun su : sepet.listeOlarakGetir()) {
            db.hediyeKaydet(kullaniciId, k.getId(), su.getUrun().getId(), su.getUrun().getUrunAdi(), su.getUrun().getFiyat(), su.getUrun().getKategori());
        }
        if (anaSayfaYenileCallback != null) anaSayfaYenileCallback.run();
        new Alert(Alert.AlertType.INFORMATION, "Hediyeler kaydedildi!").show();
    }

    public void setAnaSayfaYenileCallback(Runnable cb) { this.anaSayfaYenileCallback = cb; }
    private Label labelL(String t) { Label l = new Label(t); l.setStyle("-fx-text-fill: black; -fx-font-weight: bold;"); return l; }
    private VBox card() { VBox v = new VBox(); v.setPadding(new Insets(20)); v.setStyle("-fx-background-color: white; -fx-background-radius: 20; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 15, 0, 0, 5);"); return v; }
}
