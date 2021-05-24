package agh.sr.REST.api.stormglass.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeatherRequest {
    private String searchName;
    private String params;
    private String start;
    private String end;
}
