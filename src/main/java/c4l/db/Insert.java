package c4l.db;

import c4l.db.util.*;
import com.mysql.cj.result.SqlDateValueFactory;

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
                    insertEffectStates(device.main_effect, deviceSID, true , effect_id);
                    // save effects
                    insertEffectStates(device.effects, deviceSID, false, effect_id);

                } else {
                    throw new Exception("Device not Found: sid:" + setupID + " addres: "
                            + i * 16);
                }

            }
            // Insert new Scene

            int sceneID = createScene(name, description);
            addSceneToSetUp(setupID, sceneID);

            // add Device status to scene
            addDeviceStatesToScene(deviceStatusIDs, sceneID);

            return sceneID;

        } catch (SQLException e) {
            log.severe("Fail to Save the Scene "+e);
            throw new SQLException(e);
        }
    }

    /**
     * insert the States of effects from an Device status
     * @param effects effect which should be saved
     * @param deviceStatusId the id of the device status for the effects
     * @param isMain is it a main effect
     * @param effect_id an effect_id instance to get the ids of the effects
     * @return ids of the effect states
     * @throws SQLException
     */
    protected ResultSet insertEffectStates(LinkedList<Effect> effects, int deviceStatusId, boolean isMain , Effect_ID effect_id) throws SQLException {
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
            log.severe("Fail to insert the Effect Statis " + toString());
            throw new SQLException(e);

        }

    }

    /**
     * add new Scene to a setup
     *
     * @param setUpID Id of the setup to add to
     * @param sceneID Id of the Scene which will add
     */
    public void addSceneToSetUp(int setUpID, int sceneID) throws SQLException {
        log.config("scene: " + sceneID + " to setup: " + setUpID);
        String INSERT_SCENE_TO_SETUP = "insert into setup_has_scene(setUp_id,scene_id) values(?,?)";
        try {
            PreparedStatement insertNewSceneStatement = conn.prepareStatement(INSERT_SCENE_TO_SETUP);
            insertNewSceneStatement.setInt(1, setUpID);
            insertNewSceneStatement.setInt(2, sceneID);
            insertNewSceneStatement.execute();
        } catch (SQLException e) {
            log.severe("Fail to add Scene to Setup " + e.toString());
            throw new SQLException(e);
        }
    }

    /**
     * crate new scene entry returns the new id
     *
     * @param name name of the new Scene
     * @param description description of the new Scene
     * @return id of the new scene
     */
    private Integer createScene(String name, String description) throws SQLException {
        log.config("create new scene");
        String INSERT_SCENE = "insert into scene(scene_name,scene_description) values(?,?)";
        try {
            PreparedStatement insertNewSceneStatment = conn.prepareStatement(INSERT_SCENE,
                    Statement.RETURN_GENERATED_KEYS);

            insertNewSceneStatment.setString(1, name);
            insertNewSceneStatment.setString(2, description);
            insertNewSceneStatment.execute();

            ResultSet rs = insertNewSceneStatment.getGeneratedKeys();
            rs.next();
            return rs.getInt(1);

        } catch (SQLException e) {
            log.severe("Fail to Create a new Scene " +e);
            throw new SQLException(e);
        }

    }


    /**
     * add device states to an scene
     *
     * @param deviceStatusIds ids of the Device states wich should be add to an scene id
     * @param sceneID id of the scene to which the effects while be added
     */
    public void addDeviceStatesToScene(ArrayList<Integer> deviceStatusIds, int sceneID) throws SQLException {
        log.config("add device status to scene :" + sceneID);
        String INSERT_DS_TO_SCENE = "insert into scene_has_device_status(scene_id,device_status_id) values(?,?)";
        try {
            PreparedStatement insertNewSceneStatement = conn.prepareStatement(INSERT_DS_TO_SCENE);
            for (int id : deviceStatusIds) {
                insertNewSceneStatement.setInt(1, sceneID);
                insertNewSceneStatement.setInt(2, id);
                insertNewSceneStatement.addBatch();
            }
            insertNewSceneStatement.executeBatch();

        } catch (SQLException e) {
            log.severe("Fail to add Device "+ e);
            throw new SQLException(e);
        }
    }


}
