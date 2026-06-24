package com.spacegame;

import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;

public class PowerUp {

    public enum Type { SHIELD, RAPID_FIRE, DOUBLE_SHOT, HEAL }

    private final Type type;
    private double x, y;
    private final double speed = 2.0;
    private boolean active = true;
    private int phase = 0;

    private final Group group;

    public PowerUp(double cx, double cy, Type type) {
        this.type = type;
        this.x = cx - 16;
        this.y = cy;

        Color baseColor = switch (type) {
            case SHIELD      -> Color.color(0.2, 0.6, 1.0);
            case RAPID_FIRE  -> Color.color(1.0, 0.8, 0.0);
            case DOUBLE_SHOT -> Color.color(0.2, 1.0, 0.5);
            case HEAL        -> Color.color(1.0, 0.2, 0.5);
        };
        String symbol = switch (type) {
            case SHIELD      -> "S";
            case RAPID_FIRE  -> "R";
            case DOUBLE_SHOT -> "2";
            case HEAL        -> "+";
        };

        // Hexagon body
        Polygon hex = new Polygon();
        for (int i = 0; i < 6; i++) {
            double a = Math.PI / 6 + i * Math.PI / 3;
            hex.getPoints().addAll(16 + 14 * Math.cos(a), 16 + 14 * Math.sin(a));
        }
        hex.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.65, true, CycleMethod.NO_CYCLE,
            new Stop(0, baseColor.brighter()), new Stop(1, baseColor.darker().darker())));
        hex.setStroke(baseColor.brighter()); hex.setStrokeWidth(2.0);

        Circle glow = new Circle(16, 16, 18, Color.color(
            baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), 0.25));

        Text label = new Text(symbol);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        label.setFill(Color.WHITE);
        label.setX(16 - label.getLayoutBounds().getWidth() / 2 - 1);
        label.setY(21);

        group = new Group(glow, hex, label);
        group.setEffect(new Glow(0.7));
        group.setLayoutX(x); group.setLayoutY(y);
    }

    public void update() {
        y += speed;
        phase++;
        // Bob and rotate
        group.setLayoutX(x + Math.sin(phase * 0.07) * 4);
        group.setLayoutY(y);
        group.setRotate(phase * 1.5);
        if (y > MainApp.HEIGHT + 30) active = false;
    }

    public boolean isActive()      { return active; }
    public void deactivate()       { active = false; }
    public Type getType()          { return type; }

    public void addToPane(Pane p)      { p.getChildren().add(group); }
    public void removeFromPane(Pane p) { p.getChildren().remove(group); }

    public javafx.geometry.Bounds getBounds() { return group.getBoundsInParent(); }
}
