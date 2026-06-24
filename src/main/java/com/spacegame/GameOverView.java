package com.spacegame;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Scene;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.util.Duration;

public class GameOverView {

    private final Pane root = new Pane();
    private final Scene scene;

    public GameOverView(int score, int level, boolean victory) {
        root.setPrefSize(MainApp.WIDTH, MainApp.HEIGHT);
        root.setStyle("-fx-background-color: #000010;");

        StarField stars = new StarField(root);

        // Background overlay
        Color overlayColor = victory
            ? Color.color(0.0, 0.05, 0.2, 0.6)
            : Color.color(0.15, 0.0, 0.0, 0.65);
        Rectangle overlay = new Rectangle(0, 0, MainApp.WIDTH, MainApp.HEIGHT);
        overlay.setFill(overlayColor);
        root.getChildren().add(overlay);

        // Main message
        String headline = victory ? "VICTOIRE !" : "GAME OVER";
        Color headlineColor = victory
            ? Color.color(0.3, 1.0, 0.5)
            : Color.color(1.0, 0.25, 0.25);

        Text headlineText = new Text(headline);
        headlineText.setFont(Font.font("Arial", FontWeight.BOLD, 72));
        headlineText.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, headlineColor),
            new Stop(0.5, headlineColor.brighter()),
            new Stop(1, headlineColor)));
        headlineText.setEffect(new Glow(1.0));
        double hw = headlineText.getLayoutBounds().getWidth();
        headlineText.setX(MainApp.WIDTH / 2.0 - hw / 2);
        headlineText.setY(210);

        // Sub-message
        String sub = victory
            ? "Vous avez sauvé la Terre !"
            : "La Terre est tombée aux mains des extraterrestres...";
        Text subText = new Text(sub);
        subText.setFont(Font.font("Arial", FontWeight.NORMAL, 18));
        subText.setFill(Color.color(0.7, 0.85, 1.0));
        subText.setX(MainApp.WIDTH / 2.0 - subText.getLayoutBounds().getWidth() / 2);
        subText.setY(260);

        // Stats box
        Rectangle statsBg = new Rectangle(MainApp.WIDTH / 2.0 - 130, 285, 260, 90);
        statsBg.setFill(Color.color(0.05, 0.1, 0.25, 0.7));
        statsBg.setStroke(Color.color(0.3, 0.6, 1.0, 0.5)); statsBg.setStrokeWidth(1.5);
        statsBg.setArcWidth(12); statsBg.setArcHeight(12);

        Text scoreText = new Text("Score Final : " + score);
        scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        scoreText.setFill(Color.YELLOW);
        scoreText.setX(MainApp.WIDTH / 2.0 - scoreText.getLayoutBounds().getWidth() / 2);
        scoreText.setY(318);

        Text levelText = new Text("Niveau atteint : " + level + " / 5");
        levelText.setFont(Font.font("Arial", 16));
        levelText.setFill(Color.color(0.7, 0.85, 1.0));
        levelText.setX(MainApp.WIDTH / 2.0 - levelText.getLayoutBounds().getWidth() / 2);
        levelText.setY(346);

        // Buttons
        Rectangle restartBg = makeButton(280, 410, 160, 48, Color.color(0.15, 0.45, 0.9));
        Text restartText = buttonLabel("REJOUER", 318, 443);
        restartBg.setOnMouseClicked(e -> MainApp.startNewGame());
        restartText.setOnMouseClicked(e -> MainApp.startNewGame());
        restartBg.setOnMouseEntered(e -> { restartBg.setFill(Color.color(0.25, 0.6, 1.0)); root.setCursor(javafx.scene.Cursor.HAND); });
        restartBg.setOnMouseExited(e  -> { restartBg.setFill(Color.color(0.15, 0.45, 0.9)); root.setCursor(javafx.scene.Cursor.DEFAULT); });

        Rectangle menuBg = makeButton(460, 410, 160, 48, Color.color(0.2, 0.2, 0.2));
        Text menuText = buttonLabel("MENU", 510, 443);
        menuBg.setOnMouseClicked(e -> MainApp.showMenu());
        menuText.setOnMouseClicked(e -> MainApp.showMenu());
        menuBg.setOnMouseEntered(e -> { menuBg.setFill(Color.color(0.35, 0.35, 0.35)); root.setCursor(javafx.scene.Cursor.HAND); });
        menuBg.setOnMouseExited(e  -> { menuBg.setFill(Color.color(0.2, 0.2, 0.2)); root.setCursor(javafx.scene.Cursor.DEFAULT); });

        root.getChildren().addAll(headlineText, subText, statsBg, scoreText, levelText, restartText, menuText);

        // Pulse headline
        ScaleTransition pulse = new ScaleTransition(Duration.seconds(1.5), headlineText);
        pulse.setFromX(1.0); pulse.setToX(1.05);
        pulse.setFromY(1.0); pulse.setToY(1.05);
        pulse.setAutoReverse(true); pulse.setCycleCount(-1);
        pulse.play();

        FadeTransition fadeIn = new FadeTransition(Duration.millis(700), root);
        fadeIn.setFromValue(0); fadeIn.setToValue(1); fadeIn.play();

        new AnimationTimer() {
            @Override public void handle(long now) { stars.update(); }
        }.start();

        scene = new Scene(root, MainApp.WIDTH, MainApp.HEIGHT);
    }

    private Rectangle makeButton(double x, double y, double w, double h, Color fill) {
        Rectangle r = new Rectangle(x, y, w, h);
        r.setFill(fill); r.setArcWidth(12); r.setArcHeight(12);
        r.setStroke(fill.brighter()); r.setStrokeWidth(1.5);
        r.setEffect(new Glow(0.4));
        root.getChildren().add(r);
        return r;
    }

    private Text buttonLabel(String txt, double x, double y) {
        Text t = new Text(txt);
        t.setFont(Font.font("Arial", FontWeight.BOLD, 18));
        t.setFill(Color.WHITE);
        t.setX(x); t.setY(y);
        return t;
    }

    public Scene getScene() { return scene; }
}
