package entity;

import graphics.Sprite;
import java.awt.Graphics2D;
import java.util.Observable;
import java.util.Observer;
import java.util.Timer;
import java.util.TimerTask;
import startrekking.GamePanel;
import util.KeyHandler;
import util.Position;
import util.EntityState;

public class Player extends Entity implements Observer{
    
    private double maxJumpHeight; //refers to max height reachable in jumping
    private float deltaY = 2; // variation of height character perform on every refresh when jumping
    private float startJumpPosY; //store character y position when starting his jump
    private KeyHandler khdl;
    private boolean falling = false;
    private Timer timer;
    private final int PERIOD_INTERVAL = 2;
    private final int INITIAL_DELAY = 1;
    
    public Player(Sprite sprite, Position origin, int size, KeyHandler khdl) {
        super(sprite, origin, size, EntityState.RUN);
        this.khdl = khdl;
        timer = new Timer();
        timer.scheduleAtFixedRate(new ScheduleTask(), INITIAL_DELAY*1000,PERIOD_INTERVAL*1000);
    }
    
    public void move(){
            dx += acc;  //Player Acceleration
            if(dx > maxSpeed){
                dx = maxSpeed; //if the delta x is over the max we reset it
            }
            
            if(state == EntityState.JUMP){
                if(ani.getFrame() == 3) ani.setDelay(-1);
                if(falling  == false){
                 dy = -deltaY;
                  if(pos.getY() <= maxJumpHeight){
                    falling = true;
                    dy= deltaY;
                  }
                }else{
                   dy = deltaY;
                    if(pos.getY() >= startJumpPosY ){  
                        dy = -(pos.getY() - startJumpPosY);
                        falling = false;
                        state = EntityState.RUN;
                    }
                }
            }else if(state == EntityState.ATTACK){
                if(/*ani.hasPlayed(1)*/ ani.getFrame() == 3){
                    state = EntityState.RUN;
                }
            }else if(state == EntityState.CRUNCH){
                if(ani.getFrame() == 1) ani.setDelay(-1);
            }     
    }
    
    public void updateGame(){
        move();//this update all the aspect of a player
        super.updateGame(state);     //e.g. movement, animation, position
        pos.addX(dx);    //update x position
        pos.addY(dy);  //update y position //no longer used
        dy=0;
        GamePanel.getMapPos().addX(dx);  //RIVEDERE, NON PULITO DAL PUNTO DI VISTA DEL CODICE POICHE' map E' DICHIARATA COME POSITION STATICA
        //GamePanel.map.addY(dy);
    }
    
    @Override
    public void render(Graphics2D g) {  //draw the player in the panel
        g.drawImage(ani.getImage(), (int)pos.getWorldVar().getX(), (int)pos.getWorldVar().getY(), size, size, null);
    }
    
    private void mapValueAction(int key, boolean b){
        if(true){ //in case the player is alive
            if((key == 4) && (state != EntityState.JUMP) && (state != EntityState.NONE)){
                state = EntityState.JUMP;
                startJumpPosY = pos.getY();
                maxJumpHeight = startJumpPosY - 100;
            }
            if(key == 5 && !b ){
                state = EntityState.CRUNCH;
            }else if(key == 5 && b){
                state = EntityState.RUN;
            }
            if(key == 3){
                state = EntityState.ATTACK;
            }
        }else{
            state = EntityState.DEAD;
        }
        
    }

    @Override
    public void update(Observable o, Object s) {
        if(o == this.khdl){
            int key = this.khdl.getValue();
            boolean b = khdl.isPressed();
            mapValueAction(key, b);
        }
    }
    
    private class ScheduleTask extends TimerTask{

        @Override
        public void run() {
            System.out.println( "Jump info: \n"+"startJumpPosY: "+startJumpPosY+"\n"+"deltaY: "+deltaY+"\n");
            
            deltaY = deltaY + 0.6f;
            if (deltaY > maxSpeed) {
                deltaY = maxSpeed;
            }
        }
        
    }
}
