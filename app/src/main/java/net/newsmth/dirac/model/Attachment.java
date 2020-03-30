package net.newsmth.dirac.model;

import net.newsmth.dirac.data.Post;
import net.newsmth.dirac.util.RetrofitUtils;

public class Attachment {

    private String name;
    private String size;
    private String pos;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = pos;
    }

    public String getUrl(Post post, String board) {
        return RetrofitUtils.getScheme() + "//att.newsmth.net/nForum/att/" + board + "/" + post.id + "/" + pos;
    }
}
