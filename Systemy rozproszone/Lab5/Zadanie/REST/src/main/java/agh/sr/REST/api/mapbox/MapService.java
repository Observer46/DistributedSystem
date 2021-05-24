package agh.sr.REST.api.mapbox;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class MapService {
    private static final String API_KEY = "pk.eyJ1IjoibWF0cGF3MjQiLCJhIjoiY2twMzNhaGJ5MDcyaTJwczU3cTc5ZWFhbCJ9.wDhI3JNamQyVnsD6ziBvWw";
    private static final String MAP_ENDPOINT = "https://api.mapbox.com/geocoding/v5/mapbox.places/";

    public MapResponse getCoords(String searchString) throws IOException {

        URL url = new URL(MAP_ENDPOINT + URLEncoder.encode(searchString, StandardCharsets.UTF_8) + ".json?access_token=" + API_KEY);
        HttpURLConnection con = (HttpURLConnection) url.openConnection();
        con.setRequestMethod("GET");
        log.info("Send request to map API...");

        int status = con.getResponseCode();
        log.info("Response code for map API: " + status);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuilder content = new StringBuilder();
        while ((inputLine = in.readLine()) != null) {
            content.append(inputLine);
        }
        in.close();
        con.disconnect();

        JSONObject requestResult = new JSONObject(content.toString());
        JSONArray coords = ((JSONObject) requestResult.getJSONArray("features").get(0)).getJSONArray("center");
        Object lng = coords.get(0);
        Object lat = coords.get(1);

        return MapResponse.builder()
                .lat(lat.toString())
                .lng(lng.toString())
                .build();
    }
}
