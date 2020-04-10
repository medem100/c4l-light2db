package c4l.db.util;

import java.util.LinkedList;

public interface Device {

   public LinkedList<Effect> effects = null;
   public LinkedList<Effect> main_effect = null;

   public int[] getInputs();
   public void setInputs(int[] inputs);

}
