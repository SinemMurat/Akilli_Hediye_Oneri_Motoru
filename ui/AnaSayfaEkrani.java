package com.hediye.ui;

import com.hediye.database.VeritabaniYoneticisi;
import com.hediye.model.Kisi;
import com.hediye.model.TakvimEtiketi;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Ana Sayfa Ekranı
 */
public class AnaSayfaEkrani extends BorderPane {

    private final int kullaniciId;
    private final String kullaniciAd;
    private final VeritabaniYoneticisi db;

    private YearMonth gorunenAy;
    private GridPane takvimGrid;
    private Label ayYilLabel;

    private List<Kisi> kisiler = new ArrayList<>();
    private List<TakvimEtiketi> etiketler = new ArrayList<>();

    private FlowPane kisiKartlari;
    private VBox yaklasanListesi;
    private Runnable hediyeMotorCallback;

    private static final String GRAD_PEARL = "linear-gradient(to bottom right, #ffffff, #f0f4f7, #e0eafc)";
    private static final String GRAD_VOGUE_PINK = "linear-gradient(to right, #ff9a9e, #fecfef)";
    private static final String GRAD_LUXURY_TEAL = "linear-gradient(to right, #a1c4fd, #c2e9fb)";
    private static final String CARD_LIGHT = "-fx-background-color: rgba(255, 255, 255, 0.85); -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 15, 0, 0, 5);";

    public AnaSayfaEkrani(int kullaniciId, String kullaniciAd) {
        this.kullaniciId = kullaniciId;
        this.kullaniciAd = kullaniciAd;
        this.db = VeritabaniYoneticisi.getOrnek();
        this.gorunenAy = YearMonth.now();

        setStyle("-fx-background-color: " + GRAD_PEARL + ";");
        setPadding(new Insets(24));

        yenile();
        buildUI();
    }

    public void setHediyeMotorCallback(Runnable cb) { this.hediyeMotorCallback = cb; }

    public void yenile() {
        this.kisiler = db.kisileriGetir(kullaniciId);
        this.etiketler = db.etiketleriGetir(kullaniciId);
        Platform.runLater(() -> {
            if (takvimGrid != null) takvimYenile();
            if (kisiKartlari != null) kisiKartlariniYenile();
            if (yaklasanListesi != null) yaklasanListesiniYenile();
        });
    }

    private void buildUI() {
        // Üst Bar
        HBox topBar = new HBox(20);
        topBar.setAlignment(Pos.CENTER_LEFT);
        topBar.setPadding(new Insets(0, 0, 20, 0));

        Label logo = new Label("🎁");
        logo.setFont(Font.font(32));
        
        VBox userBox = new VBox(2);
        Label hLabel = new Label("Hoş Geldin,");
        hLabel.setTextFill(Color.web("#667eea"));
        hLabel.setFont(Font.font("System", FontWeight.MEDIUM, 14));
        Label nLabel = new Label(kullaniciAd);
        nLabel.setFont(Font.font("System", FontWeight.BOLD, 24));
        nLabel.setTextFill(Color.web("#1b263b"));
        userBox.getChildren().addAll(hLabel, nLabel);

        topBar.getChildren().addAll(logo, userBox);
        setTop(topBar);

        // İçerik Alanı
        HBox center = new HBox(24);
        
        VBox sol = new VBox(24);
        HBox.setHgrow(sol, Priority.ALWAYS);
        
        sol.getChildren().addAll(buildTakvimSection(), buildKisiSection());

        VBox sag = new VBox(24);
        sag.setPrefWidth(350);
        sag.getChildren().addAll(buildYaklasanSection(), buildActionBtn());

        center.getChildren().addAll(sol, sag);
        setCenter(center);
    }

    private VBox buildTakvimSection() {
        VBox v = card();
        
        HBox h = new HBox(10);
        h.setAlignment(Pos.CENTER_LEFT);
        ayYilLabel = new Label();
        ayYilLabel.setFont(Font.font("System", FontWeight.BOLD, 18));
        
        Region s = new Region();
        HBox.setHgrow(s, Priority.ALWAYS);
        
        Button p = navBtn("←");
        Button n = navBtn("→");
        p.setOnAction(ev -> { gorunenAy = gorunenAy.minusMonths(1); takvimYenile(); });
        n.setOnAction(ev -> { gorunenAy = gorunenAy.plusMonths(1); takvimYenile(); });
        
        h.getChildren().addAll(ayYilLabel, s, p, n);

        takvimGrid = new GridPane();
        takvimGrid.setHgap(8); takvimGrid.setVgap(8);
        takvimYenile();

        v.getChildren().addAll(h, new Separator(), takvimGrid);
        return v;
    }

