package c4l.db;

import java.sql.*;
import java.util.logging.Logger;

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
    public Integer chase(String name, String description, int setupID, int[] sceneIds, int[] fadeTimes, int[] showTimes) {
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
            return null;
        }

    }

    /**
     * insert the Scenes for a Chase
     * @param chaseid id of the chase
     * @param scnenIds scene ids where are add to the chase
     * @param fadeTimes fade times between the scenes of the Chase
     * @param showTimes show times of the different scenes
     */
    protected void chaseHasScene(int chaseid, int[] scnenIds, int[] fadeTimes, int[] showTimes) {
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
        }
    }



}
