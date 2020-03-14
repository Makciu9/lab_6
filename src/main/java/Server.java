import akka.http.javadsl.server.Route;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.*;
import akka.http.javadsl.server.AllDirectives;
import static akka.http.javadsl.server.Directives.parameter;



public class Server {

    ActorSystem system = ActorSystem.create("routes");
    final Http http = Http.get(system);
    final ActorMaterializer materializer = ActorMaterializer.create(system);
    MainHttp instance = new MainHttp(system);
    final Flow<HttpRequest, HttpResponse, NotUsed> routeFlow =
            instance.createRoute(system).flow(system, materializer);
    final CompletionStage<ServerBinding> binding = http.bindAndHandle(
            routeFlow,
            ConnectHttp.toHost("localhost", 8080),
            materializer
    );
System.out.println("Server online at http://localhost:8080/\nPress RETURN to stop...");
System.in.read();
binding
        .thenCompose(ServerBinding::unbind)
            .

    thenAccept(unbound ->system.terminate());

    ZooKeeper zoo = new ZooKeeper("1MB27.0.0.1MB:21MB81MB", 3000, this);
zoo.create("/servers/s",
        “data”.

    getBytes(),

    ZooDefs.Ids.OPEN_ACL_UNSAFE ,
    CreateMode.EPHEMERAL_SEQUENTIAL
);
    List<String> servers = zoo.getChildren("/servers", this);
for(
    String s :servers)

    {
        byte[] data = zoo.getData("/servers/" + s, false, null);
        System.out.println("server " + s + " data=" + new String(data));

        ZooKeeper(String connectString,
        int sessionTimeout,
        Watcher watcher
    )
    }

    public Route createRoute(){
        return route(
                      req(() ->
                              parameter("url", (url) ->
                                    parameter("count", (count) -> {
                                        if (count == 0) System.out.print("end");
                                        return count == 0? //
                                    }

        )
        )
        );
}
}



