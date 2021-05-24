package agh.sr.REST.db;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Service
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class DBService {
    @Getter
    private final DBManager dbManager;

    public boolean login(String email, String password) throws SQLException {
        return dbManager.login(email, password);
    }

    public void logout() {
        dbManager.resetUser();
    }

    public boolean register(String email, String password){
        return dbManager.register(email, password);
    }

    public ResultSet getUserResults(){
        return dbManager.getResultsNames();
    }

    public Map<String, String> getContentResults(int contentId) { return dbManager.getResult(contentId); }

    public boolean saveContentResults(String name, String results) { return dbManager.addSavedResult(name, results); }

    public boolean renameResult(String newName) { return dbManager.renameResult(newName); }

    public boolean deleteResult() { return dbManager.deleteResult(); }
}
