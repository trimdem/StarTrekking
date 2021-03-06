package frames;

import entity.GroundEnemy;
import entity.Player;
import entity.Shot;
import gamestate.GameOverState;
import gamestate.GameStateManager;
import gamestate.StoryPlayState;
import graphics.Sprite;
import graphics.EntitySprite;
import tiles.TileFacade;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.ListIterator;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import util.EntityState;
import util.KeyHandler;
import util.Position;

public class GamePanel extends JPanel implements Runnable {

    /*
    Responsabilità:
    aprire e disegnare il frame, thread di aggiornamento
    
    Operazioni:
    1- update ciclico di Map e Entity
    2- Riferimenti di oggetti Map, Entity, KeyHandler, TileFacade
     */
    //dimensione finestra
    
    public static float unitTime = 700000000;

    public static final int WIDTH = 980;
    public static final int HEIGHT = 528;

    //nome JFrame
    public static final String NAME = "STAR TREKKING";
    
    public static int oldFrameCount;
    private static Position map;

    private Thread thread;
    private boolean running = false;

    private BufferedImage img;
    private Graphics2D g;

    private TileFacade tf;
    private Player player;
    private Sprite hpimg;
    private Sprite font;
    private KeyHandler key;
    private ArrayList<GroundEnemy> goblins; 
    private float previousTickHitted = 0;
    private StoryPlayState sps;
    

    public GamePanel(StoryPlayState sps) {
        this.goblins = new ArrayList<>();
        this.sps = sps;
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setFocusable(true);
        requestFocus();
        startThread();

    }

    public final void startThread() {
        if (thread == null) {
            thread = new Thread(this, "GameThread");
            thread.start();
        }
    }

    public void init() {

        running = true;

        img = new BufferedImage(WIDTH, HEIGHT, BufferedImage.TYPE_INT_ARGB);
        g = (Graphics2D) img.getGraphics();
        
        map = new Position(0, 0);
        Position.setWorldVar(map.getX(), map.getY());

        key = new KeyHandler();
        addKeyListener(key);

        tf = new TileFacade("tiles/LevelOne.xml");
        
        EntitySprite playerSprite = new EntitySprite("entity/wizard", 64, 64);
        player = new Player(playerSprite, new Position(0, 0 + (GamePanel.HEIGHT) - 130), 96, key);
        
        EntitySprite enemieSprite = new EntitySprite("entity/goblin", 64, 64);
        goblins.add(new GroundEnemy(enemieSprite, new Position(1000, (GamePanel.HEIGHT) - 130) , 96));
        goblins.add(new GroundEnemy(enemieSprite, new Position(1500, (GamePanel.HEIGHT) - 130) , 96));
        goblins.add(new GroundEnemy(enemieSprite, new Position(2100, (GamePanel.HEIGHT) - 130) , 96));
        goblins.add(new GroundEnemy(enemieSprite, new Position(3080, (GamePanel.HEIGHT) - 130) , 96));
        goblins.add(new GroundEnemy(enemieSprite, new Position(4700, (GamePanel.HEIGHT) - 130) , 96));
        
        
        hpimg = new Sprite("entity/heart.png", 32,32);
        key.addObserver(player);
        font = new Sprite("font/Font.png", 10, 10);
    }

