package com.hediye;

import com.hediye.database.VeritabaniYoneticisi;
import com.hediye.service.VeriYukleyici;
import com.hediye.ui.AnaSayfaEkrani;
import com.hediye.ui.GirisEkrani;
import com.hediye.ui.HediyeMotorEkrani;
import com.hediye.ui.PerformansEkrani;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

/**
 * Akıllı Hediye Öneri Sistemi'nin ana giriş noktasıdır.
 * Uygulamanın yaşam döngüsünü, veri yükleme süreçlerini ve
 * ana ekran geçişlerini yönetir.
 */
public class Main extends Application {

    private static final String GRAD_PEARL = "linear-gradient(to bottom right, #ffffff, #f0f4f7, #e0eafc)";

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Akıllı Hediye Öneri Sistemi");

        // 1. Veri seti yüklenirken kullanıcıyı bilgilendiren yükleme ekranını gösterir
        Stage yuklemeStage = gosterYuklemeEkrani(primaryStage);
        // Veri yükleme işlemini UI thread'ini bloke etmemek için ayrı bir thread'de başlat
        Thread yukleThread = new Thread(() -> {
            try {
                VeriYukleyici.getOrnek().yukle();
                Platform.runLater(() -> {
                    yuklemeStage.close();

                    // 2. Giriş ekranı
                    new GirisEkrani(primaryStage, kullaniciId -> {
                        String kullaniciAd = VeritabaniYoneticisi.getOrnek().kullaniciAdiGetir(kullaniciId);
                        gosterAnaEkran(primaryStage, kullaniciId, kullaniciAd);
                    });
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    yuklemeStage.close();
                    Platform.exit();
                });
            }
        });
        yukleThread.setDaemon(true);
        yukleThread.start();
    }
    /**
     * Kullanıcının hediye önerileri alabildiği ve profilini yönettiği ana sekmeli arayüzü hazırlar.
     *
     * @param stage Uygulamanın ana penceresi
     * @param kullaniciId Giriş yapan kullanıcının ID'si
     * @param kullaniciAd Giriş yapan kullanıcının adı
     */
    private void gosterAnaEkran(Stage stage, int kullaniciId, String kullaniciAd) {
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabPane.setStyle("-fx-background-color: " + GRAD_PEARL + ";");

        AnaSayfaEkrani anaSayfa = new AnaSayfaEkrani(kullaniciId, kullaniciAd);
        Tab anaSayfaTab = new Tab("Ana Sayfa", anaSayfa);

        HediyeMotorEkrani hediyeMotor = new HediyeMotorEkrani(kullaniciId);
        hediyeMotor.setKisiler(anaSayfa.getKisiler());
        hediyeMotor.setAnaSayfaYenileCallback(() -> {
            anaSayfa.yenile();
            hediyeMotor.setKisiler(anaSayfa.getKisiler());
        });
        Tab hediyeTab = new Tab("Hediye Motoru", hediyeMotor);
        // Hediye motorunda bir değişiklik (ekleme/silme) olursa ana sayfayı yenileyen callback
        anaSayfa.setHediyeMotorCallback(() -> tabPane.getSelectionModel().select(hediyeTab));

        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, eski, yeni) -> {
            if (yeni == hediyeTab) {
                hediyeMotor.setKisiler(anaSayfa.getKisiler());
            }
        });

        Tab performansTab = new Tab("Performans Analizi", new PerformansEkrani());

        tabPane.getTabs().addAll(anaSayfaTab, hediyeTab, performansTab);

        BorderPane root = new BorderPane(tabPane);
        Scene scene = new Scene(root, 1280, 800);
        stage.setScene(scene);
        stage.setTitle("Akıllı Hediye Öneri Sistemi — " + kullaniciAd);
        stage.show();

        stage.setOnCloseRequest(e -> {
            VeritabaniYoneticisi.getOrnek().baglantiKapat();
            Platform.exit();
        });
    }

    private Stage gosterYuklemeEkrani(Stage owner) {
        Stage yukleme = new Stage();
        yukleme.initOwner(owner);
        yukleme.setResizable(false);
        yukleme.setTitle("Hazırlanıyor...");

        StackPane root = new StackPane();
        root.setStyle("-fx-background-color: white;");
        root.setPrefSize(400, 220);

        VBox kutu = new VBox(20);
        kutu.setAlignment(javafx.geometry.Pos.CENTER);
        kutu.setPadding(new Insets(30));

        Text icon = new Text("🎁");
        icon.setFont(Font.font(56));

        Text mesaj = new Text("Veri seti hazırlanıyor...\nLütfen bekleyin.");
        mesaj.setFont(Font.font("System", FontWeight.BOLD, 16));
        mesaj.setFill(Color.web("#764ba2"));
        mesaj.setTextAlignment(javafx.scene.text.TextAlignment.CENTER);

        javafx.scene.control.ProgressIndicator pi = new javafx.scene.control.ProgressIndicator();
        pi.setStyle("-fx-progress-color: #ff9a9e;");

        kutu.getChildren().addAll(icon, mesaj, pi);
        root.getChildren().add(kutu);

        yukleme.setScene(new Scene(root, 400, 220));
        yukleme.show();
        return yukleme;
    }

    @Override
    public void stop() {
        VeritabaniYoneticisi.getOrnek().baglantiKapat();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
