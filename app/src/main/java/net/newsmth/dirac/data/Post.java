package net.newsmth.dirac.data;

import android.os.Parcel;
import android.os.Parcelable;

import net.newsmth.dirac.util.RetrofitUtils;

import java.util.LinkedList;
import java.util.List;

public class Post implements Parcelable {

    public static final Parcelable.Creator<Post> CREATOR = new Parcelable.Creator<Post>() {
        @Override
        public Post createFromParcel(Parcel in) {
            return new Post(in);
        }

        @Override
        public Post[] newArray(int size) {
            return new Post[size];
        }
    };
    public String id;
    public List<TextOrImage> contentList;
    public String[] imageUrlArray;
    public int floor;
    public Author author;
    public String title;
    public String time;
    public String source;
    public String ip;
    public String ipLocation;
    public String replyQuote;

    public int page; // 如果大于零，为待加载页，其他数据均无效
    public int totalPost;

    public int requestFlag;

    public Post() {
    }

    private Post(Parcel in) {
        id = in.readString();
        contentList = in.readArrayList(TextOrImage.class.getClassLoader());
        imageUrlArray = in.createStringArray();
        floor = in.readInt();
        author = (Author) in.readValue(Author.class.getClassLoader());
        title = in.readString();
        time = in.readString();
        source = in.readString();
        ip = in.readString();
        ipLocation = in.readString();
        replyQuote = in.readString();
        page = in.readInt();
        totalPost = in.readInt();
    }

    public String getFloorText() {
        return floor == 0 ? "楼主" : floor + "楼";
    }

    public int size() {
        if (page > 0) {
            return 2;
        }
        int size = 2;
        if (contentList != null) {
            size += contentList.size();
        }
        if (ip != null) {
            size += 1;
        }
        return size;
    }

    public String getShareUrl(String board) {
        if (floor == 0) {
            return RetrofitUtils.getScheme() + "//m.newsmth.net/article/" + board + "/" + id;
        }
        return RetrofitUtils.getScheme() + "//m.newsmth.net/article/" + board + "/single/" + id;
    }

    public void buildReplyQuote(LinkedList<String> firstFourLines) {
        StringBuilder builder = new StringBuilder();
        builder.append("\n")
                .append("【 在 ").append(author.username)
                .append(" (")
                .append(author.nickname)
                .append(") 的大作中提到: 】");
        if (firstFourLines == null) {
            replyQuote = builder.toString();
            return;
        }
        if (firstFourLines.size() == 4) {
            firstFourLines.removeLast();
            firstFourLines.addLast(": ...................");
        }
        for (String line : firstFourLines) {
            builder.append("\n").append(line);
        }
        replyQuote = builder.toString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeList(contentList);
        dest.writeStringArray(imageUrlArray);
        dest.writeInt(floor);
        dest.writeValue(author);
        dest.writeString(title);
        dest.writeString(time);
        dest.writeString(source);
        dest.writeString(ip);
        dest.writeString(ipLocation);
        dest.writeString(replyQuote);
        dest.writeInt(page);
        dest.writeInt(totalPost);
    }
}