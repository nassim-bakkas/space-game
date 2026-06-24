package com.spacegame;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    public static final int WIDTH  = 900;
    public static final int HEIGHT = 650;

    private static Stage primaryStage;

    @Override
    public void start(Stage stage) {
        primaryStage = stage;
        stage.setTitle("Jeu de l'Espace");
        stage.setResizable(false);
        showMenu();
        stage.show();
    }

    public static void showMenu() {
        MenuView menu = new MenuView();
        applyScene(menu.getScene());
    }

    public static void startNewGame() {
        GameView game = new GameView();
        applyScene(game.getScene());
        game.start();
    }

    public static void showGameOver(int score, int level, boolean victory) {
        GameOverView gov = new GameOverView(score, level, victory);
        applyScene(gov.getScene());
    }

    private static void applyScene(Scene scene) {
        scene.getStylesheets().add(
            MainApp.class.getResource("/com/spacegame/style.css").toExternalForm());
        primaryStage.setScene(scene);
    }

    public static void main(String[] args) { launch(args); }
}
