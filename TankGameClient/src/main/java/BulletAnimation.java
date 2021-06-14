import javafx.animation.AnimationTimer;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;


public class BulletAnimation extends AnimationTimer {
    public int rotation;
    Sprite bullet;
    GraphicsContext gc;
    String enemyRotation;

    BulletAnimation(int rotation, Sprite bullet, GraphicsContext gc, String enemyRotation) {
        this.rotation = rotation;
        this.bullet = bullet;
        this.gc = gc;
        this.enemyRotation = enemyRotation;
    }

    public void handle(long currentNanoTime)
    {
        bullet.setVelocity(0,0);
        if (enemyRotation == null) {
            if (rotation == 4) {
                bullet.addVelocity(-400, 0);
            }
            if (rotation == 2) {
                bullet.addVelocity(400, 0);
            }
            if (rotation == 1) {
                bullet.addVelocity(0, -400);
            }
            if (rotation == 3) {
                bullet.addVelocity(0, 400);
            }
        } else {
            if (enemyRotation.equals("LEFT")) {
                bullet.addVelocity(400, 0);
            }
            if (enemyRotation.equals("RIGHT")) {
                bullet.addVelocity(-400, 0);
            }
            if (enemyRotation.equals("DOWN")) {
                bullet.addVelocity(0, -400);
            }
            if (enemyRotation.equals("UP")) {
                bullet.addVelocity(0, 400);
            }
        }
        bullet.update(0.016);
        if (bullet.intersects(Main.player)) {
            Main.life.setFitWidth(Main.life.getFitWidth() - 1);
            if (Main.life.getFitWidth() < 0) {
                Main.life.setFitWidth(0);
                new AnimationTimer() {
                    public void handle(long currentNanoTime) {
                        gc.setFill( Color.DARKRED );
                        gc.fillText( "YOU LOSE", Main.WIDTH / 3, Main.HEIGHT / 2 );
                        gc.strokeText( "YOU LOSE", Main.WIDTH / 3, Main.HEIGHT / 2);
                    }
                }.start();
            }
        }
        bullet.render( gc );
        if (bullet.getBoundary().getMinX() < 0 || bullet.getBoundary().getMaxX() > Main.WIDTH || bullet.getBoundary().getMinY() < 0 || bullet.getBoundary().getMaxY() > Main.HEIGHT)
            this.stop();
    }
}