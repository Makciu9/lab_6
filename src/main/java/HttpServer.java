import akka.actor.ActorSystem;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.*;
import akka.http.javadsl.server.AllDirectives;
import static akka.http.javadsl.server.Directives.parameter;



public class HttpServer {


    final Http http = Http.get(context().system());
    CompletionStage<HttpResponse> fetch(String url) {
        return http.singleRequest(HttpRequest.create(url));
    }
   

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


    public Route createRoute(){
        return route(
                      req(() ->
                              parameter("url", (url) ->
                                    parameter("count", (count) -> {
                                        if (count == 0) System.out.print("end");
                                        return count == 0? //передать:рандом и запрос

                                    }

        )
        )
        );
}
}



