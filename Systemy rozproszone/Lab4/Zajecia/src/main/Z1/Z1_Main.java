package Z1;

import akka.actor.typed.ActorSystem;

public class Z1_Main {
    public static void main(String[] args) throws Exception {
        // create actor system
        final ActorSystem<MathActor.MathCommand> system =
                ActorSystem.create(MathActor.create(), "actorMath");

        // send messages
//        system.tell(new MathActor.MathCommandAdd(5, 3));
        System.out.println("Strategy: stop");
        system.tell(new MathActor.MathCommandMultiply(5, 3, null));
        system.tell(new MathActor.MathCommandDivide(1, 3, null));
        system.tell(new MathActor.MathCommandDivide(1, 0, null));
        system.tell(new MathActor.MathCommandMultiply(5, 3, null));
        system.tell(new MathActor.MathCommandDivide(1, 3, null));

//        system.tell(new MathActor.MathCommandMultiply(5, 2, null));
//        system.tell(new MathActor.MathCommandDivide(15, 3, null));
//        system.tell(new MathActor.MathCommandDivide(15, 5, null));
//
//        system.tell(new MathActor.MathCommandDivide(15, 0, null));
//        Thread.sleep(2000);
//
//        System.out.println("Z1 main: sending second package of messages");
//        system.tell(new MathActor.MathCommandMultiply(5, 3, null));
//        system.tell(new MathActor.MathCommandMultiply(5, 2, null));
//        system.tell(new MathActor.MathCommandDivide(15, 3, null));
//        system.tell(new MathActor.MathCommandDivide(15, 5, null));
    }
}
