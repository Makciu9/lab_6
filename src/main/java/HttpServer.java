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

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.CompletionStage;

import akka.actor.ActorSystem;

import static akka.http.javadsl.server.Directives.*;
import static akka.http.javadsl.server.Directives.completeWithFuture;


public class HttpServer extends //AllDirectives
        {
    public static void main(String[] args) throws KeeperException, InterruptedException {
        private ActorRef storeActor;
        Scanner in = new Scanner(System.in);
        int port = in.nextInt();

        ActorSystem system = ActorSystem.create("dadwqdx");
        storeActor = system.actorOf(Props.create(StoreServer.class));

        final Http http = Http.get(system);
        HttpServer app = HttpServer();


        final ActorMaterializer materializer = ActorMaterializer.create(system);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow;

        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost("localhost", port),
                materializer
        );
        System.out.println("start");
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());

        ZooKeeper zoo = new ZooKeeper("1", 3000, this);
        zoo.create("/servers/s", "/servers/s".getBytes(),
                ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL
        );

        //
        List<String> servers = zoo.getChildren("/servers", a -> {
            List<String> servers = new ArrayList<>();
            try {
                //получаем сервисы
            } catch (KeeperException | InterruptedException e) {
                e.printStackTrace();
            }
        });

        for (String s : servers) {
            byte[] data = zoo.getData("/servers/" + s, false, null);
            System.out.println("server " + s + " data=" + new String(data));
        }


    }


          //  final Http http = Http.get(context().system());
            CompletionStage<HttpResponse> fetch(String url) {
                return http.singleRequest(HttpRequest.create(url));
            }

        private  Route createRoute(){
        return route(
                      req(() ->
                              parameter("url", (url) ->
                                    parameter("count", (count) -> {
                                        if (count == 0){
                                            System.out.print("end");
                                            completeWithFuture(url);
                                        } else completeWithFuture(
                                              
                                        )


                                    }

        )
        )
        );
}



}