    private void takvimYenile() {
        takvimGrid.getChildren().clear();
        ayYilLabel.setText(gorunenAy.getMonth().getDisplayName(java.time.format.TextStyle.FULL, new Locale("tr")) + " " + gorunenAy.getYear());

        String[] gunler = {"Pzt","Sal","Çar","Per","Cum","Cmt","Paz"};
        for (int i = 0; i < 7; i++) {
            Label l = new Label(gunler[i]);
            l.setFont(Font.font("System", FontWeight.BOLD, 12));
            l.setTextFill(Color.web("#764ba2"));
            l.setMinWidth(45); l.setAlignment(Pos.CENTER);
            takvimGrid.add(l, i, 0);
        }

        LocalDate ilk = gorunenAy.atDay(1);
        int off = ilk.getDayOfWeek().getValue() - 1;
        int len = gorunenAy.lengthOfMonth();

        int row = 1, col = off;
        for (int d = 1; d <= len; d++) {
            LocalDate t = gorunenAy.atDay(d);
            takvimGrid.add(buildGunBtn(t), col, row);
            col++; if (col == 7) { col = 0; row++; }
        }
    }

    private Button buildGunBtn(LocalDate t) {
        Button b = new Button(String.valueOf(t.getDayOfMonth()));
        b.setMinSize(45, 45);
        
        List<TakvimEtiketi> gEtiketler = etiketler.stream().filter(e -> e.getTarih().equals(t)).collect(Collectors.toList());
        boolean bugun = t.equals(LocalDate.now());

        String style = "-fx-background-radius: 12; -fx-cursor: hand; ";
        if (!gEtiketler.isEmpty()) {
            String renk = gEtiketler.get(0).getKisiRenk();
            if(renk == null || renk.equals("null") || renk.isEmpty()) renk = GRAD_LUXURY_TEAL;
            style += "-fx-background-color: " + renk + "; -fx-text-fill: white; -fx-font-weight: bold;";
        } else if (bugun) {
            style += "-fx-background-color: " + GRAD_VOGUE_PINK + "; -fx-text-fill: white; -fx-font-weight: bold;";
        } else {
            style += "-fx-background-color: white; -fx-text-fill: #1b263b; -fx-border-color: #f0f4f7; -fx-border-radius: 12;";
        }
        b.setStyle(style);
        b.setOnAction(e -> gunSecildi(t));
        return b;
    }

