package agh.sr.REST.api;

import agh.sr.REST.account.HTMLService;
import agh.sr.REST.api.model.WeatherRequest;
import agh.sr.REST.api.model.WeatherResponse;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@AllArgsConstructor
@Slf4j
public class WeatherController {
    private final WeatherService weatherService;
    private final HTMLService htmlService;

    @GetMapping(value = "weather", produces = MediaType.TEXT_HTML_VALUE)
    public String getWeather(@RequestParam String lng,
                             @RequestParam String lat,
                             @RequestParam(required = false) String airTemperature,
                             @RequestParam(required = false) String humidity,
                             @RequestParam(required = false) String windSpeed,
                             @RequestParam String start,
                             @RequestParam String end){

        StringBuilder paramsSb = new StringBuilder();
        if (airTemperature != null)
            paramsSb.append("airTemperature,");
        if (humidity != null)
            paramsSb.append("humidity,");
        if (windSpeed != null)
            paramsSb.append("windSpeed,");

        if (paramsSb.length() == 0){
            String menu = htmlService.parseHtml("menu.html");
            String div = "<div><h3>At least one checkbox must be selected!</h3></div>";
            return htmlService.addDiv(menu, div, 3);
        }

        String params = paramsSb.substring(0, paramsSb.length() - 1);
        WeatherRequest request = WeatherRequest.builder()
                .lat(lat)
                .lng(lng)
                .params(params)
                .start(weatherService.convertDateToUTCTimestamp(start))
                .end(weatherService.convertDateToUTCTimestamp(end))
                .build();
        try {

            WeatherResponse response = weatherService.getWeather(request);
            String table = weatherService.parseResponseToHtmlTable(response);
            String airTempInfo = airTemperature != null ? "<div><h5>Air temperature info:</h5> " +
                    weatherService.analyzeData(response.getAirTemperature(), response.getHours()) + "</div>"
                    : "";

            String humidityInfo = airTemperature != null ? "<div><h5>Humidity info:</h5> " +
                    weatherService.analyzeData(response.getHumidity(), response.getHours()) + "</div>"
                    : "";

            String windSpeedInfo = airTemperature != null ? "<div><h5>Wind speed info:</h5> " +
                    weatherService.analyzeData(response.getWindSpeed(), response.getHours()) + "</div>"
                    : "";


            return "<html>\n" +
                    "<header><title>Result</title></header>" +
                    "<body><h1>Response:</h1>" +
                    "<div><form action=\"/weather/save\" method=\"post\"><label for=\"name\">Save results - enter name:</label>" +
                    "<input type=\"text\" id=\"name\" name=\"name\"><br>"+
                    "<input type=\"submit\" value=\"Save\"></form>" +
                    "<form action=\"/menu\" method=\"get\"><input type=\"submit\" value=\"Back\"></form>" +
                    "</div>" + airTempInfo + humidityInfo + windSpeedInfo +
                    "<div>" + table + "</div>" +
                    "</body>";

        } catch (IOException e) {
            e.printStackTrace();
        }

        return "<html>\n" +
                "<header><title>Request failed!</title></header>" +
                "<body><div><h1>Weather request failed!</h1></div></body>";
    }
}