    @Override
    public void run() {

        init();

        final double GAME_HERTZ = 600.0;  //standard is 600
        //final double TBU = 1000000000 / GAME_HERTZ;
        final double TBU = 1000000000 / GAME_HERTZ;

        final int MUBR = 80;

        double lastUpdateTime = System.nanoTime();
        double lastRenderTime;

        final double TARGET_FPS = 60;
        final double TTBR = 1000000000 / TARGET_FPS;

        int frameCount = 0;
        int lastSecondTime = (int) (lastUpdateTime / 1000000000);
        oldFrameCount = 0;

        while (running) {

            double now = System.nanoTime();
            int updateCount = 0;
            while (((now - lastUpdateTime) > TBU) && (updateCount < MUBR)) {
                update();
                lastUpdateTime += TBU;
                updateCount++;
            }

            if (now - lastUpdateTime > TBU) {
                lastUpdateTime = now - TBU;
            }
            
//            render(g);
//            draw();
            render();
            SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
               repaint(); 
            }
            });
            
            
            

            lastRenderTime = now;
            frameCount++;

            int thisSecond = (int) (lastUpdateTime / 1000000000);
            if (thisSecond > lastSecondTime) {
                if (frameCount != oldFrameCount) {
                    System.out.println("NEW SECOND " + thisSecond + " " + frameCount);
                    oldFrameCount = frameCount;
                }
                frameCount = 0;
                lastSecondTime = thisSecond;
            }

            while (now - lastRenderTime < TTBR && now - lastUpdateTime < TBU) {
                Thread.yield();

                try {
                    Thread.sleep(1);
                } catch (InterruptedException e) {
                    System.out.println("ERROR: yielding thread");
                }
                now = System.nanoTime();
            }
        }

    }

    public Color color;

    public void update() {
        Position.setWorldVar(map.getX(), map.getY());
        player.updateGame();
        
        for(GroundEnemy goblin : goblins){
            if(goblin.getPos().getWorldVar().getX() < GameFrame.WIDTH+100) goblin.updateGame();
        }
        
        if(player.getDead()) {
            sps.handleNext(1);
            thread.stop();
        }
        checkCollision();
        removeEnemies();
    }

    public void checkCollision() {
        ArrayList<Shot> shots = player.getShots();
        ListIterator<Shot> shotListIter = shots.listIterator();
        
        while(shotListIter.hasNext()){
            Shot s = shotListIter.next();
            for(GroundEnemy goblin : goblins){
                if(goblin.getState() != EntityState.DEAD && s.getBounds().collides(goblin.getBounds())){
                    System.err.println("Nemico colpito");
                    shotListIter.remove();
                    goblin.isDead();
                }
            }
        }
        
        for(GroundEnemy goblin : goblins){
            if(goblin.getState() != EntityState.DEAD && goblin.getBounds().collides(player.getBounds())){
                if(System.nanoTime() - previousTickHitted > unitTime){
                    System.err.println("Collisione player");
                    player.hitted();    
                }
                previousTickHitted = System.nanoTime();
            }
        }
    }

    public boolean test;

    public void render() {
        /*
        g.setColor(Color.RED);
        g.fillRect(100, 100, 64, 64);
         */
        if (g != null) {
            test = true;
//            g.setColor(new Color(66, 134, 244));
//            g.fillRect(0, 0, WIDTH, HEIGHT);
            tf.render(g);
            for(GroundEnemy goblin : goblins){
                goblin.render(g);
            }
            player.render(g);
            Sprite.drawArray(g, font, "FPS: " + GamePanel.oldFrameCount, new Position(GamePanel.WIDTH - (8 * 40), 10), 40, 40, 32, 0);
//            ArrayList<BufferedImage> hearts = new ArrayList<BufferedImage>();
//            BufferedImage heartImg; 
//            heartImg = hpimg.getMatrix();
//            hearts.add(heartImg);
//            int space = 10;
//            for (int i =0; i<player.getHP(); i++){
//                Sprite.drawArray(g,hearts,new Position (space,10), 90 , 90, 10,0);
//                  space += 70;
//            }
            int space = 0;
            for (int i = 0; i < player.getHP(); i++) {
                Sprite.drawArray(g, hpimg.getSprite(0, 0), new Position(space, 10), 90, 90);
                space += 60;
            }
        }
        //Sprite.drawArray(g, font, "FPS: " + GamePanel.oldFrameCount , new Vector2f(GamePanel.width - (8 * 40) , 10), 40, 40, 32, 0);    
    }
    
    private void removeEnemies(){
        ListIterator<GroundEnemy> goblinListIter = goblins.listIterator();
        
        while(goblinListIter.hasNext()){
            GroundEnemy currGoblin = goblinListIter.next();
            if(currGoblin.getDead()){
                goblinListIter.remove();
            }
        }
    }
    
    @Override   
    public void paintComponent(Graphics g){
        super.paintComponents(g);
        g.drawImage(img, 0, 0, WIDTH, HEIGHT, null);
        g.dispose();
    }

    public static int getWIDTH() {
        return WIDTH;
    }

    public static int getHEIGHT() {
        return HEIGHT;
    }
//
//    public static void main(String[] args) {
//        GamePanel gamePanel = new GamePanel();
//    }

    public static Position getMapPos() {
        return map;
    }

}

