package c4l.db;

import c4l.db.util.Constants;

public class Util {

    /**
     * convert an array to Save format of the DB
     * @param array array to save
     * @return String with the values of the Array
     */
    public static String toSaveString(int[] array) {
        String result = "";
        for (int e : array) {
            result += e + Constants.DELIMITER;
        }
        return result.substring(0, result.length() - 1);
    }

}
