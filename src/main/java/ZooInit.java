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

    // public static void createZoo() throws KeeperException, InterruptedException {
    //    ZooKeeper zoo = new ZooKeeper("1", 3000, this);
    //      zoo.create("/servers/" + LOCALHOST + ":" + port, LOCALHOST.getBytes(),
    //                ZooDefs.Ids.OPEN_ACL_UNSAFE,
    //                  CreateMode.EPHEMERAL_SEQUENTIAL);
//
    //  }
    public void createZoo() throws KeeperException, InterruptedException {
        String path = zoo.create("/servers" + LOCALHOST + ":" + Integer.toString(port).getBytes(),
                Integer.toString(port).getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
        store.tell(new AddServer(LOCALHOST+ ":" + port), ActorRef.noSender());
        System.out.println(path);



    }

    private void GetServers() throws KeeperException, InterruptedException {
        System.out.println("Get -> actor");
        List<String> servers = zoo.getChildren("/servers", this);
        System.out.println(servers);
        store.tell(new StoreServer(servers), ActorRef.noSender());
    }






    //  final Http http = Http.get(context().system());
    CompletionStage<HttpResponse> fetch(String url) {
        return http.singleRequest(HttpRequest.create(url));
    }

    /*  private  Route createRoute() {
          return
                  route(
                  req(() ->
                          parameter("url", (url) ->
                                  parameter("count", (count) -> {
                                              if (count <= 0 ) {
                                                  System.out.print("end");
                                                 return completeWithFuture(url);
                                              } else {
                                                  count-=1;
                                                  return completeWithFuture(
                                                      Patterns.ask(storeActor, "", Duration.ofSeconds(10))
                                                              .thenApply(m -> m)
                                                              .thenCompose(m -> m + "/ | /" + r));}
                                          }
                                  )
                          )
                  )
          );
      }*/
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
        // List<String> servers = zoo.getChildren("/servers", a -> {
        //  List<String> servers = new ArrayList<>();
        try {
            GetServers();
        } catch (KeeperException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}