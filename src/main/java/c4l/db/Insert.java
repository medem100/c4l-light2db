package c4l.db;

import java.sql.Connection;
import java.util.logging.Logger;

public class Insert {

    Connection conn;
    Logger log ;

    protected Insert(Connection conn){
        this.conn = conn;
        log = Logger.getLogger(Insert.class.getName());
    }



}
