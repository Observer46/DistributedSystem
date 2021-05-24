package agh.sr.REST.api.model;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WeatherRequest {
    private String lat;
    private String lng;
    private String params;
    private String start;
    private String end;
}
