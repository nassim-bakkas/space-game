package com.spacegame;

import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

import java.util.Random;

public class Enemy {

    public enum Type { SCOUT, WARRIOR, ARMORED, COMMANDER, BOSS }

    private static final Random RAND = new Random();

    private double x, y;
    private double speedX, speedY;
    private final Type type;
    private int hp;
    private int maxHp;
    private int bombTimer = 0;
    private final int bombInterval;
    private boolean alive = true;

    // Boss-specific
    private double bossAngle = 0;
    private int phase = 0; // boss attack phases

    // Dimensions
    private final double W, H;

    private final Group group;
    private final Group hpIndicator; // small dots above enemy showing remaining HP

    public Enemy(double x, double y, int level, Type type) {
        this.x = x;
        this.y = y;
        this.type = type;

        switch (type) {
            case SCOUT    -> { W = 48; H = 28; hp = 1; maxHp = 1; bombInterval = Math.max(80, 200 - level * 15); }
            case WARRIOR  -> { W = 52; H = 32; hp = 1; maxHp = 1; bombInterval = Math.max(60, 160 - level * 15); }
            case ARMORED  -> { W = 54; H = 34; hp = 2; maxHp = 2; bombInterval = Math.max(70, 180 - level * 10); }
            case COMMANDER-> { W = 60; H = 40; hp = 3; maxHp = 3; bombInterval = Math.max(50, 140 - level * 10); }
            default       -> { W = 120; H = 70; hp = 20; maxHp = 20; bombInterval = 30; } // BOSS
        }

        double baseSpeed = 1.2 + level * 0.4;
        switch (type) {
            case SCOUT     -> { speedX = (RAND.nextBoolean() ? 1 : -1) * (baseSpeed + RAND.nextDouble() * 0.8); speedY = 0.25 + RAND.nextDouble() * 0.3; }
            case WARRIOR   -> { speedX = (RAND.nextBoolean() ? 1 : -1) * (baseSpeed * 1.3 + RAND.nextDouble()); speedY = 0.4 + RAND.nextDouble() * 0.4; }
            case ARMORED   -> { speedX = (RAND.nextBoolean() ? 1 : -1) * (baseSpeed * 0.9);                     speedY = 0.15 + RAND.nextDouble() * 0.2; }
            case COMMANDER -> { speedX = (RAND.nextBoolean() ? 1 : -1) * (baseSpeed * 1.6);                     speedY = 0.5 + RAND.nextDouble() * 0.5; }
            default        -> { speedX = 1.5; speedY = 0; }
        }

        group = buildShape(type);
        hpIndicator = buildHpIndicator();
        updatePosition();
    }

    private Group buildShape(Type type) {
        return switch (type) {
            case SCOUT    -> buildUFO(W, H, Color.LIMEGREEN,    Color.DARKGREEN,  Color.color(0.5, 1.0, 0.5));
            case WARRIOR  -> buildUFO(W, H, Color.ORANGE,       Color.DARKORANGE, Color.color(1.0, 0.8, 0.2));
            case ARMORED  -> buildArmored(W, H);
            case COMMANDER-> buildCommander(W, H);
            default       -> buildBoss(W, H);
        };
    }

