import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.http.javadsl.Http;
import akka.http.javadsl.model.HttpRequest;
import akka.http.javadsl.server.Route;
import akka.stream.ActorMaterializer;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.*;
import akka.http.javadsl.server.AllDirectives;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import akka.actor.ActorSystem;
import static akka.http.javadsl.server.Directives.parameter;



public class HttpServer {
    public static void main(String[] args) throws KeeperException, InterruptedException{

    Scanner in = new Scanner(System.in);
    int port = in.nextInt();

    ActorSystem = ActorSystem.create("dadwqdx");
    storeActor = system.actorOf(Props.create(StoreServer.class));


    ZooKeeper zoo = new ZooKeeper("1", 3000, this);
zoo.create("/servers/s", "/servers/s".getBytes(),
    ZooDefs.Ids.OPEN_ACL_UNSAFE ,
    CreateMode.EPHEMERAL_SEQUENTIAL
);



    List<String> servers = zoo.getChildren("/servers", a-> {
        List<String> servers = new ArrayList<>();
        try{
            //получаем сервисы
        }
        catch (KeeperException| InterruptedException e) {
            e.printStackTrace();
        }
    });

     for (String s : servers) {
        byte[] data = zoo.getData("/servers/" + s, false, null);
        System.out.println("server " + s + " data=" + new String(data));
    }






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