    private void gunSecildi(LocalDate t) {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Hatırlatıcı Düzenle");
        d.getDialogPane().setStyle("-fx-background-color: white;");

        VBox icerik = new VBox(15);
        icerik.setPadding(new Insets(20));
        icerik.setPrefWidth(350);

        ComboBox<Kisi> c = new ComboBox<>();
        c.getItems().addAll(kisiler);
        c.setMaxWidth(Double.MAX_VALUE);
        c.setPromptText("Kişi seçin...");

        TextArea a = new TextArea();
        a.setPromptText("Etkinlik notu...");
        a.setPrefHeight(80);

        List<TakvimEtiketi> mev = etiketler.stream().filter(e -> e.getTarih().equals(t)).collect(Collectors.toList());
        if (!mev.isEmpty()) {
            TakvimEtiketi m = mev.get(0);
            kisiler.stream().filter(k -> k.getId() == m.getKisiId()).findFirst().ifPresent(c::setValue);
            a.setText(m.getMetin());
        }

        icerik.getChildren().addAll(new Label("Hangi Kişi İçin?"), c, new Label("Notun"), a);
        d.getDialogPane().setContent(icerik);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        if (!mev.isEmpty()) {
            ButtonType silType = new ButtonType("Sil", ButtonBar.ButtonData.LEFT);
            d.getDialogPane().getButtonTypes().add(silType);
        }

        d.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                Kisi k = c.getValue();
                if (k == null) return;
                if (!mev.isEmpty()) db.etiketGuncelle(mev.get(0).getId(), a.getText());
                else db.etiketEkle(kullaniciId, t, k.getId(), k.getAd(), k.getRenk(), a.getText());
                yenile();
            } else if (bt.getButtonData() == ButtonBar.ButtonData.LEFT) {
                mev.forEach(e -> db.etiketSil(e.getId()));
                yenile();
            }
        });
    }

    private VBox buildKisiSection() {
        VBox v = card();
        Label l = new Label("Kişilerim");
        l.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        kisiKartlari = new FlowPane(12, 12);
        kisiKartlariniYenile();
        
        v.getChildren().addAll(l, new Separator(), kisiKartlari);
        return v;
    }

    private void kisiKartlariniYenile() {
        kisiKartlari.getChildren().clear();
        for (Kisi k : kisiler) {
            VBox kart = new VBox(8);
            kart.setAlignment(Pos.CENTER);
            kart.setPrefSize(80, 80);
            kart.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-border-color: #a1c4fd; -fx-border-radius: 15; -fx-cursor: hand;");
            
            Label av = new Label(k.getAd().substring(0, 1).toUpperCase());
            av.setFont(Font.font(24));
            av.setTextFill(Color.web("#a1c4fd"));
            
            Label n = new Label(k.getAd());
            n.setFont(Font.font(11));
            
            kart.getChildren().addAll(av, n);
            kart.setOnMouseClicked(e -> {
                Stage s = new Stage();
                s.setScene(new Scene(new KisiDetayEkrani(kullaniciId, k, etiketler, this::yenile), 700, 600));
                s.show();
            });
            kisiKartlari.getChildren().add(kart);
        }

        VBox add = new VBox();
        add.setAlignment(Pos.CENTER);
        add.setPrefSize(80, 80);
        add.setStyle("-fx-background-color: #fdfbfb; -fx-background-radius: 15; -fx-border-style: dashed; -fx-border-color: #cfdef3; -fx-border-radius: 15; -fx-cursor: hand;");
        Label plus = new Label("+");
        plus.setFont(Font.font(30));
        plus.setTextFill(Color.web("#cfdef3"));
        add.getChildren().add(plus);
        add.setOnMouseClicked(e -> kisiEkleDialog());
        kisiKartlari.getChildren().add(add);
    }

    private void kisiEkleDialog() {
        Dialog<ButtonType> d = new Dialog<>();
        d.setTitle("Kişi Ekle");
        d.getDialogPane().setStyle("-fx-background-color: white;");

        VBox v = new VBox(15);
        v.setPadding(new Insets(20));

        TextField adF = modernInput("Ad Soyad");
        ComboBox<String> yasC = new ComboBox<>();
        yasC.getItems().addAll("Çocuk", "Genç", "Yetişkin", "Yaşlı");
        yasC.setValue("Yetişkin");
        yasC.setMaxWidth(Double.MAX_VALUE);
        yasC.setStyle("-fx-background-color: #f0f4f7; -fx-background-radius: 8;");

        FlowPane ilgiKutusu = new FlowPane(10, 10);
        String[] kats = {"Teknoloji","Moda","Muzik","Spor","Kitap","Oyun","Ev-Yasam","Kozmetik","Hobi-Sanat","Otomobil-Aksesuar","Oyuncak"};
        List<CheckBox> cbList = new ArrayList<>();
        for(String k : kats) {
            CheckBox cb = new CheckBox(k);
            cbList.add(cb);
            ilgiKutusu.getChildren().add(cb);
        }

        // Rastgele renk ataması
        String[] renkler = {"#4F46E5", "#EC4899", "#10B981", "#F59E0B", "#8B5CF6", "#F43F5E", "#06B6D4"};
        String rastgeleRenk = renkler[new Random().nextInt(renkler.length)];

        Label renkLabel = new Label("  Atanan Renk  ");
        renkLabel.setStyle("-fx-background-color: " + rastgeleRenk + "; -fx-text-fill: white; -fx-background-radius: 6; -fx-padding: 5;");

        v.getChildren().addAll(new Label("Ad Soyad"), adF, new Label("Yaş Grubu"), yasC, new Label("İlgi Alanları"), ilgiKutusu, renkLabel);
        
        d.getDialogPane().setContent(v);
        d.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        
        d.showAndWait().ifPresent(bt -> {
            if (bt == ButtonType.OK) {
                String ad = adF.getText().trim();
                List<String> secilenIlgi = new ArrayList<>();
                for(CheckBox cb : cbList) if(cb.isSelected()) secilenIlgi.add(cb.getText());
                String ilgi = String.join(", ", secilenIlgi);
                
                if (!ad.isEmpty()) {
                    db.kisiEkle(kullaniciId, ad, yasC.getValue(), ilgi, rastgeleRenk);
                    yenile();
                }
            }
        });
    }

    private VBox buildYaklasanSection() {
        VBox v = card();
        VBox.setVgrow(v, Priority.ALWAYS);
        Label l = new Label("Hatırlatıcılar");
        l.setFont(Font.font("System", FontWeight.BOLD, 16));
        
        yaklasanListesi = new VBox(10);
        ScrollPane sp = new ScrollPane(yaklasanListesi);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: transparent; -fx-background-color: transparent;");
        
        yaklasanListesiniYenile();
        
        v.getChildren().addAll(l, sp);
        return v;
    }

    private void yaklasanListesiniYenile() {
        yaklasanListesi.getChildren().clear();
        LocalDate bugun = LocalDate.now();
        etiketler.stream()
            .filter(e -> {
                long d = bugun.until(e.getTarih(), java.time.temporal.ChronoUnit.DAYS);
                return d >= 0 && d <= 7;
            })
            .sorted(Comparator.comparing(TakvimEtiketi::getTarih))
            .forEach(e -> {
                String kisiAdi = e.getKisiAdi() != null ? e.getKisiAdi() : "Bilinmeyen";
                String metin = e.getMetin() != null ? e.getMetin() : "";
                String renk = (e.getKisiRenk() != null && !e.getKisiRenk().isEmpty() && !e.getKisiRenk().equals("null")) ? e.getKisiRenk() : "#ff9a9e";

                VBox k = new VBox(8);
                k.setPadding(new Insets(15));
                k.setStyle("-fx-background-color: linear-gradient(to right, white, #f8f9fa); -fx-background-radius: 12; -fx-border-color: " + renk + "; -fx-border-width: 0 0 0 6; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 10, 0, 0, 3);");
                
                long kalan = bugun.until(e.getTarih(), java.time.temporal.ChronoUnit.DAYS);
                Label t = new Label(kalan == 0 ? " BUGÜN!" : " " + kalan + " GÜN KALDI");
                t.setFont(Font.font("System", FontWeight.EXTRA_BOLD, 16));
                t.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
                try { t.setTextFill(Color.web(renk)); } catch (Exception ex) {}
                
                Label n = new Label(kisiAdi + " - " + metin);
                n.setFont(Font.font("System", FontWeight.NORMAL, 14));
                n.setWrapText(true);
                n.setMinHeight(javafx.scene.layout.Region.USE_PREF_SIZE);
                n.setTextFill(Color.web("#334155"));
                
                k.getChildren().addAll(t, n);
                yaklasanListesi.getChildren().add(k);
            });
            
        if (yaklasanListesi.getChildren().isEmpty()) {
            Label bos = new Label("Yaklaşan etkinlik yok.");
            bos.setTextFill(Color.GRAY);
            yaklasanListesi.getChildren().add(bos);
        }
    }

    private Button buildActionBtn() {
        Button b = new Button("🎁 Hediye Motorunu Aç");
        b.setMaxWidth(Double.MAX_VALUE);
        b.setStyle("-fx-background-color: " + GRAD_VOGUE_PINK + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 15; -fx-background-radius: 20; -fx-cursor: hand;");
        b.setOnAction(e -> { if (hediyeMotorCallback != null) hediyeMotorCallback.run(); });
        return b;
    }

    private VBox card() {
        VBox v = new VBox(15);
        v.setPadding(new Insets(20));
        v.setStyle(CARD_LIGHT);
        return v;
    }

    private Button navBtn(String t) {
        Button b = new Button(t);
        b.setStyle("-fx-background-color: white; -fx-background-radius: 8; -fx-border-color: #e0eafc; -fx-cursor: hand;");
        return b;
    }

    private TextField modernInput(String p) {
        TextField f = new TextField();
        f.setPromptText(p);
        f.setStyle("-fx-background-color: #f0f4f7; -fx-background-radius: 10; -fx-padding: 12;");
        return f;
    }

    public List<Kisi> getKisiler() { return kisiler; }
}
