package v1.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import messages.DatabaseWriteResults;
import utils.DBManager;

public class DatabaseWriteActor extends AbstractBehavior<DatabaseWriteResults> {

    public DatabaseWriteActor(ActorContext<DatabaseWriteResults> context) {
        super(context);
    }

    public static Behavior<DatabaseWriteResults> create(){
        return Behaviors.setup(DatabaseWriteActor::new);
    }

    @Override
    public Receive<DatabaseWriteResults> createReceive() {
        return newReceiveBuilder()
                .onMessage(DatabaseWriteResults.class, this::onDatabaseWriteResults)
                .build();
    }

    private Behavior<DatabaseWriteResults> onDatabaseWriteResults(DatabaseWriteResults results){
        results.getErrorSatellites()
                .forEach((satId, status) -> DBManager.updateSatellite(satId, 1));
        return this;
    }
}
