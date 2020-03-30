package net.newsmth.dirac.http.response;

public class TypedResponse<T> {

    public static final int FROM_MEMORY = 0;
    public static final int FROM_LOCAL_DATABSE = 1;
    public static final int FROM_SERVER = 2;

    public int error;
    public int from;

    public T data;

    public TypedResponse() {
    }

    public TypedResponse(T t) {
        this.data = t;
    }

}
