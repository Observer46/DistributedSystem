package v1.actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.SupervisorStrategy;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import messages.DispatcherMessage;
import v2.actors.DatabaseReadActor;
import v2.actors.SatelliteQueryActor2;

public class DispatcherActor extends AbstractBehavior<DispatcherMessage.Dispatcher> {
    public static ActorRef<DispatcherMessage.Dispatcher> dispatcher;
    private final ActorRef<DispatcherMessage.DatabaseReadQuery> dbReader;

    public DispatcherActor(ActorContext<DispatcherMessage.Dispatcher> context){
        super(context);
        dbReader = context.spawn(
                Behaviors.supervise(DatabaseReadActor.create())
                        .onFailure(Exception.class, SupervisorStrategy.resume()),
                "dbReader");
    }

    public static Behavior<DispatcherMessage.Dispatcher> create(){
        return Behaviors.setup(DispatcherActor::new);
    }

    @Override
    public Receive<DispatcherMessage.Dispatcher> createReceive() {
        return newReceiveBuilder()
                .onMessage(DispatcherMessage.SatelliteQuery.class, this::onSatelliteQuery)
                .onMessage(DispatcherMessage.DatabaseReadQuery.class, this::onDatabaseReadQuery)
                .build();
    }

    private Behavior<DispatcherMessage.Dispatcher> onSatelliteQuery(DispatcherMessage.SatelliteQuery query){
        ActorRef<DispatcherMessage.SatelliteQuery> satelliteQueryWorker  = getContext()
                .spawn(Behaviors.supervise(SatelliteQueryActor.create()).onFailure(SupervisorStrategy.stop()),
                        query.getSatelliteMonitor().path().name() + "SatelliteWorker" + query.getQueryId());
        satelliteQueryWorker.tell(query);
        return this;
    }

    private Behavior<DispatcherMessage.Dispatcher> onDatabaseReadQuery(DispatcherMessage.DatabaseReadQuery query){
        dbReader.tell(query);
        return this;
    }
}
