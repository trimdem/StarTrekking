package gameObjects.entityState;

import gameObjects.Player;
import util.EntityEnum;

public class PlayerJumpState extends PlayerState{
    
    public PlayerJumpState(Player p){
        super(p);
        p.setAnimation(p.getSprite().getSprite(EntityEnum.JUMP), 80);
        p.getMg().setMusic("Jump");
        p.getMg().play();
        p.setTimeY(0);
    }

    
    /*
    Computation of the gravity and of the vertical speed in order to let the player make a jump of constant distance and height, regardless of the horizontal speed.
    When the player isn't in the jump state anymore the standard values of gravity and vertical speed are reset.
    */
    @Override
    public void updateGame() {
        if(p.getTc().collisionTileDown(0, p.getDy()-p.getPreviousY())){
            System.err.println("FINE SALTO");
            p.setVy(0);            //definire come costante di player
            p.setGravity(-0.01f);  //definire come costante di player
            nextState(1);
        }
        if(p.getTimey() == 0){
            System.err.println("inizio salto");
            if(!(p.getFalling())) p.setVy(-(float)((4*p.getH()*p.getInstantVx())/p.getDIST()));
                p.setGravity(-(float)((p.getH()*8*Math.pow(p.getInstantVx(), 2))/Math.pow(p.getDIST(), 2)));
            }
        if (p.getAnimation().playingLastFrame()) {
            p.getAnimation().setDelay(-1);
        }
        
    }

    @Override
    public void nextState(int code) {
        switch(code){
            case 0:
                break;
            case 1:
                p.setState(new PlayerRunState(p));
        }
        
    }

    @Override
    public void previousState(int code) {
    
    }
}




