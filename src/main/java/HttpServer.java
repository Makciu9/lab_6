import akka.NotUsed;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.ConnectHttp;
import akka.http.javadsl.Http;
import akka.http.javadsl.ServerBinding;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.model.HttpResponse;
import akka.http.javadsl.server.Route;
import akka.pattern.Patterns;
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.*;
import akka.http.javadsl.server.AllDirectives;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;

import static akka.http.javadsl.server.Directives.completeWithFuture;


public class HttpServer extends AllDirectives {
    //private  ActorRef storeActor;
    //private  Http http;
    //private  final String LOCALHOST = "localhost";
    //private  int port;

    public static void main(String[] args) throws KeeperException, InterruptedException, IOException {
        final String port = args[0];
        //Scanner in = new Scanner(System.in);
        ///final port = in.nextInt();

        ActorSystem system = ActorSystem.create("routs");
        ActorRef storeActor = system.actorOf(Props.create(StorageActor.class));

        ZooWatcher zooWat = new ZooWatcher();
        final Http http = Http.get(system);

        final ActorMaterializer materialize = ActorMaterializer.create(system);

        ZooKeeper zoo = new ZooKeeper("127.0.0.1:2181", 3000, zooWat);

        ZooInit app = new ZooInit(zoo, storeActor, http);
        app.createZoo();

        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materialize);


        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(LOCALHOST, port),
                materialize
        );
        System.out.println("start" + port);
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());

       /* ZooKeeper zoo = new ZooKeeper("1", 3000, this);
        zoo.create("/servers/s", "/servers/s".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL
        );
    }


        List<String> servers = zoo.getChildren("/servers", a -> {
            List<String> servers = new ArrayList<>();
            try {

            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        for (String s : servers) {
            byte[] data = zoo.getData("/servers/" + s, false, null);
            System.out.println("server " + s + " data=" + new String(data));
        }*/

    }


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
}












