package c4l.db;

public class DB {

    private static DB OBJ = getInstance();
//    public Select Select = new Select();
//    public Insert Insert = new Insert();
//    public Update Update = new Update();
//    public Create Create = new Create();

    //static Logger logger = Logger.getLogger(DB.class);
//    private DB() {
//
//
//    }

    public static synchronized DB getInstance() {
        if (DB.OBJ == null) {
            DB.OBJ = new DB();
        }
        return DB.OBJ;
    }

}