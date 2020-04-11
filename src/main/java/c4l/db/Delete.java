package c4l.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

public class Delete {
    private Connection conn;
    private Logger log;

    protected Delete(Connection conn) {
        this.conn = conn;
        log = Logger.getLogger(Delete.class.getName());
    }

    /**
     * delete an scene from the setup
     *
     * @param id id of the scene
     */
    public void scene(int id) throws SQLException {
        String SQLDeleteSetupHasScene = "delete from setup_has_scene where scene_id=" + id + ";";
        String SQLDeleteChaseHasScene = "delete from chase_has_scene where scene_id=" + id + ";";

        // TODO delete effect and device statis

        try {

            Statement query = conn.createStatement();
            //	query.executeUpdate(SQLDeleteSceneSelf);
            query.executeUpdate(SQLDeleteSetupHasScene);
            query.executeUpdate(SQLDeleteChaseHasScene);

        } catch (SQLException e) {
            log.severe("Fail to Delete Scene " + e.toString());
            throw new SQLException(e);
        }

    }


    /**
     * delete a chase from setup
     *
     * @param id      id of the chase
     * @param setupid setup id
     */
    public void chase(int id, int setupid) throws SQLException {
        String SQLDeleteSetupHasChase = "delete from setup_has_chase where setUp_id=" + setupid + "and case_id=" + id + ";";

        // TODO delete effect and device statis

        try {
            Statement query = conn.createStatement();
            query.executeUpdate(SQLDeleteSetupHasChase);

        } catch (SQLException e) {
            log.severe("Fail to Delete Chase : " + e.toString());
            throw new SQLException(e);
        }

    }

    /**
     * Delete the effects From the Device Status
     * @param dsid device State id
     */
    protected void effectsFromDeviceState(int dsid) throws SQLException {
        String SQL = "delete from effect_status where device_status_id = " + dsid;
        Statement query = null;
        try {
            query = conn.createStatement();
            query.executeUpdate(SQL);
        } catch (SQLException throwables) {
            log.severe("Fail to Delete Effect from status id : " + throwables.toString());
            throw new SQLException(throwables);
        }
    }


    /**
     * delete all scenes from an chase
     *
     * @param chaseId id of the chase
     */
    protected void chaseHasScene(int chaseId) throws SQLException {

        String SQLDeleteChaseHasScene = "delete from chase_has_scene where case_id=" + chaseId + ";";

        try {
            Statement query = conn.createStatement();
            query.executeUpdate(SQLDeleteChaseHasScene);
        } catch (SQLException e) {
            log.severe("Fail to Delete Chase has Scene :" + e.toString());
            throw new SQLException(e);
        }

    }

}
