import akka.actor.AbstractActor;
import akka.actor.ActorRef;
import akka.japi.pf.ReceiveBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StorageActor extends AbstractActor {
    private List<String> listServers;
    private Random random = new Random();

    @Override
        public Receive createReceive() {
            return ReceiveBuilder.create()
                    .match(StoreServer.class, m -> this.listServers=m.getServerList())
                    .match(AddServer.class, m ->{
                        listServers.add(m.getUrl());
                        System.out.println("receive message! " + m.toString());
                    })
                    .match(GetRandomServer.class, req -> {
                          String ranS = getRanS();
                          System.out.println("receive message! " + ranS);
                          sender().tell(ranS,  ActorRef.noSender());

    }).build();

}

    private String getRanS() {
        return listServers.get(random.nextInt(listServers.size()));
    }

}




