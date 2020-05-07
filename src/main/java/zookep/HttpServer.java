package zookep;
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


public class HttpServer {

    public static void main(String[] args) throws KeeperException, InterruptedException, IOException {
        final String port = args[0];
        final String LOCALHOST = "localhost";

        ActorSystem system = ActorSystem.create("routs");
        ActorRef storeActor = system.actorOf(Props.create(StorageActor.class));

        final ActorMaterializer materialize = ActorMaterializer.create(system);

        ZooWatcher zooWat = new ZooWatcher();
        ZooKeeper zoo = new ZooKeeper("127.0.0.1:2181", 3000, zooWat);

        final Http http = Http.get(system);

        ZooInit app = new ZooInit(zoo, storeActor, http);
        app.createZoo(LOCALHOST, port);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materialize);


        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(LOCALHOST, Integer.parseInt(port)),
                materialize
        );
        System.out.println("start" + port);
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }
}












