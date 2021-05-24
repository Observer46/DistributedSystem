package agh.sr.REST.account;

import agh.sr.REST.account.model.LoginRequest;
import agh.sr.REST.account.model.RegisterRequest;
import agh.sr.REST.api.WeatherService;
import agh.sr.REST.api.model.WeatherResponse;
import agh.sr.REST.db.DBService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

@Controller
@AllArgsConstructor
@Slf4j
public class AccountController {
    private final DBService dbService;
    private final HTMLService htmlService;
    private final WeatherService weatherService;

    @GetMapping(value = "logout",
        produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> logout() {
        dbService.logout();
        return ResponseEntity.ok(htmlService.parseHtml("index.html"));
    }

    @PostMapping(value = "login",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> login(LoginRequest request) throws SQLException {
        if (dbService.login(request.getEmail(), request.getPassword())) {
            log.info("User logged in: " + dbService.getDbManager().getCurrentUserEmail());
            return ResponseEntity.ok(htmlService.menuHtmlSite(dbService.getDbManager().getCurrentUserEmail()));
        }
        return new ResponseEntity(htmlService.loginErrorHtml(), HttpStatus.BAD_REQUEST);
    }

    @PostMapping(value = "register",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE,
            produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> register(RegisterRequest request){
        if(!request.getEmail().matches("\\w+@\\w+.\\w+"))
            return new ResponseEntity(htmlService.registerMessageHtml("Error: Email has incorrect structure!"), HttpStatus.BAD_REQUEST);
        if(request.getPassword().length() < 8)
            return new ResponseEntity(htmlService.registerMessageHtml("Error: Too short password!"), HttpStatus.BAD_REQUEST);
        if (!request.getPassword().equals(request.getRepeatedPassword()))
            return new ResponseEntity(htmlService.registerMessageHtml("Error: Passwords did not match!"), HttpStatus.BAD_REQUEST);

        if(dbService.register(request.getEmail(), request.getPassword())) {
            log.info("User registered: " + request.getEmail());
            return ResponseEntity.ok(htmlService.registerMessageHtml("Signed up successfully!"));
        }

        return new ResponseEntity(htmlService.registerMessageHtml("Error: Email in use!"), HttpStatus.CONFLICT);
    }

    @GetMapping(value = "menu", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> menu(){
        return ResponseEntity.ok(htmlService.menuHtmlSite(dbService.getDbManager().getCurrentUserEmail()));
    }

    @GetMapping(value = "history", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getUserHistory() throws SQLException {
        ResultSet history = dbService.getUserResults();
        String content = htmlService.parseHistoryToHtml(history);
        String htmlSite = "<div><head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Weather you like it!</title>\n" +
                "</head>\n" +
                "<body>\n" +
                "<div><form action=\"/menu\" method=\"get\"><input type=\"submit\" value=\"Back\"></form>" +
                "<div>\n" +
                "  <h1>Saved results:</h1>\n" +
                "</div>" +
                "<div>" + content + "</div>" +
                "</div>";
        return ResponseEntity.ok(htmlSite);
    }

    @GetMapping(value = "history/content", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> getHistoryContent(@RequestParam int contentId){
        Map<String, String> content = dbService.getContentResults(contentId);
        WeatherResponse results = weatherService.buildWeatherResponse(content.get("jsonResults"));
        String table = weatherService.parseResponseToHtmlTable(results);

        String airTempInfo = results.getAirTemperature().size() > 0 ? "<div><h5>Air temperature info:</h5> " +
                weatherService.analyzeData(results.getAirTemperature(), results.getHours()) + "</div>"
                : "";

        String humidityInfo = results.getHumidity().size() > 0 ? "<div><h5>Humidity info:</h5> " +
                weatherService.analyzeData(results.getHumidity(), results.getHours()) + "</div>"
                : "";

        String windSpeedInfo = results.getWindSpeed().size() > 0 ? "<div><h5>Wind speed info:</h5> " +
                weatherService.analyzeData(results.getWindSpeed(), results.getHours()) + "</div>"
                : "";


        String htmlSite = "<html>\n" +
                "<header><title>Result</title></header>" +
                "<body>" +
                "<div><form action=\"/menu\" method=\"get\"><input type=\"submit\" value=\"Back\"></form>" +
                "<div><form action=\"/weather/delete\" method=\"post\"><input type=\"submit\" value=\"Delete\"></form>" +
                "<div><form action=\"/weather/rename\" method=\"post\">" +
                "<label for=\"name\">Rename results:</label>" +
                "<input type=\"text\" id=\"name\" name=\"name\"><br>\n" +
                "<input type=\"submit\" value=\"Rename\"></form>" +
                "<div>\n" +
                "  <h1>" + content.get("name") + "</h1>\n" +
                "</div>" +
                "</div>" + airTempInfo + humidityInfo + windSpeedInfo +
                "<div>" + table + "</div>" +
                "</body>";
        return ResponseEntity.ok(htmlSite);
    }


    @PostMapping(value = "weather/save", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> saveContent(String name){
        dbService.saveContentResults(name, weatherService.getResString());
        return ResponseEntity.ok(htmlService.parseHtml("saved.html"));
    }

    @PostMapping(value = "weather/rename", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> renameContent(String name){
        dbService.renameResult(name);
        return ResponseEntity.ok(htmlService.parseHtml("updated.html"));
    }

    @PostMapping(value = "weather/delete", produces = MediaType.TEXT_HTML_VALUE)
    public ResponseEntity<String> deleteContent(){
        dbService.deleteResult();
        return ResponseEntity.ok(htmlService.parseHtml("deleted.html"));
    }
}
