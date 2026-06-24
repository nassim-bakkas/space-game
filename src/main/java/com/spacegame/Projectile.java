package com.spacegame;

import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Rectangle;

public class Projectile {

    private double x, y;
    private final double speedY;
    private final double offsetX; // for double-shot spread
    private boolean active = true;

    private final Group group;

    public Projectile(double cx, double topY, double offsetX) {
        this.offsetX = offsetX;
        this.x = cx - 4 + offsetX;
        this.y = topY - 4;
        this.speedY = 12.0;

        // Laser beam body
        Rectangle beam = new Rectangle(8, 22);
        beam.setFill(new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.WHITE),
            new Stop(0.3, Color.color(0.4, 0.9, 1.0)),
            new Stop(1, Color.color(0.1, 0.5, 1.0, 0.0))));
        beam.setArcWidth(6); beam.setArcHeight(6);

        // Glow halo
        Ellipse halo = new Ellipse(4, 4, 7, 5);
        halo.setFill(Color.color(0.3, 0.7, 1.0, 0.35));

        group = new Group(halo, beam);
        group.setEffect(new Glow(0.9));
        group.setLayoutX(x);
        group.setLayoutY(y);
    }

    public void update() {
        y -= speedY;
        group.setLayoutY(y);
        if (y < -30) active = false;
    }

    public boolean isActive()    { return active; }
    public void deactivate()     { active = false; }

    public void addToPane(Pane p)   { p.getChildren().add(group); }
    public void removeFromPane(Pane p) { p.getChildren().remove(group); }

    public javafx.geometry.Bounds getBounds() { return group.getBoundsInParent(); }
}
