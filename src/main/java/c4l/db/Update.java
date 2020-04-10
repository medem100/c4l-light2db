package c4l.db;

import c4l.db.util.Device;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
     *
     * @param devices
     * @param id
     */
    public void scene(Device[] devices, int id) {
        ArrayList<Integer> deviceStatusIDs = select.getDeviceStatusIdsForScene(id);
        int iterator = 0;
        for (int dsid : deviceStatusIDs) {
            deletEffects(dsid);
            insert.insertEffectStatis(devices[iterator].effects, dsid, false);
            insert.insertEffectStatis(devices[iterator].main_effect, dsid, true);
            String SQL = "update device_status set input ='" + toSaveString(devices[iterator].getInputs())
                    + "' where device_status_id= " + dsid;
            updateDbData(SQL);
            iterator++;
        }
        // TODO insert rest devices

    }


    /**
     * Update an Scene name
     * @param id id of the scene
     * @param name new name of the scene
     */
    public void sceneName(int id , String name) {
        log.config("update scene name for: " + id +" -> " +name);
        String SQL = "update scene set scene_name = '"+name+"' where scene_id = "+id+";";
        updateDbData(SQL);
    }

    /**
     * generic function to update the DB with an SQL Statement
     * @param SQL to executeSQL
     */
    private void updateDbData(String SQL) {
        try {
            Statement query = conn.createStatement();
            query.execute(SQL);
        } catch (SQLException e) {
            log.severe("Fail to Update DB wit SQL: " );
        }
    }








}
