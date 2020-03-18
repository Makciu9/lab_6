import akka.actor.ActorRef;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import org.apache.zookeeper.*;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.*;

public  class ZooInit implements Watcher {

    private final ZooKeeper zoo;
    private ActorRef store;
    private final Http http;

    ZooInit(ZooKeeper zoo, ActorRef store, Http http) throws KeeperException, InterruptedException {
        this.zoo = zoo;
        this.store = store;
        this.http = http;
        GetServers();
    }

    private void GetServers() throws KeeperException, InterruptedException {
        System.out.println("Get -> actor");
        List<String> servers = zoo.getChildren("/servers5", this);
        System.out.println(servers);
        store.tell(new StoreServer(servers), ActorRef.noSender());
    }

    public void createZoo(String LOCALHOST, String port) throws KeeperException, InterruptedException {
        String path = zoo.create("/servers5/" + LOCALHOST + ":" + port,
                port.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL);
        store.tell(new AddServer(LOCALHOST+ ":" + port), ActorRef.noSender());
    }




    CompletionStage<HttpResponse> fetch(String url) {
        return http.singleRequest(HttpRequest.create(url));
    }


    public Route createRoute() {
        return
                route(
                        pathSingleSlash(() ->
                                parameter("url", (url) ->
                                        parameter("count", (count) -> SortRequest(new Request(url, count)))
                                )
                        )
                );
    }

    private Route SortRequest(Request r) {
        if (r.count <= 0) {
            System.out.println("END");
            return completeWithFuture(fetch(r.url));
        } else {
            r.next();
            return completeWithFuture(
                    Patterns.ask(store, new GetRandomServer(), Duration.ofSeconds(5))
                            .thenApply(m -> (String) m)
                            .thenCompose(re -> fetch(SerReq(re, r))"http://" + re + "/?url=" + r.url + "&count=" + r.count)));

        }

    }
    private String SerReq()


    @Override
    public void process(WatchedEvent event) {
        try {
            GetServers();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}