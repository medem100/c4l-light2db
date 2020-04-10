package c4l.db;

import java.sql.Connection;

public class Update {

    private Connection conn;
    private DB db;

    protected  Update (Connection conn){
        this.conn = conn;
        db =  DB.getInstance();
    }






}
