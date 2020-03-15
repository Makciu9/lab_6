import java.util.List;

public class StoreServer {
    private List<String> storeServer;

    StoreServer(List<String> storeServer){
        this.storeServer=storeServer;
    }


    public List<String> getServerList() {
        return storeServer;
    }

}
