package com.spacegame;

import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Bomb {

    private static final Random RAND = new Random();

    private double x, y;
    private final double speedX, speedY;
    private boolean active = true;

    private final Group group;
    private final List<Circle> trail = new ArrayList<>();
    private int trailTimer = 0;

    public Bomb(double cx, double fromY) {
        this(cx, fromY, 0);
    }

    public Bomb(double cx, double fromY, double angleOffset) {
        this.x = cx;
        this.y = fromY;
        // angleOffset allows spray bombs from boss
        double angle = Math.PI / 2 + angleOffset; // base: straight down
        double speed  = 4.5 + RAND.nextDouble() * 1.5;
        this.speedX = Math.cos(angle) * speed * 0.35;
        this.speedY = Math.sin(angle) * speed;

        Circle core = new Circle(8,
            new RadialGradient(0, 0, 0.4, 0.35, 0.65, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE),
                new Stop(0.3, Color.ORANGE),
                new Stop(1, Color.color(0.7, 0.0, 0.0))));
        core.setStroke(Color.color(1.0, 0.5, 0.0)); core.setStrokeWidth(1.5);

        Circle outerGlow = new Circle(12, Color.color(1.0, 0.3, 0.0, 0.25));

        group = new Group(outerGlow, core);
        group.setEffect(new Glow(0.7));
        group.setLayoutX(x); group.setLayoutY(y);
    }

    public void update(Pane pane) {
        x += speedX;
        y += speedY;
        group.setLayoutX(x); group.setLayoutY(y);

        // Spawn trail particle every 2 frames
        trailTimer++;
        if (trailTimer % 2 == 0) {
            Circle t = new Circle(x + 8, y + 8, 4 + RAND.nextDouble() * 2,
                Color.color(1.0, 0.4 + RAND.nextDouble() * 0.3, 0.0, 0.6));
            t.setEffect(new Glow(0.5));
            trail.add(t);
            pane.getChildren().add(t);
        }

        // Fade and remove old trail particles
        trail.removeIf(t -> {
            t.setOpacity(t.getOpacity() - 0.08);
            t.setRadius(t.getRadius() - 0.3);
            if (t.getOpacity() <= 0 || t.getRadius() <= 0) {
                pane.getChildren().remove(t);
                return true;
            }
            return false;
        });

        if (y > MainApp.HEIGHT + 20) active = false;
    }

    public void cleanup(Pane pane) {
        trail.forEach(t -> pane.getChildren().remove(t));
        trail.clear();
    }

    public boolean isActive()      { return active; }
    public void deactivate()       { active = false; }

    public void addToPane(Pane p)      { p.getChildren().add(group); }
    public void removeFromPane(Pane p) { p.getChildren().remove(group); }

    public javafx.geometry.Bounds getBounds() { return group.getBoundsInParent(); }
}
