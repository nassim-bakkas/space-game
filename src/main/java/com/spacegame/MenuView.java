package com.spacegame;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.util.Duration;

public class MenuView {

    private final Pane root = new Pane();
    private final Scene scene;

    public MenuView() {
        root.setPrefSize(MainApp.WIDTH, MainApp.HEIGHT);
        root.setStyle("-fx-background-color: #000010;");

        StarField stars = new StarField(root);

        // Nebula background blobs
        addNebula(200, 150, 180, Color.color(0.05, 0.0, 0.2, 0.35));
        addNebula(650, 300, 160, Color.color(0.0, 0.05, 0.25, 0.3));
        addNebula(400, 450, 200, Color.color(0.1, 0.0, 0.15, 0.25));

        // Title
        Text title = new Text("JEU DE L'ESPACE");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        title.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0.3, 0.8, 1.0)),
            new Stop(0.5, Color.WHITE),
            new Stop(1, Color.color(0.3, 0.8, 1.0))));
        title.setEffect(new Glow(0.8));
        title.setTextAlignment(TextAlignment.CENTER);
        title.setX(MainApp.WIDTH / 2.0 - 285);
        title.setY(160);

        Text subtitle = new Text("Défendez la Terre contre l'invasion extraterrestre !");
        subtitle.setFont(Font.font("Arial", FontWeight.NORMAL, 16));
        subtitle.setFill(Color.color(0.6, 0.8, 1.0, 0.85));
        subtitle.setX(MainApp.WIDTH / 2.0 - 215);
        subtitle.setY(200);

        // Play button
        Rectangle playBg = makeButton(350, 265, 200, 50, Color.color(0.15, 0.45, 0.9));
        Text playText = new Text("JOUER");
        playText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        playText.setFill(Color.WHITE);
        playText.setX(430); playText.setY(298);

        playBg.setOnMouseEntered(e -> { playBg.setFill(Color.color(0.25, 0.6, 1.0)); root.setCursor(javafx.scene.Cursor.HAND); });
        playBg.setOnMouseExited(e  -> { playBg.setFill(Color.color(0.15, 0.45, 0.9)); root.setCursor(javafx.scene.Cursor.DEFAULT); });
        playBg.setOnMouseClicked(e -> MainApp.startNewGame());
        playText.setOnMouseClicked(e -> MainApp.startNewGame());

        // Controls info box
        Text controlsTitle = new Text("CONTROLES");
        controlsTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        controlsTitle.setFill(Color.color(0.6, 0.9, 1.0));
        controlsTitle.setX(310); controlsTitle.setY(375);

        String[] controls = {
            "← → ou A/D     Déplacer",
            "↑ ↓ ou W/S     Monter / Descendre",
            "ESPACE          Tirer",
            "P               Pause"
        };
        for (int i = 0; i < controls.length; i++) {
            Text t = new Text(controls[i]);
            t.setFont(Font.font("Courier New", 13));
            t.setFill(Color.color(0.75, 0.85, 1.0, 0.85));
            t.setX(290); t.setY(400 + i * 22);
            root.getChildren().add(t);
        }

        // Power-ups legend
        Text puTitle = new Text("BONUS");
        puTitle.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        puTitle.setFill(Color.color(0.6, 0.9, 1.0));
        puTitle.setX(610); puTitle.setY(375);

        String[] powerUps = { "S  Bouclier", "R  Tir rapide", "2  Double tir", "+  Soin" };
        Color[] puColors = { Color.color(0.2, 0.6, 1.0), Color.YELLOW, Color.color(0.2, 1.0, 0.5), Color.color(1.0, 0.2, 0.5) };
        for (int i = 0; i < powerUps.length; i++) {
            Text t = new Text(powerUps[i]);
            t.setFont(Font.font("Courier New", 13));
            t.setFill(puColors[i]);
            t.setX(600); t.setY(400 + i * 22);
            root.getChildren().add(t);
        }

        // Footer
        Text footer = new Text("5 niveaux  •  4 types d'ennemis  •  1 Boss final");
        footer.setFont(Font.font("Arial", 13));
        footer.setFill(Color.color(0.5, 0.6, 0.7, 0.7));
        footer.setX(MainApp.WIDTH / 2.0 - 175);
        footer.setY(MainApp.HEIGHT - 25);

        root.getChildren().addAll(title, subtitle, playText,
            controlsTitle, puTitle, footer);

        // Pulsing glow on title
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(2), title);
        pulse.setFromX(1.0); pulse.setToX(1.04);
        pulse.setFromY(1.0); pulse.setToY(1.04);
        pulse.setAutoReverse(true); pulse.setCycleCount(-1);
        pulse.play();

        // Fade in
        FadeTransition fadeIn = new FadeTransition(Duration.millis(800), root);
        fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();

        new AnimationTimer() {
            @Override public void handle(long now) { stars.update(); }
        }.start();

        scene = new Scene(root, MainApp.WIDTH, MainApp.HEIGHT);
    }

    private void addNebula(double cx, double cy, double r, Color color) {
        javafx.scene.shape.Circle nebula = new javafx.scene.shape.Circle(cx, cy, r, color);
        nebula.setEffect(new javafx.scene.effect.GaussianBlur(r * 0.8));
        root.getChildren().add(nebula);
    }

    private Rectangle makeButton(double x, double y, double w, double h, Color fill) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(fill);
        r.setArcWidth(12); r.setArcHeight(12);
        r.setStroke(fill.brighter()); r.setStrokeWidth(1.5);
        r.setEffect(new Glow(0.4));
        root.getChildren().add(r);
        return r;
    }

    public Scene getScene() { return scene; }
}
