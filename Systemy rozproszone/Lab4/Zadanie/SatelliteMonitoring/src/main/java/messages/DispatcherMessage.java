package messages;

import akka.actor.typed.ActorRef;
import lombok.Builder;
import lombok.Getter;

public class DispatcherMessage {
    public interface Dispatcher {}

    @Getter
    @Builder
    public static class SatelliteQuery implements Dispatcher {
        private final int queryId;
        private final int firstSatId;
        private final int range;
        private final int timeout;
        private final ActorRef<MonitoringMessage.Monitoring> satelliteMonitor;
    }

    @Getter
    @Builder
    public static class DatabaseReadQuery implements Dispatcher {
        private final int satId;
        private final ActorRef<MonitoringMessage.Monitoring> satelliteMonitor;
    }
}
