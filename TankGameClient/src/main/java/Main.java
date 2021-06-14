import javafx.animation.*;
import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.scene.Group;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;



public class Main extends Application {
    public int rotation = 1;
    public static final double WIDTH = 1042;
    public static final double HEIGHT = 1042;
    private static Socket clientSocket;
    private static BufferedReader reader;
    private static BufferedReader in;
    private static BufferedWriter out;
    public static Sprite player;
    public static String lastKey = "DOWN";
    public static ImageView border;
    public static ImageView life;

    public static void main(String[] args) {
        launch(args);
    }

    public void createLifeBar(Group root) {
        border = new ImageView("border.png");
        border.setFitHeight(60);
        border.setFitWidth(330);
        life = new ImageView("life.png");
        life.setFitHeight(35);
        life.setFitWidth(305);
        life.setX(713);
        life.setY(20);
        border.setX(700);
        border.setY(7);
        root.getChildren().addAll(life, border);
    }

    public boolean connectToServer() {
        try {
            clientSocket = new Socket("localhost", 4004);
            reader = new BufferedReader(new InputStreamReader(System.in));
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            out = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            return true;
        } catch (IOException e) {
            System.out.println("Server closed");
            System.exit(1);
        }
        return false;
    }

    public void shoot(Sprite tank, Canvas canvas, String enemyRotation) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Sprite bullet = new Sprite();
        double x = tank.getBoundary().getMinX() + ((tank.getBoundary().getMaxX() - tank.getBoundary().getMinX()) / 2);
        double y = tank.getBoundary().getMinY() + ((tank.getBoundary().getMaxY() - tank.getBoundary().getMinY()) / 2);
        if (enemyRotation != null) {
            if (enemyRotation.equals("RIGHT")) {
                x = tank.getBoundary().getMinX() - 5;
                bullet.setImage("bulletLeft.png");
            }
            if (enemyRotation.equals("LEFT")) {
                x = 5 + tank.getBoundary().getMaxX();
                bullet.setImage("bulletRight.png");
            }
            if (enemyRotation.equals("DOWN")) {
                y = tank.getBoundary().getMinY() - 5;
                bullet.setImage("bulletUp.png");
            }
            if (enemyRotation.equals("UP")) {
                y = 5 + tank.getBoundary().getMaxY();
                bullet.setImage("bulletDown.png");
            }
        } else {
            if (rotation == 1) {
                y = tank.getBoundary().getMinY() - 5;
                bullet.setImage("bulletUp.png");
            }
            if (rotation == 2) {
                x = 5 + tank.getBoundary().getMaxX();
                bullet.setImage("bulletRight.png");
            }
            if (rotation == 3) {
                y = 5 + tank.getBoundary().getMaxY();
                bullet.setImage("bulletDown.png");
            }
            if (rotation == 4) {
                x = tank.getBoundary().getMinX() - 5;
                bullet.setImage("bulletLeft.png");
            }
        }
        bullet.setPosition(x, y);
        BulletAnimation anim = new BulletAnimation(rotation, bullet, gc, enemyRotation);
        anim.start();
    }

    @Override
    public void start(Stage theStage)
    {
        theStage.setTitle( "World of Tanks Client" );
        Group root = new Group();
        Scene theScene = new Scene( root );
        theStage.setScene( theScene );
        Canvas canvas = new Canvas( WIDTH, HEIGHT );

        root.getChildren().add(canvas);
        GraphicsContext gc = canvas.getGraphicsContext2D();



        ArrayList<String> input = new ArrayList<String>();
        theScene.setOnKeyPressed(
                e -> {
                    String code = e.getCode().toString();
                    if ( !input.contains(code) ) {
                        if (!code.equals("SPACE"))
                            lastKey = code;
                        input.add(code);
                    }
                });

        theScene.setOnKeyReleased(
                e -> {
                    String code = e.getCode().toString();
                    input.remove( code );
                });

        Font theFont = Font.font( "Arial", FontWeight.BOLD, 48 );
        gc.setFont( theFont );
        gc.setFill( Color.DARKRED );
        gc.setStroke( Color.WHITE );
        gc.setLineWidth(1);

        player = new Sprite();
        player.setImage("playerup.png");
        player.setPosition(480, 922);

        Sprite enemy = new Sprite();
        enemy.setImage("enemydown.png");
        enemy.setPosition(480, 22);


        Image background = new Image( "field.png");
        createLifeBar(root);


        connectToServer();


        new AnimationTimer()
        {
            public void handle(long currentNanoTime)
            {
                player.setVelocity(0,0);
                if (input.contains("LEFT") && player.getBoundary().getMinX() > 0) {
                    player.setImage("playerleft.png");
                    rotation = 4;
                    player.addVelocity(-200,0);
                    try { out.write(rotation + "\n"); out.flush(); } catch (IOException e) {}
                }
                else if (input.contains("RIGHT") && player.getBoundary().getMaxX() < WIDTH) {
                    player.setImage("playerright.png");
                    player.addVelocity(200, 0);
                    rotation = 2;
                    try { out.write(rotation + "\n"); out.flush(); } catch (IOException e) {}
                }
                else if (input.contains("UP") && player.getBoundary().getMinY() > 0) {
                    player.setImage("playerup.png");
                    rotation = 1;
                    player.addVelocity(0, -200);
                    try { out.write(rotation + "\n"); out.flush(); } catch (IOException e) {}
                }
                else if (input.contains("DOWN") && player.getBoundary().getMaxY() < HEIGHT) {
                    player.setImage("playerdown.png");
                    rotation = 3;
                    player.addVelocity(0, 200);
                    try { out.write(rotation + "\n"); out.flush(); } catch (IOException e) {}
                }
                if (input.contains("SPACE")) {
                    shoot(player, canvas, null);
                    input.remove("SPACE");
                    try { out.write("6" + lastKey + "\n"); out.flush(); } catch (IOException e) {}
                }
                if (input.contains("ESCAPE"))
                    System.exit(0);

                player.update(0.016);

                String rotat = null;
                try {
                    if (in.ready()) {
                        rotat = in.readLine();
                        enemy.setVelocity(0,0);
                        if (rotat.equals("4") && enemy.getBoundary().getMaxX() > 0) {
                            enemy.setImage("enemyright.png");
                            enemy.addVelocity(200,0);
                        }
                        else if (rotat.equals("2") && enemy.getBoundary().getMinX() < WIDTH) {
                            enemy.setImage("enemyleft.png");
                            enemy.addVelocity(-200, 0);
                        }
                        else if (rotat.equals("1") && enemy.getBoundary().getMaxY() > 0) {
                            enemy.setImage("enemydown.png");
                            enemy.addVelocity(0, 200);
                        }
                        else if (rotat.equals("3") && enemy.getBoundary().getMinY() < HEIGHT) {
                            enemy.setImage("enemyup.png");
                            enemy.addVelocity(0, -200);
                        }
                        if (rotat.startsWith("6")) {
                            shoot(enemy, canvas, rotat.substring(1));
                        }
                        enemy.update(0.016);
                    }
                    gc.clearRect(0, 0, WIDTH, HEIGHT);
                    gc.drawImage(background, 0, 0);
                    player.render( gc );
                    enemy.render(gc);

                } catch (IOException e) {}
            }
        }.start();
        theStage.show();
    }
}

