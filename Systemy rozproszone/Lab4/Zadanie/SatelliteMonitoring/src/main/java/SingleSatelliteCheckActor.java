//import akka.actor.typed.ActorRef;
//import akka.actor.typed.Behavior;
//import akka.actor.typed.javadsl.AbstractBehavior;
//import akka.actor.typed.javadsl.ActorContext;
//import akka.actor.typed.javadsl.Receive;
//import messages.MonitoringMessage;
//
//public class SingleSatelliteCheckActor extends AbstractBehavior<MonitoringMessage.SingleSatelliteCheckQuery> {
//    private ActorRef<actors.SatelliteQueryActor> parentActor;
//
//    public SingleSatelliteCheckActor(ActorContext<MonitoringMessage.SingleSatelliteCheckQuery> context,
//                                     ActorRef<actors.SatelliteQueryActor> parentActor){
//        super(context);
//        this.parentActor = parentActor;
//    }
//
//
//    @Override
//    public Receive<MonitoringMessage.SingleSatelliteCheckQuery> createReceive() {
//        return null;
//    }
//
//    private Behavior<MonitoringMessage.SingleSatelliteCheckQuery> onSingleSatelliteCheckQuery(MonitoringMessage.SingleSatelliteCheckQuery query){
//        satellite.SatelliteAPI.Status status = satellite.SatelliteAPI.getStatus(query.sat_id);
//        //parentActor.tell(); // Single satellite response
//        return null;
//    }
//}
