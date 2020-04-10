package c4l.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DB {

    private static Logger logger;


    private static DB OBJ = getInstance();
    private static Connection conn = null;
//    public Select Select = new Select();
//    public Insert Insert = new Insert();
//    public Update Update = new Update();
//    public Create Create = new Create();

    //static Logger logger = Logger.getLogger(DB.class);

    public static void main(String[] args) {
        DB db = DB.getInstance();
    }


    private DB() {
        String path = DB.class.getClassLoader()
                .getResource("logging.properties")
                .getFile();
        System.setProperty("java.util.logging.config.file", path);
        logger = Logger.getLogger(DB.class.getName());

        conn = getConnection();

    }

    public static synchronized DB getInstance() {
        if (DB.OBJ == null) {
            logger.config("Create new db instance");
            DB.OBJ = new DB();
        }
        return DB.OBJ;
    }

    private Connection getConnection() {
        try {
            logger.config("Create ne Connection");
            Class.forName("com.mysql.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://" + Constants.dbHost + ":"
                    + Constants.dbPort + "/" + Constants.database + "?" + "user=" + Constants.dbUser + "&"
                    + "password=" + Constants.dbPassword);
            return conn;
        } catch (Exception e) {
            logger.severe("connection can not be Create");

            return null;
        }

    }

}