package actors;

import akka.actor.typed.ActorRef;
import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import messages.DispatcherMessage;

public class DispatcherActor extends AbstractBehavior<DispatcherMessage.Dispatcher> {
    private final ActorRef<DispatcherMessage.DatabaseReadQuery> dbReader;

    public DispatcherActor(ActorContext<DispatcherMessage.Dispatcher> context){
        super(context);
        dbReader = getContext().spawn(DatabaseReadActor.create(), "dbReader");
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
                .spawn(SatelliteQueryActor.create(),
                        query.getSatelliteMonitor().path().name() + "SatelliteWorker" + query.getQueryId());
        satelliteQueryWorker.tell(query);
        return this;
    }

    private Behavior<DispatcherMessage.Dispatcher> onDatabaseReadQuery(DispatcherMessage.DatabaseReadQuery query){
        dbReader.tell(query);
        return this;
    }
}
