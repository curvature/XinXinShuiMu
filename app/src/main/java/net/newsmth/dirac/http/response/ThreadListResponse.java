package net.newsmth.dirac.http.response;

import net.newsmth.dirac.model.ThreadSummary;

import java.util.List;

public class ThreadListResponse {

    private int error;

    private List<ThreadSummary> posts;

    public int getError() {
        return error;
    }

    public void setError(int error) {
        this.error = error;
    }

    public List<ThreadSummary> getPosts() {
        return posts;
    }

    public void setPosts(List<ThreadSummary> posts) {
        this.posts = posts;
    }
}
