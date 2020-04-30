package c4l.db;

import c4l.db.util.Device;
import c4l.db.util.Effect_ID;
import c4l.db.util.Util;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.logging.Logger;

public class Update {

    private Logger log;
    private Connection conn;
    private DB db;


    protected Update(Connection conn) {
        this.conn = conn;
        db = DB.getInstance();
        log = Logger.getLogger(Update.class.getName());
    }

    /**
     * update one Scene
     *
     * @param devices new state of the Devices
     * @param id      id of scene
     */
    public void scene(Device[] devices, int id , Effect_ID effect_id) throws SQLException {
        ArrayList<Integer> deviceStatusIDs = select.getDeviceStatusIdsForScene(id);
        int iterator = 0;
        for (int deviceStatusid : deviceStatusIDs) {
            db.Delete.effectsFromDeviceState(deviceStatusid);
            db.Insert.insertEffectStates(devices[iterator].effects, deviceStatusid, false , effect_id);
            db.Insert.insertEffectStates(devices[iterator].main_effect, deviceStatusid, true , effect_id);
            String SQL = "update device_status set input ='" + Util.toSaveString(devices[iterator].getInputs())
                    + "' where device_status_id= " + deviceStatusid;
            updateDbData(SQL);
            iterator++;
        }
        // TODO insert rest devices

    }


    /**
     * Update an Scene name
     *
     * @param id   id of the scene
     * @param name new name of the scene
     */
    public void sceneName(int id, String name) throws SQLException {
        log.config("update scene name for: " + id + " -> " + name);
        String SQL = "update scene set scene_name = '" + name + "' where scene_id = " + id + ";";
        updateDbData(SQL);
    }


    /**
     * Update the name of an Chase
     * @param chaseid id of the chase
     * @param name new Name of the chase
     * @throws SQLException
     */
    public void chaseName(int chaseid, String name) throws SQLException {
        String SQL = "update chase set chase_name='" + name + "' where chase_id=" + chaseid + ";";
        updateDbData(SQL);
    }

    /**
     *  Update the scenes of the chase
     * @param chaseId id of the chase
     * @param sceneIds ids of the new Scenes
     * @param fadeTimes Fade times between the new Scenes
     * @param showTimes Show times of the new Scenes
     * @throws SQLException
     */
    public void chaseScens(int chaseId, int[] sceneIds, int[] fadeTimes, int[] showTimes) throws SQLException {
        DB db = DB.getInstance();
        db.Delete.chaseHasScene(chaseId);
        db.Insert.chaseHasScene(chaseId, sceneIds, fadeTimes, showTimes);
    }


    /**
     * generic function to update the DB with an SQL Statement
     *
     * @param SQL to executeSQL
     */
    private void updateDbData(String SQL) throws SQLException {
        try {
            Statement query = conn.createStatement();
            query.execute(SQL);
        } catch (SQLException e) {
            log.severe("Fail to Update DB wit SQL: " + e.toString());
            throw new SQLException(e);
        }
    }

}
