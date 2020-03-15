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
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorSystem;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.Directives.completeWithFuture;


public class HttpServer extends AllDirectives {
    private static ActorRef storeActor;
    private static Http http;
    private static final String LOCALHOST = "localhost";
    private static int port;
    public static void main(String[] args) throws KeeperException, InterruptedException, IOException {

        Scanner in = new Scanner(System.in);
        port = in.nextInt();

        ActorSystem system = ActorSystem.create("routs");
        storeActor = system.actorOf(Props.create(StoreServer.class));



        http = Http.get(system);

        final ActorMaterializer materializer = ActorMaterializer.create(system);
        HttpServer app = new HttpServer();


        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materializer);

        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(LOCALHOST, port),
                materializer
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






    public class ZooInit implements Watcher {

        private final ZooKeeper zoo;
        private final ActorRef store;
        private final Http http;

        ZooInit(ZooKeeper zoo, ActorRef store, Http http) throws InterruptedException, KeeperException {
            this.zoo = zoo;
            this.store = store;
            this.http = http;
        }

       // public static void createZoo() throws KeeperException, InterruptedException {
        //    ZooKeeper zoo = new ZooKeeper("1", 3000, this);
      //      zoo.create("/servers/" + LOCALHOST + ":" + port, LOCALHOST.getBytes(),
    //                ZooDefs.Ids.OPEN_ACL_UNSAFE,
  //                  CreateMode.EPHEMERAL_SEQUENTIAL);
//
      //  }
        public void createZoo() throws KeeperException, InterruptedException {
            String path = zoo.create("/servers" + LOCALHOST +":" + Integer.toString(port).getBytes(),
                    Integer.toString(port).getBytes(),
                    ZooDefs.Ids.OPEN_ACL_UNSAFE,
                    CreateMode.EPHEMERAL_SEQUENTIAL);
        }


        @Override
        public void process(WatchedEvent event) {
           // List<String> servers = zoo.getChildren("/servers", a -> {
              //  List<String> servers = new ArrayList<>();
                try {
                    System.out.println("Get -> actor");
                    List<String> servers = zoo.getChildren("/servers", this);
                    System.out.println(servers);
                    store.tell(new StoreServer(servers), ActorRef.noSender());

                } catch (KeeperException | InterruptedException e) {
                    e.printStackTrace();
                }
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
      private  Route createRoute() {
          return
                  route(
                          req(() ->
                                  parameter("url", (url) ->
                                          parameter("count", (count) -> SortRequst(new Request(url, count)))
                          )
                  )
                  );
      }
          private SortRequst(Request r){
          if (r.count <= 0){
              System.out.println("END");
              return completeWithFuture(fetch(r.getUrl()));
          } else {
              r.next();
              return completeWithFuture(
                      Patterns.ask(storeActor, new GetRandomServer.class, Duration.ofSeconds(10))
                              .thenApply(m -> (String) m)
                              .thenCompose(req -> fetch(req + r.url + r.count)));}
          }
}












