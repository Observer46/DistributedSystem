package agh.sr.REST.api;

import agh.sr.REST.api.model.AnalyzedData;
import agh.sr.REST.api.model.WeatherRequest;
import agh.sr.REST.api.model.WeatherResponse;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class WeatherService {
    private static final String API_KEY = "0efbe204-b4b8-11eb-80d0-0242ac130002-0efbe2d6-b4b8-11eb-80d0-0242ac130002";
    private static final String WEATHER_ENDPOINT = "https://api.stormglass.io/v2/weather/point?";

    @Getter
    private String resString;

    public WeatherResponse getWeather(WeatherRequest request) throws IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("lat", request.getLat());
        params.put("lng", request.getLng());
        params.put("params", request.getParams());
        params.put("start", request.getStart());
        params.put("end", request.getEnd());

        URL url = new URL(WEATHER_ENDPOINT + convertParams(params));
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", API_KEY);
        log.info("Send request to API with params: " + params);

        int status = con.getResponseCode();
        log.info("Response code for weather API: " + status);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        JSONObject requestResult = new JSONObject(content.toString());
        resString = requestResult.getJSONArray("hours").toString();
        return buildWeatherResponse(requestResult.getJSONArray("hours").toString());
    }

    private String convertParams(Map<String, Object> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();

        for(var entry : params.entrySet()){
            result.append(URLEncoder.encode(entry.getKey(), StandardCharsets.UTF_8));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue().toString(), StandardCharsets.UTF_8));
            result.append("&");
        }

        if (result.length() > 0)
            result.setLength(result.length() - 1);

        return result.toString();
    }

    public String parseResponseToHtmlTable(WeatherResponse response) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean useAirTemp = response.getAirTemperature().size() > 0;
        boolean useHumidity = response.getHumidity().size() > 0;
        boolean useWindSpeed = response.getWindSpeed().size() > 0;
        int resCount = Math.max(Math.max(response.getAirTemperature().size(),
                response.getHumidity().size()),
                response.getWindSpeed().size());

        stringBuilder.append("<table border=\"1\"><tr><th>Time (UTC)</th>");
        if(useAirTemp)  stringBuilder.append("<th>Air temperature (°C)</th>");
        if(useHumidity)  stringBuilder.append("<th>Humidity (%)</th>");
        if(useWindSpeed)  stringBuilder.append("<th>Wind speed (m/s)</th>");
        stringBuilder.append("</tr>");

        for(int i=0; i < resCount; i++){
            String time = response.getHours().get(i);
            stringBuilder.append("<tr><td>" + time + "</td>");
            if(useAirTemp) appendData(stringBuilder, response.getAirTemperature().get(i));
            if(useHumidity) appendData(stringBuilder, response.getHumidity().get(i));
            if(useWindSpeed) appendData(stringBuilder, response.getWindSpeed().get(i));
            stringBuilder.append("</tr>");
        }

        stringBuilder.append("</table>");
        return stringBuilder.toString();
    }

    public void appendData(StringBuilder sb, String data){
        sb.append("<td>" + data + "</td>");
    }

    public String convertDateToUTCTimestamp(String date){
        date += " 00:00:00";
        Timestamp timestamp = Timestamp.valueOf(date);
        return Long.toString(timestamp.getTime() / 1000);
    }

    public String analyzeData(List<String> data, List<String> time){
        List<Map<String, Float>> parsedData = new ArrayList<>();
        for(int i=0; i < data.size(); ++i){
            String singleHour = data.get(i);
            String[] dataFromSources = singleHour.split(",");
            Map<String, Float> singleHourResult = new HashMap<>();
            for(int j=0; j < dataFromSources.length; ++j){
                String[] sourceAndValue = dataFromSources[j].split(":");
                String source = sourceAndValue[0];
                Float value = Float.parseFloat(sourceAndValue[1].trim());
                singleHourResult.put(source, value);
            }
            parsedData.add(singleHourResult);
        }

        AnalyzedData minData = getMin(parsedData);
        AnalyzedData maxData = getMax(parsedData);
        Float avg = getAvg(parsedData);
        Float var = getVar(parsedData, avg);

        String dataInfo = "Min: " + minData.getValue() + " (time: " + time.get(minData.getIndex()) + ", source: " + minData.getSource() + ")<br>" +
                "Max: " + maxData.getValue() + " (time: " + time.get(maxData.getIndex()) + ", source: " + maxData.getSource() + ")<br>" +
                "Avg: " + avg + "<br>" +
                "Variance: " + var + "<br>";

        return dataInfo;
    }

    public AnalyzedData getMax(List<Map<String, Float>> parsedData){
        Float maxVal = null;
        String maxValSource = null;
        Integer index = null;
        int idx = 0;

        for(var dataAndSource : parsedData){
            for(var data : dataAndSource.entrySet()){
                if (maxVal == null || maxVal < data.getValue()) {
                    maxVal = data.getValue();
                    maxValSource = data.getKey();
                    index = idx;
                }
            }
            ++idx;
        }

        return AnalyzedData.builder()
                .value(maxVal)
                .source(maxValSource)
                .index(index)
                .build();
    }

    public AnalyzedData getMin(List<Map<String, Float>> parsedData){
        Float minVal = null;
        String minValSource = null;
        Integer index = null;
        int idx = 0;

        for(var dataAndSource : parsedData){
            for(var data : dataAndSource.entrySet()){
                if (minVal == null || minVal > data.getValue()) {
                    minVal = data.getValue();
                    minValSource = data.getKey();
                    index = idx;
                }
            }
            ++idx;
        }

        return AnalyzedData.builder()
                .value(minVal)
                .source(minValSource)
                .index(index)
                .build();
    }

    public Float getAvg(List<Map<String, Float>> parsedData) {
        Float sum = 0.f;
        int counter = 0;

        for(var dataAndSource : parsedData){
            for(var data : dataAndSource.entrySet()) {
                sum += data.getValue();
                ++counter;
            }
        }

        return sum / counter;
    }
    public Float getVar(List<Map<String, Float>> parsedData, Float avg) {
        Float sum = 0.f;
        int counter = 0;

        for(var dataAndSource : parsedData){
            for(var data : dataAndSource.entrySet()) {
                sum += (data.getValue() - avg) * (data.getValue() - avg);
                ++counter;
            }
        }

        return sum / counter;
    }


    public WeatherResponse buildWeatherResponse(String jsonString){
        List<String> hours = new ArrayList<>();
        List<String> airTemperature = new ArrayList<>();
        List<String> humidity = new ArrayList<>();
        List<String> windSpeed = new ArrayList<>();
        List<Object> JSONHours = new JSONArray(jsonString).toList();

        for(Object o : JSONHours){
            HashMap<String, Object> jsonObject = (HashMap) o;
            String hourString = (String) jsonObject.get("time");

            String[] hourParts = hourString.split("T");
            String[] hourParts2 = hourParts[1].split("\\+");
            hours.add(hourParts[0] + " " + hourParts2[0]);

            StringBuilder res1 = new StringBuilder();
            StringBuilder res2 = new StringBuilder();
            StringBuilder res3 = new StringBuilder();

            HashMap<String, Object> temperatures = (HashMap<String, Object>) jsonObject.get("airTemperature");
            if(temperatures != null) {
                temperatures.forEach((s, t) -> res1.append(s + ": " + t.toString() + ","));
                airTemperature.add(res1.substring(0, res1.length() - 1));
            }

            HashMap<String, Object> humidities = (HashMap<String, Object>) jsonObject.get("humidity");
            if(humidities != null) {
                humidities.forEach((s, t) -> res2.append(s + ": " + t.toString() + ","));
                humidity.add(res2.substring(0, res2.length() - 1));
            }

            HashMap<String, Object> windSpeeds = (HashMap<String, Object>) jsonObject.get("windSpeed");
            if(windSpeeds != null) {
                windSpeeds.forEach((s, t) -> res3.append(s + ": " + t.toString() + ","));
                windSpeed.add(res3.substring(0, res3.length() - 1));
            }
        }
        return WeatherResponse.builder()
                .hours(hours)
                .airTemperature(airTemperature)
                .humidity(humidity)
                .windSpeed(windSpeed)
                .build();
    }
}
