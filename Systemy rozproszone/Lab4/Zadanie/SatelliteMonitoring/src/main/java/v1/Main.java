package v1;

import v1.actors.DatabaseWriteActor;
import v1.actors.DispatcherActor;
import v1.actors.SatelliteMonitoringActor;
import v1.actors.SatelliteQueryActor;
import akka.actor.typed.*;
import akka.actor.typed.javadsl.Behaviors;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import messages.MonitoringMessage;
import utils.DBManager;

import java.io.File;
import java.util.stream.IntStream;

public class Main {
    public static Behavior<Void> create(){
        return Behaviors.setup(
                context -> {
                    DBManager.prepareDB();

                    DispatcherActor.dispatcher = context.spawn(
                            Behaviors.supervise(DispatcherActor.create())
                                    .onFailure(Exception.class, SupervisorStrategy.resume()),
                            "dispatcher");
                    SatelliteQueryActor.dbWriter = context.spawn(
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

        ActorSystem.create(Main.create(), "Dispatcher", config);
    }
}
