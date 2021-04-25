package v2.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import messages.SatelliteMessage.SingleSatelliteRequest;
import messages.SatelliteMessage.SingleSatelliteResponse;
import utils.SatelliteAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

public class SatelliteActor2 extends AbstractBehavior<SingleSatelliteRequest> {
    public static Map<Integer, ActorRef<SingleSatelliteRequest>> satellites = new HashMap<>();

    private final int satId;

    public SatelliteActor2(ActorContext<SingleSatelliteRequest> context, int satId) {
        super(context);
        this.satId = satId;
        satellites.put(satId, context.getSelf());
    }

    public static Behavior<SingleSatelliteRequest> create(int satId){
        return Behaviors.setup(context -> new SatelliteActor2(context, satId).createReceive());
    }

    @Override
    public Receive<SingleSatelliteRequest> createReceive() {
        return newReceiveBuilder()
                .onMessage(SingleSatelliteRequest.class, this::onSingleSatelliteRequest)
                .build();
    }

    private Behavior<SingleSatelliteRequest> onSingleSatelliteRequest(SingleSatelliteRequest request){
        Executors.newSingleThreadExecutor().submit(() -> askSatellite(request));
        return this;
    }

    private void askSatellite( SingleSatelliteRequest request){
        Future<SatelliteAPI.Status> satelliteResponse = Executors.newSingleThreadExecutor()
                .submit(() -> SatelliteAPI.getStatus(satId));
        SingleSatelliteResponse response = null;
        try {
            response = SingleSatelliteResponse.builder()
                    .satId(satId)
                    .status(satelliteResponse.get(request.getTimeout(), TimeUnit.MILLISECONDS))
                    .build();
        }
        catch (TimeoutException ignored) {
            response = SingleSatelliteResponse.builder()
                    .satId(satId)
                    .status(null)
                    .build();
        }
        catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
        }

        request.getQueryWorkerActor().tell(response);
    }

}
