import utils.DBManager;

public class Main2 {
    public static void main(String[] args){
        DBManager.prepareDB();
        DBManager.closeDB();
    }
}
