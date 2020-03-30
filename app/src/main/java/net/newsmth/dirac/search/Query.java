package net.newsmth.dirac.search;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by cameoh on 10/12/2017.
 */

public class Query implements Parcelable {

    public static final Creator<Query> CREATOR = new Creator<Query>() {
        @Override
        public Query createFromParcel(Parcel in) {
            return new Query(in);
        }

        @Override
        public Query[] newArray(int size) {
            return new Query[size];
        }
    };
    public String board;
    public String title;
    public String author;
    public boolean gilded;
    public boolean att;

    public Query(String board, String title, String author, boolean gilded, boolean att) {
        this.board = board;
        this.title = title;
        this.author = author;
        this.gilded = gilded;
        this.att = att;
    }

    protected Query(Parcel in) {
        board = in.readString();
        title = in.readString();
        author = in.readString();
        gilded = in.readByte() != 0;
        att = in.readByte() != 0;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(board);
        parcel.writeString(title);
        parcel.writeString(author);
        parcel.writeByte((byte) (gilded ? 1 : 0));
        parcel.writeByte((byte) (att ? 1 : 0));
    }
}
