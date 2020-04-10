package c4l.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class Update {

    private Logger log;
    private Connection conn;
    private DB db;


    protected  Update (Connection conn){
        this.conn = conn;
        db =  DB.getInstance();
        log = Logger.getLogger(Update.class.getName());
    }

    /**
     * Update an Scene name
     * @param id id of the scene
     * @param name new name of the scene
     */
    public void sceneName(int id , String name) {
        log.config("update scene name for: " + id +" -> " +name);
        String SQL = "update scene set scene_name = '"+name+"' where scene_id = "+id+";";
        updatDbData(SQL);
    }

    private void updatDbData(String SQL) {

        try {

            Statement query = conn.createStatement();
            query.execute(SQL);

        } catch (SQLException e) {
            //logger.error(e);

        }
    }






}
