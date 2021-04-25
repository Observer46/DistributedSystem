package v2.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import messages.DatabaseWriteResults;
import messages.DispatcherMessage;
import messages.MonitoringMessage;
import messages.SatelliteMessage;
import utils.SatelliteAPI;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class SatelliteQueryActor2 extends AbstractBehavior<SatelliteMessage.QueryWorkerMessage> {

    public static ActorRef<DatabaseWriteResults> dbWriter;

    private int singleSatelliteDoneQueries = 0;
    private int successfullyCompletedQueries = 0;
    private Map<Integer, SatelliteAPI.Status> satelliteErrors = new HashMap<>();
    private DispatcherMessage.SatelliteQuery queryToDo;

    public SatelliteQueryActor2(ActorContext<SatelliteMessage.QueryWorkerMessage> context){
        super(context);
    }

    public static Behavior<SatelliteMessage.QueryWorkerMessage> create() {
        return Behaviors.setup(SatelliteQueryActor2::new);
    }

    @Override
    public Receive<SatelliteMessage.QueryWorkerMessage> createReceive() {
        return newReceiveBuilder()
                .onMessage(DispatcherMessage.SatelliteQuery.class, this::onSatelliteQuery)
                .onMessage(SatelliteMessage.SingleSatelliteResponse.class, this::onSingleSatelliteResponse)
                .build();
    }

    private Behavior<SatelliteMessage.QueryWorkerMessage> onSatelliteQuery(DispatcherMessage.SatelliteQuery query){
        int range = query.getFirstSatId() + query.getRange();
        queryToDo = query;

        IntStream.range(query.getFirstSatId(), range)
                .forEach(satId -> SatelliteActor2.satellites
                        .get(satId)
                        .tell(SatelliteMessage.SingleSatelliteRequest.builder()
                                .queryWorkerActor(getContext().getSelf())
                                .timeout(query.getTimeout())
                                .build()));
        return this;
    }

    private Behavior<SatelliteMessage.QueryWorkerMessage> onSingleSatelliteResponse
            (SatelliteMessage.SingleSatelliteResponse response){

        singleSatelliteDoneQueries++;

        if(response.getStatus() != null){
            successfullyCompletedQueries++;
            if(response.getStatus() != SatelliteAPI.Status.OK)
                satelliteErrors.put(response.getSatId(), response.getStatus());
        }

        if(queryToDo.getRange() <= singleSatelliteDoneQueries) {
            queryToDo.getSatelliteMonitor().tell(MonitoringMessage.SatelliteQueryResponse.builder()
                    .queryId(queryToDo.getQueryId())
                    .errorSatellites(satelliteErrors)
                    .responseRatio(1f * successfullyCompletedQueries / queryToDo.getRange())
                    .build());

            dbWriter.tell(DatabaseWriteResults.builder()
                    .errorSatellites(satelliteErrors)
                    .build());

            return Behaviors.stopped();
        }

        return this;
    }
}
