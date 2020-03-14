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



    ZooKeeper(String connectString,
               int sessionTimeout,
               Watcher watcher
    )




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



