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
import akka.stream.ActorMaterializer;
import akka.stream.javadsl.Flow;
import org.apache.zookeeper.*;
import java.io.IOException;
import java.util.concurrent.CompletionStage;
import static zookep.Const.*;

public class HttpServer {

    public static void main(String[] args) throws KeeperException, InterruptedException, IOException {
        final String port = args[0];

        ActorSystem system = ActorSystem.create("routs");
        ActorRef storeActor = system.actorOf(Props.create(StorageActor.class));

        final ActorMaterializer materialize = ActorMaterializer.create(system);

        ZooWatcher zooWat = new ZooWatcher();
        ZooKeeper zoo = new ZooKeeper(zoo_host, time_out, zooWat);

        final Http http = Http.get(system);

        ZooInit app = new ZooInit(zoo, storeActor, http);
        app.createZoo(localhost, port);
        final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow = app.createRoute().flow(system, materialize);


        final CompletionStage<ServerBinding> binding = http.bindAndHandle(
                routeFlow,
                ConnectHttp.toHost(localhost, Integer.parseInt(port)),
                materialize
        );
        System.out.println("start" + port);
        System.in.read();
        binding
                .thenCompose(ServerBinding::unbind)
                .thenAccept(unbound -> system.terminate());
    }
}












