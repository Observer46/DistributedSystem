package agh.sr.REST.db;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.PreDestroy;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class DBManager {
    private static final String LOGIN_TABLE = "logins";
    private static final String RESULTS_TABLE = "results";
    private static final String DB_PATH = "jdbc:sqlite:restDatabase.db";

    private Connection connection;
    private int currentUserId = -1;

    @Getter
    private String currentUserEmail = "";

    private int selectedResultId = -1;

    public DBManager(){
        prepareDB();
    }

    public void resetUser() {
        currentUserId = -1;
        selectedResultId = -1;
        currentUserEmail = "";
    }

    public void prepareDB(){
        try {
            connection = DriverManager.getConnection(DBManager.DB_PATH);
            if(connection == null){
                log.error("Opening connection to DB failed!");
                System.exit(1);
            }
            prepareTables();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    private void prepareTables(){
        String createLoginTableString =
                "CREATE TABLE IF NOT EXISTS " + LOGIN_TABLE + "  ("
                        + "     id INTEGER PRIMARY KEY,"
                        + "     email TEXT NOT NULL UNIQUE,"
                        + "     password TEXT NOT NULL"
                        + ")";
        String createSavedResultsTable =
                "CREATE TABLE IF NOT EXISTS " + RESULTS_TABLE + "  ("
                        + "     id INTEGER PRIMARY KEY,"
                        + "     ownerId INTEGER NOT NULL,"
                        + "     name TEXT NOT NULL,"
                        + "     parsedHtmlResults TEXT NOT NULL,"
                        + "     FOREIGN KEY (ownerId)"
                        + "         REFERENCES " + LOGIN_TABLE + "(id)"
                        + ")";
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(createLoginTableString);
            stmt.executeUpdate(createSavedResultsTable);
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    public boolean addSavedResult(String name, String jsonResults){
        String insertString = "INSERT INTO " + RESULTS_TABLE + " (ownerId, name, parsedHtmlResults) "
                + "VALUES(" + currentUserId + ",\"" + name +"\", \"" + jsonResults.replace("\"", "\\") + "\")";
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(insertString);
            return true;

        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public Map<String, String> getResult(int id){
        String readString =
                "SELECT name, parsedHtmlResults "
                        + "FROM " + RESULTS_TABLE + " "
                        + "WHERE id = " + id;
        try {
            Statement stmt = connection.createStatement();
            ResultSet res = stmt.executeQuery(readString);
            if(res.next()){
                Map<String, String> results = new HashMap<>();
                results.put("name", res.getString("name"));
                results.put("jsonResults", res.getString("parsedHtmlResults").replace("\\","\""));
                selectedResultId = id;
                return results;
            }
            return null;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }

    public ResultSet getResultsNames() {
        String readString = "SELECT id, name "
                + "FROM " + RESULTS_TABLE + " "
                + "WHERE ownerId = " + currentUserId;
        try {
            Statement stmt = connection.createStatement();
            ResultSet res = stmt.executeQuery(readString);
            return res;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return null;
    }
        public boolean login(String email, String password){
        String readString =
                "SELECT id, email, password "
                        + "FROM " + LOGIN_TABLE + " "
                        + "WHERE email = \"" + email + "\""
                            + " AND password = \"" + password + "\"";
        try {
            Statement stmt = connection.createStatement();
            ResultSet res = stmt.executeQuery(readString);
            if(res.next()){
                currentUserId = res.getInt("id");
                currentUserEmail = email;
                return true;
            }
            return false;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean register(String email, String password){
        String insertString = "INSERT INTO " + LOGIN_TABLE + " (email, password) "
                + "VALUES( \"" + email + "\", \"" + password + "\")";
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(insertString);
            return true;
        } catch (SQLException throwables) {
            System.out.println("ERRRRRROR!");
            throwables.printStackTrace();
        }
        return false;
    }

    @PreDestroy
    public void closeDB(){
        log.info("Closing connection to DB...");
        try {
            String dropTableString = "DROP TABLE IF EXISTS " + LOGIN_TABLE;
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(dropTableString);
            connection.close();
            log.info("Connection to DB has been closed");
        } catch (SQLException throwables) {
            log.info("Closing connection to DB failed!");
            throwables.printStackTrace();
        }
    }

    public boolean renameResult(String newName) {
        String readString =
                "UPDATE " + RESULTS_TABLE + " SET " +
                        "name=\"" + newName + "\"" +
                        "WHERE id=" + selectedResultId;
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(readString);
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }

    public boolean deleteResult() {
        String readString =
                "DELETE FROM " + RESULTS_TABLE + " " +
                        "WHERE id=" + selectedResultId;
        try {
            Statement stmt = connection.createStatement();
            stmt.executeUpdate(readString);
            return true;
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return false;
    }
}
