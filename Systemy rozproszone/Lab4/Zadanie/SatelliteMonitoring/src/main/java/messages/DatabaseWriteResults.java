package messages;

import lombok.Builder;
import lombok.Getter;
import utils.SatelliteAPI;

import java.util.Map;

@Builder
@Getter
public class DatabaseWriteResults {
    private final Map<Integer, SatelliteAPI.Status> errorSatellites;
}