    private Group buildUFO(double w, double h, Color body, Color bodyStroke, Color domeColor) {
        Ellipse bodyEllipse = new Ellipse(w / 2, h * 0.65, w / 2, h * 0.38);
        bodyEllipse.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.6, true, CycleMethod.NO_CYCLE,
            new Stop(0, body.brighter()), new Stop(1, body.darker())));
        bodyEllipse.setStroke(bodyStroke);
        bodyEllipse.setStrokeWidth(1.5);

        Ellipse dome = new Ellipse(w / 2, h * 0.38, w * 0.28, h * 0.42);
        dome.setFill(new RadialGradient(0, 0, 0.4, 0.3, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0.85, 1.0, 1.0, 0.95)), new Stop(1, domeColor.deriveColor(0, 1, 1, 0.5))));
        dome.setStroke(domeColor);
        dome.setStrokeWidth(1.2);

        // Lights under body
        Group lights = new Group();
        for (int i = 0; i < 5; i++) {
            Circle light = new Circle(w * 0.18 + i * w * 0.16, h * 0.75, 3,
                i % 2 == 0 ? Color.YELLOW : Color.color(1.0, 0.3, 0.0));
            light.setEffect(new Glow(0.8));
            lights.getChildren().add(light);
        }

        Group g = new Group(bodyEllipse, dome, lights);
        g.setEffect(new Glow(0.25));
        return g;
    }

    private Group buildArmored(double w, double h) {
        Ellipse body = new Ellipse(w / 2, h * 0.65, w / 2, h * 0.38);
        body.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.6, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0.8, 0.15, 0.15)), new Stop(1, Color.color(0.4, 0.05, 0.05))));
        body.setStroke(Color.color(1.0, 0.3, 0.3));
        body.setStrokeWidth(2.0);

        // Armor plates
        Rectangle plateL = new Rectangle(w * 0.05, h * 0.45, w * 0.3, h * 0.35);
        plateL.setFill(Color.color(0.5, 0.1, 0.1, 0.7)); plateL.setArcWidth(4); plateL.setArcHeight(4);
        Rectangle plateR = new Rectangle(w * 0.65, h * 0.45, w * 0.3, h * 0.35);
        plateR.setFill(Color.color(0.5, 0.1, 0.1, 0.7)); plateR.setArcWidth(4); plateR.setArcHeight(4);

        Ellipse dome = new Ellipse(w / 2, h * 0.38, w * 0.22, h * 0.38);
        dome.setFill(Color.color(0.9, 0.5, 0.1, 0.85));
        dome.setStroke(Color.ORANGE); dome.setStrokeWidth(1.5);

        Group g = new Group(body, plateL, plateR, dome);
        g.setEffect(new Glow(0.3));
        return g;
    }

    private Group buildCommander(double w, double h) {
        // Diamond/angular shape
        Polygon body = new Polygon(
            w / 2, 0.0,
            w,     h * 0.45,
            w * 0.75, h,
            w * 0.25, h,
            0.0,   h * 0.45
        );
        body.setFill(new RadialGradient(0, 0, 0.5, 0.4, 0.6, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0.7, 0.2, 1.0)), new Stop(1, Color.color(0.3, 0.05, 0.5))));
        body.setStroke(Color.color(0.9, 0.5, 1.0)); body.setStrokeWidth(2.0);

        Circle core = new Circle(w / 2, h * 0.48, w * 0.16,
            new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, Color.WHITE), new Stop(1, Color.color(0.8, 0.4, 1.0))));
        core.setEffect(new Glow(1.0));

        Group g = new Group(body, core);
        g.setEffect(new Glow(0.4));
        return g;
    }

    private Group buildBoss(double w, double h) {
        Ellipse mainBody = new Ellipse(w / 2, h * 0.6, w / 2, h * 0.42);
        mainBody.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.6, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0.9, 0.1, 0.1)), new Stop(0.6, Color.color(0.5, 0.0, 0.3)), new Stop(1, Color.color(0.2, 0.0, 0.1))));
        mainBody.setStroke(Color.color(1.0, 0.3, 0.3)); mainBody.setStrokeWidth(3.0);

        Ellipse topDome = new Ellipse(w / 2, h * 0.35, w * 0.32, h * 0.38);
        topDome.setFill(new RadialGradient(0, 0, 0.4, 0.3, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(1.0, 0.6, 0.0, 0.9)), new Stop(1, Color.color(0.6, 0.1, 0.0, 0.5))));
        topDome.setStroke(Color.ORANGE); topDome.setStrokeWidth(2.0);

        // Wing spikes
        Polygon leftSpike = new Polygon(0.0, h * 0.5, w * 0.1, h * 0.3, w * 0.3, h * 0.55, w * 0.1, h * 0.9);
        leftSpike.setFill(Color.color(0.8, 0.0, 0.2, 0.8));
        Polygon rightSpike = new Polygon(w, h * 0.5, w * 0.9, h * 0.3, w * 0.7, h * 0.55, w * 0.9, h * 0.9);
        rightSpike.setFill(Color.color(0.8, 0.0, 0.2, 0.8));

        // Cannon barrels
        Rectangle cannonL = new Rectangle(w * 0.25, h * 0.82, 10, 22);
        cannonL.setFill(Color.DARKGRAY); cannonL.setArcWidth(3); cannonL.setArcHeight(3);
        Rectangle cannonC = new Rectangle(w / 2 - 5, h * 0.88, 10, 26);
        cannonC.setFill(Color.DARKGRAY); cannonC.setArcWidth(3); cannonC.setArcHeight(3);
        Rectangle cannonR = new Rectangle(w * 0.65, h * 0.82, 10, 22);
        cannonR.setFill(Color.DARKGRAY); cannonR.setArcWidth(3); cannonR.setArcHeight(3);

        Circle eye = new Circle(w / 2, h * 0.42, w * 0.1, Color.color(1.0, 0.0, 0.0));
        eye.setEffect(new Glow(1.2));

        Group g = new Group(leftSpike, rightSpike, mainBody, topDome, cannonL, cannonC, cannonR, eye);
        g.setEffect(new Glow(0.5));
        return g;
    }

    private Group buildHpIndicator() {
        Group g = new Group();
        for (int i = 0; i < maxHp; i++) {
            Circle dot = new Circle(i * 10, -10, 4, Color.LIMEGREEN);
            dot.setStroke(Color.WHITE); dot.setStrokeWidth(0.5);
            g.getChildren().add(dot);
        }
        if (maxHp <= 1) g.setVisible(false);
        return g;
    }

    private void refreshHpDots() {
        for (int i = 0; i < hpIndicator.getChildren().size(); i++) {
            Circle dot = (Circle) hpIndicator.getChildren().get(i);
            dot.setFill(i < hp ? Color.LIMEGREEN : Color.DARKRED);
        }
    }

    public void update() {
        if (type == Type.BOSS) {
            updateBoss();
        } else {
            x += speedX;
            y += speedY;
            if (x <= 0 || x + W >= MainApp.WIDTH)     speedX = -speedX;
            if (y > MainApp.HEIGHT * 0.52)             speedY = -Math.abs(speedY);
            if (y < 25)                                speedY = Math.abs(speedY);
        }
        updatePosition();
        bombTimer++;
    }

    private void updateBoss() {
        bossAngle += 0.015;
        // Figure-8 / circular movement in upper portion
        x = MainApp.WIDTH / 2.0 - W / 2.0 + Math.sin(bossAngle) * 320;
        y = 40 + Math.sin(bossAngle * 2) * 80;

        // Phase change at HP milestones
        if (hp <= maxHp / 2) phase = 1;
        if (hp <= maxHp / 4) phase = 2;
    }

    public boolean shouldDropBomb() {
        int interval = (type == Type.BOSS && phase > 0) ? bombInterval / (phase + 1) : bombInterval;
        if (bombTimer >= interval) { bombTimer = 0; return true; }
        return false;
    }

    /** Returns number of bombs to drop (boss fires spray) */
    public int bombCount() {
        if (type == Type.BOSS) return 2 + phase;
        if (type == Type.COMMANDER) return 2;
        return 1;
    }

    public boolean hit() {
        hp--;
        refreshHpDots();
        return hp <= 0;
    }

    private void updatePosition() {
        group.setLayoutX(x);
        group.setLayoutY(y);
        hpIndicator.setLayoutX(x + W / 2.0 - (maxHp * 10) / 2.0);
        hpIndicator.setLayoutY(y);
    }

    public void addToPane(Pane pane)    { pane.getChildren().addAll(group, hpIndicator); }
    public void removeFromPane(Pane p)  { p.getChildren().removeAll(group, hpIndicator); }

    public double getCenterX() { return x + W / 2; }
    public double getCenterY() { return y + H / 2; }
    public double getBottomY() { return y + H; }
    public double getW()       { return W; }
    public Type   getType()    { return type; }
    public int    getHp()      { return hp; }
    public int    getMaxHp()   { return maxHp; }
    public boolean isBoss()    { return type == Type.BOSS; }

    public javafx.geometry.Bounds getBounds() { return group.getBoundsInParent(); }
}
