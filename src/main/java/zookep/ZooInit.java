package zookep;
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
import static zookep.Const.*;

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
        List<String> servers = zoo.getChildren(path, this);
        System.out.println(servers);
        store.tell(new StoreServer(servers), ActorRef.noSender());
    }

    public void createZoo(String localhost, String port) throws KeeperException, InterruptedException {
        String path = zoo.create(pathC + localhost + ":" + port,
                port.getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL);
        store.tell(new AddServer(localhost+ ":" + port), ActorRef.noSender());
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
            return completeWithFuture(fetch(r.url));
            //запрос по url
        } else {
            r.next();
            return completeWithFuture(
                    Patterns.ask(store, new GetRandomServer(), Duration.ofSeconds(5))
                            //получаем новый url
                            .thenApply(m -> (String) m)
                            .thenCompose(re -> fetch(SerReq(re, r))));
            //запрос с счетчикам на один меньше

        }

    }
    private String SerReq(String url, Request r)
    {
        System.out.println("fetch ==>" + "http://" + url + "/?url=" + r.url + "&count=" + r.count);
        return "http://" + url + "/?url=" + r.url + "&count=" + r.count;
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