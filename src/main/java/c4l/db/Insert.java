package c4l.db;

import c4l.db.util.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.logging.Logger;

import static c4l.db.util.Util.toSaveString;

public class Insert {

    Connection conn;
    Logger log ;

    protected Insert(Connection conn){
        this.conn = conn;
        log = Logger.getLogger(Insert.class.getName());
    }




    /**
     *  Insert a new Chase
     * @param name name of the Chase
     * @param description Description of the Chase
     * @param setupID id of the Setup for the Chase
     * @param sceneIds Scene ids of the Chase
     * @param fadeTimes fade times between the scenes of the Chase
     * @param showTimes show times of the different scenes
     * @return
     */
    public Integer chase(String name, String description, int setupID, int[] sceneIds, int[] fadeTimes, int[] showTimes) throws SQLException {
        log.config("insert Case");

        try {
            String SQLInsertChase = "insert into chase (chase_name,chase_description) values (?, ?);";
            String SQLInsertChaseHasScene = "insert into chase_has_scene (case_id,scene_id,pos,running_time) values (?,?,?,?);";
            String SQLInsertSetupHasChase = "insert into setup_has_chase( setUp_id, case_id) values(?,?)";

            PreparedStatement preparedStmt = conn.prepareStatement(SQLInsertChase, Statement.RETURN_GENERATED_KEYS);
            preparedStmt.setString(1, name);
            preparedStmt.setString(2, description);
            preparedStmt.execute();
            ResultSet rs = preparedStmt.getGeneratedKeys();
            rs.next();
            int chaseId = rs.getInt(1);

            chaseHasScene(chaseId, sceneIds, fadeTimes, showTimes);

            PreparedStatement preparedStmt3 = conn.prepareStatement(SQLInsertSetupHasChase);
            preparedStmt3.setInt(1, setupID);
            preparedStmt3.setInt(2, chaseId);
            preparedStmt3.execute();

            return chaseId;

        } catch (SQLException e) {
            log.severe("Fail to insert Chase "+ e.toString());
            throw new SQLException(e);
        }

    }

    /**
     * insert the Scenes for a Chase
     * @param chaseid id of the chase
     * @param scnenIds scene ids where are add to the chase
     * @param fadeTimes fade times between the scenes of the Chase
     * @param showTimes show times of the different scenes
     */
    protected void chaseHasScene(int chaseid, int[] scnenIds, int[] fadeTimes, int[] showTimes) throws SQLException {
        log.config("insert chaseHasScene");

        try {
            String SQLInsertChaseHasScene = "insert into chase_has_scene (case_id,scene_id,pos,running_time) values (?,?,?,?);";

            for (int i = 0; i < scnenIds.length; i++) {
                PreparedStatement preparedStmt2 = conn.prepareStatement(SQLInsertChaseHasScene);
                preparedStmt2.setInt(1, chaseid);
                preparedStmt2.setInt(2, scnenIds[i]);
                preparedStmt2.setInt(3, i);
                preparedStmt2.setInt(4, showTimes[i]);
                preparedStmt2.execute();
            }

        } catch (SQLException e) {
            log.severe("Fail to inset Scenes for Chase : " + e.toString());
            throw  new SQLException(e);
        }
    }


    /**
     *Insert Scene to setup
     *
     * @param Devices State of the Devices
     * @param setupID setup id from the setup of the scene
     * @return the Id of the new Scene
     * @throws Exception
     */
    public Integer scene(Device[] Devices, int setupID, String name, String description, Effect_ID effect_id) throws Exception {
        log.config("insert new scene");

        try {
           String SELECT_DEVICE_ID = "select d.device_id from device d " + "inner join setup_has_device shd "
                    + "on shd.device_id = d.device_id " + "where shd.setup_id =" + setupID + " and d.start_address =";

            String INSERT_DEVICE_STATUS = "insert into device_status(input, device_id) values(?,?)";

            PreparedStatement insertDeviceStatusStamens = conn.prepareStatement(INSERT_DEVICE_STATUS,
                    Statement.RETURN_GENERATED_KEYS);
            ArrayList<Integer> deviceStatusIDs = new ArrayList<>();
            // insert the device statis
            for (int i = 0; i < Devices.length; i++) {
                Device device = Devices[i];
                String dId = select.getOneData(SELECT_DEVICE_ID + i * Constants.DEVICE_CHANNELS + ";",
                        "device_id");
                if (dId != null) {
                    int deviceID = Integer.valueOf(dId);
                    log.config("deviceId: " + deviceID);
                    insertDeviceStatusStamens.setString(1, toSaveString(device.getInputs()));
                    insertDeviceStatusStamens.setInt(2, deviceID);
                    insertDeviceStatusStamens.execute();

                    ResultSet keys = insertDeviceStatusStamens.getGeneratedKeys();
                    keys.next();
                    int deviceSID = keys.getInt(1);
                    deviceStatusIDs.add(deviceSID);
                    // save main effects
                    insertEffectStatis(device.main_effect, deviceSID, true , effect_id);
                    // save effects
                    insertEffectStatis(device.effects, deviceSID, false, effect_id);

                } else {
                    throw new Exception("Device not Found: sid:" + setupID + " addres: "
                            + i * 16);
                }

            }
            // Insert new Scene

            int sceneID = createScene(name, description);
            addSceneToSetUp(setupID, sceneID);

            // add Device status to scene
            addDeviceStatusToScene(deviceStatusIDs, sceneID);

            return sceneID;

        } catch (SQLException e) {
            log.severe("Fail to Save the Scene "+e);
            throw new SQLException(e);
        }
    }

    /**
     * insert new effect status
     *
     * @param effect
     */
    protected ResultSet insertEffectStatis(LinkedList<Effect> effects, int deviceStatusId, boolean isMain , Effect_ID effect_id) {
        log.config("insert effect status for device statusID: " + deviceStatusId);
        String INSERT_EFFECT_STATUS = "insert into effect_status(size,speed,channels,accept_input,state,Device_status_id,Effect_id,is_main)"
                + "values(?,?,?,?,?,?,?,?);";
        // PreparedStatement insertNewSceneStatment =
        // conn.prepareStatement(INSERT_DS_TO_SCENE);
        try {
            PreparedStatement insertNewEffectStatusStatment = conn.prepareStatement(INSERT_EFFECT_STATUS,
                    Statement.RETURN_GENERATED_KEYS);
            for (Effect effect : effects) {
                insertNewEffectStatusStatment.setInt(1, effect.getSize());
                insertNewEffectStatusStatment.setInt(2, effect.getSpeed());
                insertNewEffectStatusStatment.setString(3, toSaveString(effect.getChannels()));
                insertNewEffectStatusStatment.setInt(4, effect.isAcceptInput() ? 1 : 0);
                insertNewEffectStatusStatment.setInt(5, effect.getState());
                insertNewEffectStatusStatment.setInt(6, deviceStatusId);
                insertNewEffectStatusStatment.setString(7, effect_id.getEffectID(effect).toString());
                insertNewEffectStatusStatment.setInt(8, isMain ? 1 : 0);
                insertNewEffectStatusStatment.addBatch();
            }
            insertNewEffectStatusStatment.executeBatch();
            return insertNewEffectStatusStatment.getGeneratedKeys();
        } catch (SQLException e) {
            logger.error(e);
            return null;

        }

    }



}
