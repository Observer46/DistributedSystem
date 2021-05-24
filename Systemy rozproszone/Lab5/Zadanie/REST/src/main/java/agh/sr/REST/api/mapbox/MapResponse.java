package agh.sr.REST.api.mapbox;

import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MapResponse {
    private String lat;
    private String lng;
}
