package com.spacegame;

import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.geometry.Pos;
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GameView {

    // ── Layout constants ─────────────────────────────────────────────────────
    private static final int    MAX_LEVEL         = 5;
    private static final int    ENEMIES_PER_LEVEL = 10;
    private static final double POWERUP_CHANCE    = 0.28;
    private static final Random RAND              = new Random();

    // ── Scene / root ─────────────────────────────────────────────────────────
    private final Pane  root  = new Pane();
    private final Scene scene;

    // ── Game state ───────────────────────────────────────────────────────────
    private Player player;
    private final List<Enemy>     enemies     = new ArrayList<>();
    private final List<Projectile> projectiles = new ArrayList<>();
    private final List<Bomb>      bombs       = new ArrayList<>();
    private final List<Explosion> explosions  = new ArrayList<>();
    private final List<PowerUp>   powerUps    = new ArrayList<>();

    private int  score                = 0;
    private int  level                = 1;
    private int  enemiesKilledInLevel = 0;
    private boolean gameOver   = false;
    private boolean paused     = false;
    private boolean bossLevel  = false;

    // ── Input ────────────────────────────────────────────────────────────────
    private boolean leftPressed, rightPressed, upPressed, downPressed, spacePressed;

    // ── Timers ───────────────────────────────────────────────────────────────
    private int shootCooldown  = 0;
    private int spawnTimer     = 0;
    private int spawnInterval  = 130;
    private int screenFlash    = 0;

    // ── HUD nodes ────────────────────────────────────────────────────────────
    private Text      scoreLabel, levelLabel, livesLabel, powerUpLabel;
    private Rectangle energyBarFg, energyBarBg;
    private Rectangle screenFlashRect;
    private Text      pauseText;

    // ── Boss health bar ───────────────────────────────────────────────────────
    private Rectangle bossBarFg, bossBarBg;
    private Text      bossBarLabel;

    // ── Background ───────────────────────────────────────────────────────────
    private StarField starField;

    public GameView() {
        root.setPrefSize(MainApp.WIDTH, MainApp.HEIGHT);
        root.setStyle("-fx-background-color: #000010;");

        starField = new StarField(root);
        buildHUD();

        player = new Player(MainApp.WIDTH / 2.0 - Player.W / 2, MainApp.HEIGHT - 110);
        player.addToPane(root);

        spawnWave();

        scene = new Scene(root, MainApp.WIDTH, MainApp.HEIGHT);

        FadeTransition fi = new FadeTransition(Duration.millis(500), root);
        fi.setFromValue(0); fi.setToValue(1); fi.play();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HUD
    // ─────────────────────────────────────────────────────────────────────────
    private void buildHUD() {
        // Top bar background
        Rectangle hudBg = new Rectangle(0, 0, MainApp.WIDTH, 42);
        hudBg.setFill(Color.color(0.0, 0.0, 0.1, 0.75));
        root.getChildren().add(hudBg);

        scoreLabel = hudText("Score: 0", 10, 27, 16, Color.WHITE);
        levelLabel = hudText("Niveau 1", MainApp.WIDTH / 2.0 - 40, 27, 16, Color.CYAN);
        livesLabel = hudText("♥♥♥", MainApp.WIDTH - 160, 27, 16, Color.color(1.0, 0.3, 0.4));

        Text energyLbl = hudText("Énergie", MainApp.WIDTH - 255, 27, 13, Color.color(0.7, 0.9, 1.0));

        energyBarBg = new Rectangle(MainApp.WIDTH - 170, 14, 130, 14);
        energyBarBg.setFill(Color.color(0.15, 0.0, 0.0)); energyBarBg.setArcWidth(5); energyBarBg.setArcHeight(5);

        energyBarFg = new Rectangle(MainApp.WIDTH - 170, 14, 130, 14);
        energyBarFg.setFill(Color.LIMEGREEN); energyBarFg.setArcWidth(5); energyBarFg.setArcHeight(5);

        powerUpLabel = hudText("", 10, MainApp.HEIGHT - 10, 13, Color.YELLOW);
        powerUpLabel.setEffect(new Glow(0.6));

        // Boss health bar (hidden initially)
        bossBarBg = new Rectangle(MainApp.WIDTH / 2.0 - 160, MainApp.HEIGHT - 30, 320, 14);
        bossBarBg.setFill(Color.color(0.2, 0.0, 0.0)); bossBarBg.setArcWidth(5); bossBarBg.setArcHeight(5);
        bossBarBg.setStroke(Color.color(0.8, 0.2, 0.2, 0.6)); bossBarBg.setStrokeWidth(1.0);
        bossBarBg.setVisible(false);

        bossBarFg = new Rectangle(MainApp.WIDTH / 2.0 - 160, MainApp.HEIGHT - 30, 320, 14);
        bossBarFg.setFill(new LinearGradient(0, 0, 1, 0, true, CycleMethod.NO_CYCLE,
            new Stop(0, Color.color(1.0, 0.1, 0.1)), new Stop(1, Color.ORANGE)));
        bossBarFg.setArcWidth(5); bossBarFg.setArcHeight(5);
        bossBarFg.setVisible(false);

        bossBarLabel = hudText("BOSS", MainApp.WIDTH / 2.0 - 18, MainApp.HEIGHT - 19, 11, Color.color(1.0, 0.5, 0.5));
        bossBarLabel.setVisible(false);

        // Screen flash overlay
        screenFlashRect = new Rectangle(0, 0, MainApp.WIDTH, MainApp.HEIGHT);
        screenFlashRect.setFill(Color.color(1.0, 0.2, 0.0, 0.0));
        screenFlashRect.setMouseTransparent(true);

        // Pause overlay
        pauseText = new Text("PAUSE");
        pauseText.setFont(Font.font("Arial", FontWeight.BOLD, 52));
        pauseText.setFill(Color.color(0.6, 0.9, 1.0, 0.9));
        pauseText.setEffect(new Glow(0.7));
        pauseText.setX(MainApp.WIDTH / 2.0 - 70);
        pauseText.setY(MainApp.HEIGHT / 2.0 + 18);
        pauseText.setVisible(false);

        root.getChildren().addAll(energyBarBg, energyBarFg,
            bossBarBg, bossBarFg, screenFlashRect, pauseText);
    }

    private Text hudText(String txt, double x, double y, double size, Color fill) {
        Text t = new Text(txt);
        t.setFont(Font.font("Arial", FontWeight.BOLD, size));
        t.setFill(fill); t.setX(x); t.setY(y);
        root.getChildren().add(t);
        return t;
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Public entry-point
    // ─────────────────────────────────────────────────────────────────────────
    public void start() {
        scene.setOnKeyPressed(e -> {
            switch (e.getCode()) {
                case LEFT,  A -> leftPressed  = true;
                case RIGHT, D -> rightPressed = true;
                case UP,    W -> upPressed    = true;
                case DOWN,  S -> downPressed  = true;
                case SPACE    -> spacePressed = true;
                case P, ESCAPE -> togglePause();
            }
        });
        scene.setOnKeyReleased(e -> {
            switch (e.getCode()) {
                case LEFT,  A -> leftPressed  = false;
                case RIGHT, D -> rightPressed = false;
                case UP,    W -> upPressed    = false;
                case DOWN,  S -> downPressed  = false;
                case SPACE    -> spacePressed = false;
            }
        });

        new AnimationTimer() {
            @Override public void handle(long now) {
                if (!gameOver && !paused) update();
            }
        }.start();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Main update
    // ─────────────────────────────────────────────────────────────────────────
    private void update() {
        starField.update();

        // Player movement
        if (leftPressed)  player.moveLeft();
        if (rightPressed) player.moveRight();
        if (upPressed)    player.moveUp();
        if (downPressed)  player.moveDown();
        player.update();

        // Shooting
        int cooldownMax = player.isRapidFire() ? 6 : 14;
        if (spacePressed && shootCooldown <= 0) {
            if (player.isDoubleShot()) {
                fireProjectile(-10);
                fireProjectile(+10);
            } else {
                fireProjectile(0);
            }
            shootCooldown = cooldownMax;
        }
        if (shootCooldown > 0) shootCooldown--;

        // Spawn enemies gradually
        spawnTimer++;
        if (!bossLevel && spawnTimer >= spawnInterval && enemies.size() < maxEnemiesOnScreen()) {
            spawnEnemy();
            spawnTimer = 0;
        }

        // Update enemies
        for (Enemy e : enemies) e.update();

        // Enemy bomb drops
        for (Enemy e : enemies) {
            if (e.shouldDropBomb()) {
                int count = e.bombCount();
                for (int i = 0; i < count; i++) {
                    double spread = count > 1 ? (i - (count - 1) / 2.0) * 0.25 : 0;
                    Bomb b = new Bomb(e.getCenterX(), e.getBottomY(), spread);
                    b.addToPane(root);
                    bombs.add(b);
                }
            }
        }

        // Update projectiles
        projectiles.removeIf(p -> { p.update(); if (!p.isActive()) { p.removeFromPane(root); return true; } return false; });

        // Update bombs
        bombs.removeIf(b -> {
            b.update(root);
            if (!b.isActive()) { b.cleanup(root); b.removeFromPane(root); return true; }
            return false;
        });

        // Update explosions
        explosions.removeIf(ex -> { ex.update(); if (!ex.isActive()) { ex.removeFromPane(root); return true; } return false; });

        // Update power-ups
        powerUps.removeIf(pu -> { pu.update(); if (!pu.isActive()) { pu.removeFromPane(root); return true; } return false; });

        // Collisions
        handleProjectileEnemyCollisions();
        handleBombPlayerCollisions();
        handlePowerUpPlayerCollisions();

        // Screen flash
        if (screenFlash > 0) {
            screenFlash--;
            screenFlashRect.setFill(Color.color(1.0, 0.15, 0.0, screenFlash * 0.025));
        }

        // Power-up status label
        updatePowerUpLabel();
        updateBossBar();
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Collision handlers
    // ─────────────────────────────────────────────────────────────────────────
    private void handleProjectileEnemyCollisions() {
        Iterator<Projectile> pIt = projectiles.iterator();
        outer:
        while (pIt.hasNext()) {
            Projectile p = pIt.next();
            Iterator<Enemy> eIt = enemies.iterator();
            while (eIt.hasNext()) {
                Enemy e = eIt.next();
                if (p.getBounds().intersects(e.getBounds())) {
                    p.deactivate(); p.removeFromPane(root); pIt.remove();
                    boolean killed = e.hit();
                    if (killed) {
                        addExplosion(e.getCenterX(), e.getCenterY(), e.isBoss());
                        e.removeFromPane(root); eIt.remove();
                        score += scoreForEnemy(e);
                        enemiesKilledInLevel++;
                        scoreLabel.setText("Score: " + score);
                        maybeDrop(e.getCenterX(), e.getCenterY());
                        checkLevelCompletion();
                    } else {
                        // Hit flash on armored enemy
                        addExplosion(e.getCenterX(), e.getCenterY(), false);
                    }
                    continue outer;
                }
            }
        }
    }

    private void handleBombPlayerCollisions() {
        Iterator<Bomb> bIt = bombs.iterator();
        while (bIt.hasNext()) {
            Bomb b = bIt.next();
            if (b.getBounds().intersects(player.getBounds()) && player.canBeHit()) {
                b.deactivate(); b.cleanup(root); b.removeFromPane(root); bIt.remove();
                player.takeDamage(20);
                screenFlash = 12;
                addExplosion(player.getCenterX(), player.getY() + Player.H / 2, false);
                updateEnergyBar();
                if (player.getEnergy() <= 0) {
                    player.loseLife();
                    updateLivesLabel();
                    if (player.getLives() <= 0) triggerGameOver();
                    else updateEnergyBar();
                }
            }
        }
    }

    private void handlePowerUpPlayerCollisions() {
        Iterator<PowerUp> it = powerUps.iterator();
        while (it.hasNext()) {
            PowerUp pu = it.next();
            if (pu.getBounds().intersects(player.getBounds())) {
                applyPowerUp(pu.getType());
                pu.removeFromPane(root);
                it.remove();
            }
        }
    }

    private void applyPowerUp(PowerUp.Type type) {
        switch (type) {
            case SHIELD      -> player.activateShield(300);
            case RAPID_FIRE  -> player.activateRapidFire(420);
            case DOUBLE_SHOT -> player.activateDoubleShot(420);
            case HEAL        -> { player.healEnergy(30); updateEnergyBar(); }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ─────────────────────────────────────────────────────────────────────────
    private void fireProjectile(double offsetX) {
        Projectile p = new Projectile(player.getCenterX(), player.getTopY(), offsetX);
        p.addToPane(root);
        projectiles.add(p);
    }

    private void addExplosion(double cx, double cy, boolean large) {
        Explosion ex = new Explosion(cx, cy, large);
        ex.addToPane(root);
        explosions.add(ex);
    }

    private void maybeDrop(double cx, double cy) {
        if (RAND.nextDouble() < POWERUP_CHANCE) {
            PowerUp.Type[] types = PowerUp.Type.values();
            PowerUp pu = new PowerUp(cx, cy, types[RAND.nextInt(types.length)]);
            pu.addToPane(root);
            powerUps.add(pu);
        }
    }

    private int scoreForEnemy(Enemy e) {
        return switch (e.getType()) {
            case SCOUT     -> 10 * level;
            case WARRIOR   -> 15 * level;
            case ARMORED   -> 25 * level;
            case COMMANDER -> 35 * level;
            case BOSS      -> 500;
        };
    }

    private int maxEnemiesOnScreen() { return 6 + level; }

    // ─────────────────────────────────────────────────────────────────────────
    //  Level / wave management
    // ─────────────────────────────────────────────────────────────────────────
    private void spawnWave() {
        bossLevel = (level == MAX_LEVEL);
        if (bossLevel) {
            spawnBoss();
            // A few minions for flavour
            for (int i = 0; i < 4; i++) spawnEnemy();
        } else {
            int count = 3 + level;
            for (int i = 0; i < count; i++) spawnEnemy();
        }
    }

    private void spawnEnemy() {
        double ex = 30 + RAND.nextDouble() * (MainApp.WIDTH - 100);
        double ey = 50 + RAND.nextDouble() * 90;
        Enemy.Type type = pickEnemyType();
        Enemy enemy = new Enemy(ex, ey, level, type);
        enemy.addToPane(root);
        enemies.add(enemy);
    }

    private Enemy.Type pickEnemyType() {
        int r = RAND.nextInt(100);
        return switch (level) {
            case 1 -> Enemy.Type.SCOUT;
            case 2 -> r < 60 ? Enemy.Type.SCOUT   : Enemy.Type.WARRIOR;
            case 3 -> r < 30 ? Enemy.Type.SCOUT   : r < 70 ? Enemy.Type.WARRIOR : Enemy.Type.ARMORED;
            case 4 -> r < 20 ? Enemy.Type.WARRIOR : r < 55 ? Enemy.Type.ARMORED : Enemy.Type.COMMANDER;
            default-> Enemy.Type.SCOUT;
        };
    }

    private void spawnBoss() {
        Enemy boss = new Enemy(MainApp.WIDTH / 2.0 - 60, 60, level, Enemy.Type.BOSS);
        boss.addToPane(root);
        enemies.add(boss);
        showBossBar(boss);
    }

    private void checkLevelCompletion() {
        boolean bossAlive = enemies.stream().anyMatch(Enemy::isBoss);
        boolean levelDone;

        if (bossLevel) {
            levelDone = !bossAlive;
        } else {
            levelDone = enemiesKilledInLevel >= ENEMIES_PER_LEVEL;
        }

        if (!levelDone) return;

        if (level >= MAX_LEVEL) {
            javafx.application.Platform.runLater(() ->
                MainApp.showGameOver(score, level, true));
        } else {
            level++;
            enemiesKilledInLevel = 0;
            spawnInterval = Math.max(70, spawnInterval - 15);
            levelLabel.setText("Niveau " + level);
            // Clear remaining enemies cleanly
            enemies.forEach(e -> e.removeFromPane(root));
            enemies.clear();
            showLevelBanner();
            spawnWave();
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  Game over
    // ─────────────────────────────────────────────────────────────────────────
    private void triggerGameOver() {
        gameOver = true;
        javafx.application.Platform.runLater(() ->
            MainApp.showGameOver(score, level, false));
    }

    // ─────────────────────────────────────────────────────────────────────────
    //  HUD updates
    // ─────────────────────────────────────────────────────────────────────────
    private void updateEnergyBar() {
        double ratio = player.getEnergy() / 100.0;
        energyBarFg.setWidth(130 * ratio);
        if (ratio > 0.5)      energyBarFg.setFill(Color.LIMEGREEN);
        else if (ratio > 0.25) energyBarFg.setFill(Color.ORANGE);
        else                   energyBarFg.setFill(Color.RED);
    }

    private void updateLivesLabel() {
        int lives = player.getLives();
        livesLabel.setText("♥".repeat(Math.max(0, lives)) + "♡".repeat(Math.max(0, 3 - lives)));
    }

    private void updatePowerUpLabel() {
        StringBuilder sb = new StringBuilder();
        if (player.isRapidFire())  sb.append("[Tir Rapide] ");
        if (player.isDoubleShot()) sb.append("[Double Tir] ");
        powerUpLabel.setText(sb.toString());
    }

    private void showBossBar(Enemy boss) {
        bossBarBg.setVisible(true);
        bossBarFg.setVisible(true);
        bossBarLabel.setVisible(true);
    }

    private void updateBossBar() {
        if (!bossLevel) return;
        enemies.stream().filter(Enemy::isBoss).findFirst().ifPresentOrElse(boss -> {
            bossBarBg.setVisible(true);
            bossBarFg.setVisible(true);
            bossBarLabel.setVisible(true);
            double ratio = (double) boss.getHp() / boss.getMaxHp();
            bossBarFg.setWidth(320 * ratio);
        }, () -> {
            bossBarBg.setVisible(false);
            bossBarFg.setVisible(false);
            bossBarLabel.setVisible(false);
        });
    }

    private void showLevelBanner() {
        Text banner = new Text("NIVEAU " + level);
        banner.setFont(Font.font("Arial", FontWeight.BOLD, 46));
        banner.setFill(Color.color(0.4, 0.9, 1.0));
        banner.setEffect(new Glow(0.8));
        banner.setX(MainApp.WIDTH / 2.0 - 110);
        banner.setY(MainApp.HEIGHT / 2.0 + 16);
        root.getChildren().add(banner);
        FadeTransition ft = new FadeTransition(Duration.seconds(1.8), banner);
        ft.setFromValue(1.0); ft.setToValue(0.0);
        ft.setOnFinished(e -> root.getChildren().remove(banner));
        ft.play();
    }

    private void togglePause() {
        paused = !paused;
        pauseText.setVisible(paused);
    }

    public Scene getScene() { return scene; }
}
