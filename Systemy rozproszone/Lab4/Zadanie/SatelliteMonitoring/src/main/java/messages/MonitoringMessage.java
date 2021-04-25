package messages;

import lombok.Builder;
import lombok.Getter;
import utils.SatelliteAPI;

import java.util.Map;

public class MonitoringMessage {
    public interface Monitoring {}

    @Builder
    @Getter
    public static class SatelliteQueryResponse implements Monitoring{
        private final int queryId;
        private final Map<Integer, SatelliteAPI.Status> errorSatellites;
        private final float responseRatio;
    }

    @Builder
    @Getter
    public static class DatabaseReadResponse implements Monitoring{
        private final int satId;
        private final int errorCount;
    }

    public static class InitiateSatelliteQuery implements Monitoring {}

    @Builder
    @Getter
    public static class InitiateDatabaseReadQuery implements Monitoring {
        private final int satId;
    }
}
