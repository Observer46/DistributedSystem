package agh.sr.REST.api.stormglass.model;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Builder
@Getter
public class WeatherResponse {
    private List<String> hours;
    private List<String> airTemperature;
    private List<String> humidity;
    private List<String> windSpeed;
}
