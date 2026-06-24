package com.spacegame;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class StarField {

    private static final Random RAND = new Random();

    private record Star(Circle circle, double speed) {}

    private final List<Star> stars = new ArrayList<>();
    private final Pane pane;

    public StarField(Pane pane) {
        this.pane = pane;
        // Layer 1: distant small slow stars
        for (int i = 0; i < 100; i++) addStar(0.5 + RAND.nextDouble() * 0.8, 0.3 + RAND.nextDouble() * 0.4);
        // Layer 2: mid stars
        for (int i = 0; i < 50; i++)  addStar(0.9 + RAND.nextDouble() * 0.8, 0.6 + RAND.nextDouble() * 0.5);
        // Layer 3: bright close stars
        for (int i = 0; i < 20; i++)  addStar(1.4 + RAND.nextDouble(),        1.0 + RAND.nextDouble() * 0.8);
    }

    private void addStar(double radius, double speed) {
        double x = RAND.nextDouble() * MainApp.WIDTH;
        double y = RAND.nextDouble() * MainApp.HEIGHT;
        double brightness = 0.4 + speed * 0.3;
        Circle c = new Circle(x, y, radius, Color.color(brightness, brightness, Math.min(1.0, brightness + 0.15)));
        c.setOpacity(0.5 + RAND.nextDouble() * 0.5);
        pane.getChildren().add(c);
        stars.add(new Star(c, speed));
    }

    public void update() {
        for (Star s : stars) {
            double ny = s.circle().getCenterY() + s.speed();
            if (ny > MainApp.HEIGHT) {
                s.circle().setCenterY(0);
                s.circle().setCenterX(RAND.nextDouble() * MainApp.WIDTH);
            } else {
                s.circle().setCenterY(ny);
            }
        }
    }
}
