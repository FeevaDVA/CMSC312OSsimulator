public class Resource {
    private String message;
    private int res;

    public Resource(){
        message = "";
        res = (int) (Math.random()*51);
    }

    public int getRes() {
        return res;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setRes(int res) {
        this.res = res;
    }
}
