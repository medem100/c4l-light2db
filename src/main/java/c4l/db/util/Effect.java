package c4l.db.util;

public interface Effect {

    public void setSize(int size);
    public int getSize();

    public void setSpeed(int speed);
    public int getSpeed();

    public int[] getChannels();

    public int getState();
    public void setState(int state);




}
