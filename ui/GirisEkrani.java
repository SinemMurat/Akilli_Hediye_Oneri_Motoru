package com.hediye.ui;

import com.hediye.database.VeritabaniYoneticisi;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.*;
import javafx.stage.Stage;

import java.util.function.Consumer;

/**
 * Giriş ve Kayıt Ekranı
 */
public class GirisEkrani {

    private final Stage stage;
    private final VeritabaniYoneticisi db;
    private Consumer<Integer> loginSuccessCallback;

    private static final String GRAD_PEARL = "linear-gradient(to bottom right, #ffffff, #f0f4f7, #e0eafc)";
    private static final String GRAD_VOGUE_PINK = "linear-gradient(to right, #ff9a9e, #fecfef)";

    public GirisEkrani(Stage stage, Consumer<Integer> onLoginSuccess) {
        this.stage = stage;
        this.loginSuccessCallback = onLoginSuccess;
        this.db = VeritabaniYoneticisi.getOrnek();
        gosterGiris();
    }

    public void gosterGiris() {
        VBox root = new VBox(25);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + GRAD_PEARL + ";");
        root.setPadding(new Insets(40));

        VBox card = new VBox(20);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(35));
        card.setMaxWidth(400);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 25; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 20, 0, 0, 10);");

        Label logo = new Label("🎁");
        logo.setFont(Font.font(64));

        Label title = new Label("Akıllı Hediye Öneri Motoru");
        title.setFont(Font.font("System", FontWeight.BOLD, 28));
        title.setTextFill(Color.web("#764ba2"));

        TextField uField = modernInput("Kullanıcı Adı");
        PasswordField pField = modernPass("Şifre");

        Button loginBtn = new Button("Giriş Yap");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        loginBtn.setStyle("-fx-background-color: " + GRAD_VOGUE_PINK + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 15; -fx-cursor: hand;");
        loginBtn.setOnAction(e -> {
            int[] res = db.girisYap(uField.getText(), pField.getText());
            if (res[0] != -1) {
                if (loginSuccessCallback != null) loginSuccessCallback.accept(res[0]);
            } else {
                error("Kullanıcı adı veya şifre hatalı!");
            }
        });

        Hyperlink reg = new Hyperlink("Yeni Hesap Oluştur");
        reg.setOnAction(e -> gosterKayit());

        card.getChildren().addAll(logo, title, uField, pField, loginBtn, reg);
        root.getChildren().add(card);

        Scene sc = new Scene(root, 450, 650);
        stage.setScene(sc);
        stage.show();
    }

    public void gosterKayit() {
        VBox root = new VBox(20);
        root.setAlignment(Pos.CENTER);
        root.setStyle("-fx-background-color: " + GRAD_PEARL + ";");
        root.setPadding(new Insets(40));

        VBox card = new VBox(15);
        card.setPadding(new Insets(35));
        card.setMaxWidth(400);
        card.setStyle("-fx-background-color: white; -fx-background-radius: 25;");

        Label t = new Label("Kayıt Ol");
        t.setFont(Font.font("System", FontWeight.BOLD, 24));

        TextField n = modernInput("Ad Soyad");
        TextField u = modernInput("Kullanıcı Adı");
        PasswordField p = modernPass("Şifre");

        Button rBtn = new Button("Kayıt Ol");
        rBtn.setMaxWidth(Double.MAX_VALUE);
        rBtn.setStyle("-fx-background-color: #a1c4fd; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 12; -fx-background-radius: 15; -fx-cursor: hand;");
        rBtn.setOnAction(e -> {
            if (db.kullaniciKaydet(u.getText(), p.getText(), n.getText())) {
                gosterGiris();
            } else {
                error("Bu kullanıcı adı alınmış!");
            }
        });

        Button b = new Button("Geri Dön");
        b.setOnAction(e -> gosterGiris());

        card.getChildren().addAll(t, n, u, p, rBtn, b);
        root.getChildren().add(card);
        stage.getScene().setRoot(root);
    }

    private TextField modernInput(String p) {
        TextField f = new TextField();
        f.setPromptText(p);
        f.setStyle("-fx-background-color: #f0f4f7; -fx-background-radius: 10; -fx-padding: 12;");
        return f;
    }

    private PasswordField modernPass(String p) {
        PasswordField f = new PasswordField();
        f.setPromptText(p);
        f.setStyle("-fx-background-color: #f0f4f7; -fx-background-radius: 10; -fx-padding: 12;");
        return f;
    }

    private void error(String m) {
        Alert a = new Alert(Alert.AlertType.ERROR, m);
        a.showAndWait();
    }
}
