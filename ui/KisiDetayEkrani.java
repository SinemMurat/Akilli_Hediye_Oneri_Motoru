package com.hediye.ui;

import com.hediye.database.VeritabaniYoneticisi;
import com.hediye.model.Kisi;
import com.hediye.model.TakvimEtiketi;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Kişi Detay Ekranı
 */
public class KisiDetayEkrani extends BorderPane {

    private final Kisi kisi;
    private final List<TakvimEtiketi> etiketler;
    private final Runnable yenileCallback;
    private final VeritabaniYoneticisi db;

    // Stil Sabitleri
    private static final String GRAD_PEARL = "linear-gradient(to bottom right, #ffffff, #f0f4f7, #e0eafc)";
    private static final String GRAD_VOGUE_PINK = "linear-gradient(to right, #ff9a9e, #fecfef)";
    private static final String GRAD_LUXURY_TEAL = "linear-gradient(to right, #a1c4fd, #c2e9fb)";

    public KisiDetayEkrani(int kullaniciId, Kisi kisi, List<TakvimEtiketi> etiketler, Runnable yenileCallback) {
        this.kisi = kisi;
        this.etiketler = etiketler;
        this.yenileCallback = yenileCallback;
        this.db = VeritabaniYoneticisi.getOrnek();

        setStyle("-fx-background-color: " + GRAD_PEARL + ";");
        setPadding(new Insets(24));
        buildUI();
    }

    private void buildUI() {
        VBox header = new VBox(15);
        header.setAlignment(Pos.CENTER);
        header.setPadding(new Insets(0, 0, 20, 0));

        Label avatar = new Label(kisi.getAd().substring(0, 1).toUpperCase());
        avatar.setStyle("-fx-background-color: " + GRAD_LUXURY_TEAL + "; -fx-text-fill: #1b263b; -fx-background-radius: 40; -fx-min-width: 80; -fx-min-height: 80; -fx-font-size: 32px; -fx-font-weight: bold;");
        avatar.setAlignment(Pos.CENTER);

        HBox headTop = new HBox(15);
        headTop.setAlignment(Pos.CENTER);
        
        Label ad = new Label(kisi.getAd());
        ad.setFont(Font.font("System", FontWeight.BOLD, 26));
        ad.setTextFill(Color.web("#764ba2"));

        Button silBtn = new Button("🗑");
        silBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #ff5e62; -fx-font-size: 18px; -fx-cursor: hand;");
        silBtn.setOnAction(e -> {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION, kisi.getAd() + " adlı kişiyi silmek istediğine emin misin?", ButtonType.YES, ButtonType.NO);
            alert.showAndWait().ifPresent(res -> {
                if (res == ButtonType.YES) {
                    db.kisiSil(kisi.getId());
                    if (yenileCallback != null) yenileCallback.run();
                    ((javafx.stage.Stage) getScene().getWindow()).close();
                }
            });
        });
        
        headTop.getChildren().addAll(ad, silBtn);

        Label det = new Label(kisi.getYasGrubu() + " | " + kisi.getIlgiAlanlari());
        det.setTextFill(Color.web("#4ca1af"));

        header.getChildren().addAll(avatar, headTop, det);
        setTop(header);

        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        Tab t1 = new Tab("Kayıtlı Hediyeler", buildHediyeListesi());
        Tab t2 = new Tab("Önemli Tarihler", buildTarihListesi());
        
        tabs.getTabs().addAll(t1, t2);
        setCenter(tabs);
    }

    private VBox buildHediyeListesi() {
        VBox v = new VBox(12);
        v.setPadding(new Insets(20));
        
        List<String[]> hediyeler = db.hediyeleriGetirDetay(kisi.getId());
        if (hediyeler.isEmpty()) {
            v.getChildren().add(new Label("Henüz bir hediye kaydedilmemiş."));
        } else {
            for (String[] h : hediyeler) {
                HBox card = new HBox(15);
                card.setPadding(new Insets(15));
                card.setAlignment(Pos.CENTER_LEFT);
                card.setStyle("-fx-background-color: white; -fx-background-radius: 15; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 10, 0, 0, 5);");
                
                boolean tamam = h[4].equals("1");
                CheckBox cb = new CheckBox();
                cb.setSelected(tamam);
                cb.setOnAction(e -> {
                    db.hediyeleriTamamlandiGuncelle(Integer.parseInt(h[0]), cb.isSelected());
                    if (yenileCallback != null) yenileCallback.run();
                });

                VBox info = new VBox(2);
                Label n = new Label(h[1]);
                n.setFont(Font.font("System", FontWeight.BOLD, 14));
                Label f = new Label(h[2] + " TL");
                f.setTextFill(Color.web("#ff5e62"));
                info.getChildren().addAll(n, f);
                HBox.setHgrow(info, Priority.ALWAYS);

                Button silBtn = new Button("Sil");
                silBtn.setStyle("-fx-background-color: #ff5e62; -fx-text-fill: white; -fx-background-radius: 8; -fx-padding: 5 10; -fx-cursor: hand;");
                silBtn.setOnAction(e -> {
                    db.hediyeSil(Integer.parseInt(h[0]));
                    if (yenileCallback != null) yenileCallback.run();
                    buildUI();
                });

                card.getChildren().addAll(cb, info, silBtn);
                v.getChildren().add(card);
            }
        }
        return v;
    }

    private VBox buildTarihListesi() {
        VBox v = new VBox(12);
        v.setPadding(new Insets(20));
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd MMMM yyyy");
        
        List<TakvimEtiketi> kEtiketler = etiketler.stream().filter(e -> e.getKisiId() == kisi.getId()).collect(Collectors.toList());
        for (TakvimEtiketi e : kEtiketler) {
            VBox k = new VBox(5);
            k.setPadding(new Insets(12));
            k.setStyle("-fx-background-color: white; -fx-background-radius: 12; -fx-border-color: #fecfef; -fx-border-width: 0 0 0 5;");
            
            Label t = new Label(e.getTarih().format(fmt));
            t.setFont(Font.font("System", FontWeight.BOLD, 13));
            t.setTextFill(Color.web("#ff9a9e"));
            
            Label m = new Label(e.getMetin());
            k.getChildren().addAll(t, m);
            v.getChildren().add(k);
        }
        return v;
    }
}
