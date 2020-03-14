import akka.actor.AbstractActor;
import akka.japi.pf.ReceiveBuilder;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class StoreActor extends AbstractActor {
        private Map<String, String> store = new HashMap<>();
    private List<String> listServers;

    @Override
        public Receive createReceive() {
            return ReceiveBuilder.create()
                    .match(StoreServer.class, m -> this.listServers=m.getServerList())
                    .match(){
                        store.put(m.getKey(), m.getValue());
                        System.out.println("receive message! " + m.toString());
                    })
                    .match(GetRandomServer.class, req -> {
                          String ranS = getRanS();
                          sender().tell(ranS, store.get(req.getKey())), self());

    }).build();

}

    private String getRanS() {
        return listServers.get(Math.random((listServers.size())));
    }

}




