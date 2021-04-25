package v2;

import akka.actor.typed.*;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import messages.MonitoringMessage;
import utils.DBManager;
import v2.actors.*;

import java.io.File;
import java.util.stream.IntStream;

public class Main2 {
    public static Behavior<Void> create(){
        return Behaviors.setup(
                context -> {
                    DBManager.prepareDB();

                    IntStream.range(100, 200).forEach(satId -> context.spawn(
                            Behaviors.supervise(SatelliteActor2.create(satId))
                            .onFailure(Exception.class, SupervisorStrategy.resume()),
                            "satellite" + satId));

                    DispatcherActor2.dispatcher = context.spawn(
                            Behaviors.supervise(DispatcherActor2.create())
                                    .onFailure(Exception.class, SupervisorStrategy.resume()),
                            "dispatcher2");
                    SatelliteQueryActor2.dbWriter = context.spawn(
                            Behaviors.supervise(DatabaseWriteActor.create())
                                    .onFailure(Exception.class, SupervisorStrategy.resume()),
                            "dbWriter");

                    ActorRef<MonitoringMessage.Monitoring> monitoringStation1 = context.spawn(
                            Behaviors.supervise(SatelliteMonitoringActor.create())
                                    .onFailure(Exception.class, SupervisorStrategy.resume()),
                            "satelliteMonitoringStation1");
                    ActorRef<MonitoringMessage.Monitoring> monitoringStation2 = context.spawn(
                            Behaviors.supervise(SatelliteMonitoringActor.create())
                                    .onFailure(Exception.class, SupervisorStrategy.resume()),
                            "satelliteMonitoringStation2");
                    ActorRef<MonitoringMessage.Monitoring> monitoringStation3 = context.spawn(
                            Behaviors.supervise(SatelliteMonitoringActor.create())
                                    .onFailure(Exception.class, SupervisorStrategy.resume()),
                            "satelliteMonitoringStation3");

                    monitoringStation1.tell(new MonitoringMessage.InitiateSatelliteQuery());
                    monitoringStation1.tell(new MonitoringMessage.InitiateSatelliteQuery());
                    monitoringStation2.tell(new MonitoringMessage.InitiateSatelliteQuery());
                    monitoringStation2.tell(new MonitoringMessage.InitiateSatelliteQuery());
                    monitoringStation3.tell(new MonitoringMessage.InitiateSatelliteQuery());
                    monitoringStation3.tell(new MonitoringMessage.InitiateSatelliteQuery());

                    Thread.sleep(1000);

                    IntStream.range(100, 200)
                            .forEach(satId -> monitoringStation1
                                    .tell(MonitoringMessage.InitiateDatabaseReadQuery.builder()
                                            .satId(satId)
                                            .build()));

                    Thread.sleep(1000);
                    DBManager.closeDB();

                    return Behaviors.receive(Void.class)
                            .onSignal(Terminated.class, sig -> Behaviors.stopped())
                            .build();
                }
        );
    }

    public static void main(String[] args){
        File configFile = new File("dispatcher.conf");
        Config config = ConfigFactory.parseFile(configFile);

        ActorSystem.create(Main2.create(), "Dispatcher2", config);
    }
}
