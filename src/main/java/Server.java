import akka.http.javadsl.server.Route;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

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
            .thenAccept(unbound -> system.terminate());

    ZooKeeper zoo = new ZooKeeper("1MB27.0.0.1MB:21MB81MB", 3000, this);
zoo.create("/servers/s",
        “data”.getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE ,
    CreateMode.EPHEMERAL_SEQUENTIAL
);
    List<String> servers = zoo.getChildren("/servers", this);
for (String s : servers) {
        byte[] data = zoo.getData("/servers/" + s, false, null);
        System.out.println("server " + s + " data=" + new String(data));

    ZooKeeper(String connectString,
               int sessionTimeout,
               Watcher watcher
    )
    public Route createRoute(){
        return
        }

              route(
                      req(() ->
                                get( () -> {
                                    Future<Object> result = Patterns.ask(testPackageActor,
                                            SemaphoreActor.makeRequest(), 5000);
                                    return completeOKWithFuture(result, Jackson.marshaller());
                                }))),
                path("test", () ->
                        route(
                                post(() ->
                                        entity(Jackson.unmarshaller(TestPackageMsg.class), msg -> {
                                            testPackageActor.tell(msg, ActorRef.noSender());
                                            return complete("Test started!");
                                        })))),
                path("put", () ->
                        get(() ->
                                parameter("key", (key) ->
                                        parameter("value", (value) ->
                                        {
                                            storeActor.tell(new StoreActor.StoreMessage(key, value), ActorRef.noSender());
                                            return complete("value saved to store ! key=" + key + " value=" + value);

        }
}
