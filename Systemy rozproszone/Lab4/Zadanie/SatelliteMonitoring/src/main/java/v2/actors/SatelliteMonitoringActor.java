package v2.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import messages.DispatcherMessage;
import messages.MonitoringMessage;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class SatelliteMonitoringActor extends AbstractBehavior<MonitoringMessage.Monitoring> {
    private final Map<Integer, Long> queryStartTime = new HashMap<>();
    private int queryCounter = 0;
    private Random rand = new Random();

    public SatelliteMonitoringActor(ActorContext<MonitoringMessage.Monitoring> context) {
        super(context);
    }

    public static Behavior<MonitoringMessage.Monitoring> create(){
        return Behaviors.setup(SatelliteMonitoringActor::new);
    }

    @Override
    public Receive<MonitoringMessage.Monitoring> createReceive() {
        return newReceiveBuilder()
                .onMessage(MonitoringMessage.SatelliteQueryResponse.class, this::onSatelliteQueryResponse)
                .onMessage(MonitoringMessage.DatabaseReadResponse.class, this::onDatabaseReadResponse)
                .onMessage(MonitoringMessage.InitiateSatelliteQuery.class, this::onInitiateSatelliteQuery)
                .onMessage(MonitoringMessage.InitiateDatabaseReadQuery.class, this::onInitiateDatabaseReadQuery)
                .build();
    }

    public void createSatelliteQuery(int firstSatId, int range, int timeout){
        DispatcherActor2.dispatcher.tell(DispatcherMessage.SatelliteQuery.builder()
                .queryId(queryCounter)
                .firstSatId(firstSatId)
                .range(range)
                .timeout(timeout)
                .satelliteMonitor(getContext().getSelf())
                .build());
        queryStartTime.put(queryCounter++, System.currentTimeMillis());
    }

    public void createDatabaseReadQuery(int satId){
        DispatcherActor2.dispatcher.tell(DispatcherMessage.DatabaseReadQuery.builder()
                .satId(satId)
                .satelliteMonitor(getContext().getSelf())
                .build());
    }

    private Behavior<MonitoringMessage.Monitoring> onSatelliteQueryResponse
            (MonitoringMessage.SatelliteQueryResponse response){

        long timeForResponse = System.currentTimeMillis() - queryStartTime.get(response.getQueryId());
        synchronized (System.out) {
            System.out.println("Monitoring station name: " + getContext().getSelf().path().name());
            System.out.println("Query ID: " + response.getQueryId());
            System.out.println("Response time: " + timeForResponse);
            System.out.println("In-time response ratio: " + response.getResponseRatio());
            System.out.println("Number of errors: " + response.getErrorSatellites().size());
            response.getErrorSatellites()
                    .forEach((integer, status) ->
                            System.out.println("SatID: " + integer + ", error: " + status));
            System.out.println();
        }
        return this;
    }

    private Behavior<MonitoringMessage.Monitoring> onDatabaseReadResponse
            (MonitoringMessage.DatabaseReadResponse response){

        if(response.getErrorCount() > 0)
            System.out.println("SatID: " + response.getSatId() +
                    ", error count: " + response.getErrorCount());
        return this;
    }

    private Behavior<MonitoringMessage.Monitoring> onInitiateSatelliteQuery
            (MonitoringMessage.InitiateSatelliteQuery request){

        int firstSatId = 100 + rand.nextInt(50);
        int range = 50;
        int timeout = 300;

        createSatelliteQuery(firstSatId, range, timeout);
        return this;
    }

    private Behavior<MonitoringMessage.Monitoring> onInitiateDatabaseReadQuery
            (MonitoringMessage.InitiateDatabaseReadQuery request){

        createDatabaseReadQuery(request.getSatId());
        return this;
    }
}
