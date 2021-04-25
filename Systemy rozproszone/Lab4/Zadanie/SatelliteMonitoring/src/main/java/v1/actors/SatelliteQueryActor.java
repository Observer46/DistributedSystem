package v1.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import messages.DatabaseWriteResults;
import messages.DispatcherMessage;
import messages.MonitoringMessage;
import utils.SatelliteAPI;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.stream.IntStream;

public class SatelliteQueryActor extends AbstractBehavior<DispatcherMessage.SatelliteQuery> {

    public static ActorRef<DatabaseWriteResults> dbWriter;

    public SatelliteQueryActor(ActorContext<DispatcherMessage.SatelliteQuery> context){
        super(context);
    }

    public static Behavior<DispatcherMessage.SatelliteQuery> create() {
        return Behaviors.setup(SatelliteQueryActor::new);
    }

    @Override
    public Receive<DispatcherMessage.SatelliteQuery> createReceive() {
        return newReceiveBuilder()
                .onMessage(DispatcherMessage.SatelliteQuery.class, this::onSatelliteQuery)
                .build();
    }

    private Behavior<DispatcherMessage.SatelliteQuery> onSatelliteQuery(DispatcherMessage.SatelliteQuery query){
        int range = query.getFirstSatId() + query.getRange();
        Map<Integer, SatelliteAPI.Status> satelliteErrors = new HashMap<>();
        int completedQueries = 0;
        ExecutorService executor = Executors.newFixedThreadPool(query.getRange());
        List<Future<SatelliteAPI.Status>> satelliteResponses = new ArrayList<>();

        IntStream.range(query.getFirstSatId(), range)
                .forEach(satId -> satelliteResponses
                    .add(executor.submit(
                        () -> askSatellite(satId, query.getTimeout()))
                    )
                );

        for(int i=0; i < query.getRange(); i++){
            Future<SatelliteAPI.Status> response = satelliteResponses.get(i);
            try {
                if (response.get() != null){
                    completedQueries++;
                    if (response.get() != SatelliteAPI.Status.OK)
                        satelliteErrors.put(query.getFirstSatId() + i, response.get());
                }
            } catch (InterruptedException | ExecutionException ignored) { }
        }

        query.getSatelliteMonitor().tell(MonitoringMessage.SatelliteQueryResponse.builder()
                .queryId(query.getQueryId())
                .errorSatellites(satelliteErrors)
                .responseRatio(1f * completedQueries / query.getRange())
                .build());

        dbWriter.tell(DatabaseWriteResults.builder()
                .errorSatellites(satelliteErrors)
                .build());

        return Behaviors.stopped();
    }



    private SatelliteAPI.Status askSatellite(int satId, int timeout){
        Future<SatelliteAPI.Status> satelliteResponse = Executors.newSingleThreadExecutor()
                .submit(() -> SatelliteAPI.getStatus(satId));
        try {
            return satelliteResponse.get(timeout, TimeUnit.MILLISECONDS);
        }
        catch (TimeoutException ignored) {}
        catch (InterruptedException | ExecutionException e){
            e.printStackTrace();
        }

        return null;
    }
}
