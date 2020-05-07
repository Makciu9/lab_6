public class Request {
    public String url;
    public int count;

    Request(String url, String count){
        this.url = url;
        this.count= Integer.parseInt(count);
    }

    Request(String url, int count){
        this.url = url;
        this.count = count;
    }



    public int getCount(){
        return count;
    }

    public void next() {
        count-=1;
    }

    public String getUrl(){
        return url;
    }

}
