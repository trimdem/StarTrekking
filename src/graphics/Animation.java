package graphics;

import java.awt.image.BufferedImage;

public class Animation {

    private BufferedImage[] frames;
    private int currentFrame;
    private int numFrames;

    private int count;
    private int delay;

    private int timesPlayed;

    public Animation(BufferedImage[] frames) {
        timesPlayed = 0;
        this.frames = frames;
        currentFrame = 0;
        count = 0;
        timesPlayed = 0;
        //System.out.println("arrivo prima io" + frames);
        numFrames = frames.length;
        
        delay = 5;
    }

    public Animation() {
        timesPlayed = 0;
    }

    public void setFrames(BufferedImage[] frames) {
        this.frames = frames;
        currentFrame = 0;
        count = 0;
        timesPlayed = 0;
        numFrames = frames.length;
    }

    public void setDelay(int i) {
        delay = i;
    }

    public void setFrame(int i) {
        currentFrame = i;
    }

    public void setNumFrames(int i) {
        numFrames = i;
    }

    public void updateGame() {
        if (delay == -1) {
            return;
        }

        count++;

        if (count == delay) {
            currentFrame++;
            count = 0;
        }
        if (currentFrame == numFrames) {
            currentFrame = 0;
            timesPlayed++;
        }
    }

    public int getDelay() {
        return delay;
    }

    public int getFrame() {
        return currentFrame;
    }

    public int getCount() {
        return count;
    }

    public BufferedImage getImage() {
        return frames[currentFrame];
    }

    public boolean hasPlayedOnce() {
        return timesPlayed > 0;
    }

    public boolean playingLastFrame() {
        return currentFrame == (numFrames - 1);
    }

    public boolean hasPlayed(int i) {
        return timesPlayed == i;
    }

}
