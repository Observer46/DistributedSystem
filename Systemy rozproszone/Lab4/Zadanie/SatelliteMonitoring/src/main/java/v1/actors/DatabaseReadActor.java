package v1.actors;

import akka.actor.typed.Behavior;
import akka.actor.typed.javadsl.AbstractBehavior;
import akka.actor.typed.javadsl.ActorContext;
import akka.actor.typed.javadsl.Behaviors;
import akka.actor.typed.javadsl.Receive;
import messages.DispatcherMessage;
import messages.MonitoringMessage;
import utils.DBManager;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DatabaseReadActor extends AbstractBehavior<DispatcherMessage.DatabaseReadQuery> {

    public DatabaseReadActor(ActorContext<DispatcherMessage.DatabaseReadQuery> context) {
        super(context);
    }

    public static Behavior<DispatcherMessage.DatabaseReadQuery> create() {
        return Behaviors.setup(DatabaseReadActor::new);
    }

    @Override
    public Receive<DispatcherMessage.DatabaseReadQuery> createReceive() {
        return newReceiveBuilder()
                .onMessage(DispatcherMessage.DatabaseReadQuery.class, this::onDatabaseReadQuery)
                .build();
    }

    private Behavior<DispatcherMessage.DatabaseReadQuery> onDatabaseReadQuery(DispatcherMessage.DatabaseReadQuery query){
        ResultSet result = DBManager.readSatellite(query.getSatId());
        try {
            query.getSatelliteMonitor()
                    .tell(MonitoringMessage.DatabaseReadResponse.builder()
                            .satId(result.getInt("id"))
                            .errorCount(result.getInt("errors"))
                            .build());
            result.close();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
        return this;
    }
}
