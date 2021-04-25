package messages;

import akka.actor.typed.ActorRef;
import lombok.Builder;
import lombok.Getter;
import utils.SatelliteAPI;

public class SatelliteMessage {
    public interface QueryWorkerMessage {}

    @Builder
    @Getter
    public static class SingleSatelliteResponse implements QueryWorkerMessage {
        private final SatelliteAPI.Status status;
        private final int satId;
    }

    @Builder
    @Getter
    public static class SingleSatelliteRequest {
        private final ActorRef<QueryWorkerMessage> queryWorkerActor;
        private final int timeout;
    }
}
