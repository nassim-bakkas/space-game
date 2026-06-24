package com.spacegame;

import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class Explosion {

    private static final Random RAND = new Random();

    private record Particle(Circle circle, double vx, double vy) {}

    private final List<Particle> particles = new ArrayList<>();
    private boolean active = true;

    public Explosion(double cx, double cy, boolean large) {
        int count = large ? 30 : 14;
        double maxSpeed = large ? 6.0 : 3.5;

        for (int i = 0; i < count; i++) {
            double angle = RAND.nextDouble() * Math.PI * 2;
            double speed = 1.0 + RAND.nextDouble() * maxSpeed;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            double radius = large ? 3 + RAND.nextDouble() * 5 : 2 + RAND.nextDouble() * 3;

            Color color = pickColor(RAND.nextDouble());
            Circle c = new Circle(cx, cy, radius, color);
            c.setOpacity(0.9 + RAND.nextDouble() * 0.1);
            particles.add(new Particle(c, vx, vy));
        }
    }

    private Color pickColor(double r) {
        if (r < 0.25) return Color.WHITE;
        if (r < 0.5)  return Color.color(1.0, 0.95, 0.4);
        if (r < 0.75) return Color.ORANGE;
        return Color.color(1.0, 0.25, 0.0);
    }

    public void update() {
        boolean anyAlive = false;
        for (Particle p : particles) {
            p.circle().setCenterX(p.circle().getCenterX() + p.vx());
            p.circle().setCenterY(p.circle().getCenterY() + p.vy());
            p.circle().setOpacity(p.circle().getOpacity() - 0.038);
            p.circle().setRadius(Math.max(0, p.circle().getRadius() - 0.12));
            if (p.circle().getOpacity() > 0) anyAlive = true;
        }
        if (!anyAlive) active = false;
    }

    public boolean isActive() { return active; }

    public void addToPane(Pane pane)    { particles.forEach(p -> pane.getChildren().add(p.circle())); }
    public void removeFromPane(Pane pane) { particles.forEach(p -> pane.getChildren().remove(p.circle())); }
}
