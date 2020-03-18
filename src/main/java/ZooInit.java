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

    ZooInit(ZooKeeper zoo, ActorRef store, Http http) throws InterruptedException, KeeperException {
        this.zoo = zoo;
        this.store = store;
        this.http = http;
        GetServers();
    }


    public void createZoo(String LOCALHOST, String port) throws KeeperException, InterruptedException {
        String path = zoo.create("/servers3/" + LOCALHOST + ":" + port,
                port.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        store.tell(new AddServer(LOCALHOST+ ":" + port), ActorRef.noSender());
        System.out.println(path);
        System.out.println(port);



    }

    private void GetServers() throws KeeperException, InterruptedException {
        System.out.println("Get -> actor");
        List<String> servers = zoo.getChildren("/servers3", this);
        //нет детей
        System.out.println(servers+"few");
        store.tell(new StoreServer(servers), ActorRef.noSender());
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
                    Patterns.ask(store, new GetRandomServer(), Duration.ofSeconds(10))
                            .thenApply(m -> (String) m)
                            .thenCompose(re -> fetch("http://" + re + "/?url=" + r.url + "&count=" + r.count)));
        }

    }


    @Override
    public void process(WatchedEvent event) {
        try {
            GetServers();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}