package com.spacegame;

import javafx.scene.Group;
import javafx.scene.effect.Glow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.paint.CycleMethod;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Ellipse;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;

public class Player {

    public static final double W = 44;
    public static final double H = 52;

    private double x, y;
    private final double speed = 5.5;
    private int energy = 100;
    private int lives = 3;

    // Power-up states
    private int shieldTimer    = 0;
    private int rapidFireTimer = 0;
    private int doubleShotTimer = 0;

    // Invincibility frames after being hit
    private int invincibleTimer = 0;

    private final Group group;
    private final Circle shieldCircle;
    private final Ellipse thrusterGlow;
    private int thrusterPhase = 0;

    public Player(double startX, double startY) {
        this.x = startX;
        this.y = startY;

        // Hull
        Polygon hull = new Polygon(
            W / 2, 0.0,
            W * 0.85, H * 0.55,
            W, H * 0.75,
            W * 0.72, H * 0.7,
            W * 0.6,  H * 0.88,
            W * 0.5,  H,
            W * 0.4,  H * 0.88,
            W * 0.28, H * 0.7,
            0.0,      H * 0.75,
            W * 0.15, H * 0.55
        );
        hull.setFill(new RadialGradient(0, 0, 0.5, 0.3, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0.2, 0.6, 1.0)),
            new Stop(1, Color.color(0.05, 0.15, 0.45))));
        hull.setStroke(Color.color(0.4, 0.85, 1.0));
        hull.setStrokeWidth(1.5);

        // Cockpit dome
        Ellipse cockpit = new Ellipse(W / 2, H * 0.35, W * 0.18, H * 0.14);
        cockpit.setFill(new RadialGradient(0, 0, 0.4, 0.35, 0.7, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(0.7, 1.0, 1.0, 0.95)),
            new Stop(1, Color.color(0.1, 0.5, 0.9, 0.6))));
        cockpit.setStroke(Color.CYAN);
        cockpit.setStrokeWidth(1.0);

        // Wing accents
        Polygon leftWing = new Polygon(0.0, H * 0.75, W * 0.15, H * 0.55, W * 0.3, H * 0.65, W * 0.15, H * 0.9);
        leftWing.setFill(Color.color(0.1, 0.4, 0.8, 0.7));

        Polygon rightWing = new Polygon(W, H * 0.75, W * 0.85, H * 0.55, W * 0.7, H * 0.65, W * 0.85, H * 0.9);
        rightWing.setFill(Color.color(0.1, 0.4, 0.8, 0.7));

        // Engine glow
        thrusterGlow = new Ellipse(W / 2, H + 4, W * 0.18, 8);
        thrusterGlow.setFill(new RadialGradient(0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(1.0, 0.7, 0.0, 0.9)),
            new Stop(1, Color.color(1.0, 0.2, 0.0, 0.0))));

        // Shield visual
        shieldCircle = new Circle(W / 2, H / 2, W * 0.7);
        shieldCircle.setFill(Color.color(0.2, 0.6, 1.0, 0.12));
        shieldCircle.setStroke(Color.color(0.3, 0.8, 1.0, 0.7));
        shieldCircle.setStrokeWidth(2.5);
        shieldCircle.setVisible(false);
        shieldCircle.setEffect(new Glow(0.8));

        group = new Group(thrusterGlow, leftWing, rightWing, hull, cockpit, shieldCircle);
        group.setEffect(new Glow(0.3));
        updatePosition();
    }

    public void moveLeft()  { if (x - speed > 0)                       x -= speed; updatePosition(); }
    public void moveRight() { if (x + speed + W < MainApp.WIDTH)        x += speed; updatePosition(); }
    public void moveUp()    { if (y - speed > MainApp.HEIGHT * 0.4)     y -= speed; updatePosition(); }
    public void moveDown()  { if (y + speed + H < MainApp.HEIGHT - 20)  y += speed; updatePosition(); }

    public void update() {
        // Thruster animation
        thrusterPhase++;
        double scale = 1.0 + 0.3 * Math.sin(thrusterPhase * 0.3);
        thrusterGlow.setRadiusX(W * 0.18 * scale);
        thrusterGlow.setRadiusY(8 * scale);

        if (shieldTimer > 0)     { shieldTimer--;     shieldCircle.setVisible(shieldTimer > 0); }
        if (rapidFireTimer > 0)  { rapidFireTimer--;  }
        if (doubleShotTimer > 0) { doubleShotTimer--; }
        if (invincibleTimer > 0) { invincibleTimer--;
            group.setOpacity(invincibleTimer % 6 < 3 ? 0.4 : 1.0);
        } else {
            group.setOpacity(1.0);
        }
    }

    public boolean canBeHit() { return invincibleTimer <= 0 && shieldTimer <= 0; }

    public void takeDamage(int dmg) {
        energy = Math.max(0, energy - dmg);
        invincibleTimer = 90;
    }

    public void activateShield(int duration)    { shieldTimer    = duration; shieldCircle.setVisible(true); }
    public void activateRapidFire(int duration) { rapidFireTimer = duration; }
    public void activateDoubleShot(int duration){ doubleShotTimer = duration; }
    public void healEnergy(int amount)          { energy = Math.min(100, energy + amount); }

    public boolean isRapidFire()  { return rapidFireTimer  > 0; }
    public boolean isDoubleShot() { return doubleShotTimer > 0; }

    public void loseLife() { lives--; energy = 100; invincibleTimer = 120; shieldCircle.setVisible(false); shieldTimer = 0; }
    public int  getLives() { return lives; }

    private void updatePosition() { group.setLayoutX(x); group.setLayoutY(y); }

    public void addToPane(Pane pane)    { pane.getChildren().add(group); }
    public void removeFromPane(Pane p)  { p.getChildren().remove(group); }

    public double getCenterX()            { return x + W / 2; }
    public double getTopY()               { return y; }
    public double getX()                  { return x; }
    public double getY()                  { return y; }
    public int    getEnergy()             { return energy; }

    public javafx.geometry.Bounds getBounds() { return group.getBoundsInParent(); }
}
