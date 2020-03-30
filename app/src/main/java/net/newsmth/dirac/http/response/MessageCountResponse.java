package net.newsmth.dirac.http.response;

public class MessageCountResponse {

    private int error;

    //@JsonProperty("total_count")
    private int count;

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
