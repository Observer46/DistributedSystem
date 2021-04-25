package utils;

import java.sql.*;
import java.util.stream.IntStream;

public class DBManager {
    public static Connection connection;
    private static final String TABLE_NAME = "satellites";
    private static final String DB_PATH = "jdbc:sqlite:satelliteDatabase.db";

    public static void prepareDB(){
        try {
            DBManager.connection = DriverManager.getConnection(DBManager.DB_PATH);
            if(DBManager.connection == null){
                System.out.println("Opening connection to DB failed!");
                System.exit(1);
            }
            DBManager.createSatelliteTable();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static void createSatelliteTable(){
        String createTableString =
                "CREATE TABLE " + TABLE_NAME + " ("
                + "     id integer PRIMARY KEY,"
                + "     errors integer NOT NULL"
                + ")";
        try {
            Statement stmt = DBManager.connection.createStatement();
            stmt.executeUpdate(createTableString);
            IntStream.range(100, 200)
                    .forEach(satId -> insertSatellite(satId));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static ResultSet readSatellite(int satId){
        String readString =
                "SELECT id, errors "
                + "FROM " + TABLE_NAME + " "
                + "WHERE id = " + satId;
        try {
            Statement stmt = DBManager.connection.createStatement();
            return stmt.executeQuery(readString);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public static void updateSatellite(int satId, int errorCount){
        String updateString =
                "UPDATE " + TABLE_NAME + " "
                + "SET errors = errors + " + errorCount + " "
                + "WHERE id = " + satId;
        try {
            Statement stmt = DBManager.connection.createStatement();
            stmt.executeUpdate(updateString);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private static void insertSatellite(int satId){
        String insertString = "INSERT INTO " + TABLE_NAME + "(id, errors) "
                + "VALUES(" + satId + ", 0)";
        try {
            Statement stmt = DBManager.connection.createStatement();
            stmt.executeUpdate(insertString);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public static void closeDB(){
        try {
            String dropTableString = "DROP TABLE IF EXISTS " + TABLE_NAME;
            Statement stmt = DBManager.connection.createStatement();
            stmt.executeUpdate(dropTableString);
            DBManager.connection.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }
}
