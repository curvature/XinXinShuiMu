package net.newsmth.dirac.data;

import android.os.Parcel;
import android.os.Parcelable;

public class Author implements Parcelable {

    public static final int SEX_UNKNOWN = 0;
    public static final int SEX_MALE = 1;
    public static final int SEX_FEMALE = 2;
    public static final Parcelable.Creator<Author> CREATOR = new Parcelable.Creator<Author>() {
        @Override
        public Author createFromParcel(Parcel in) {
            return new Author(in);
        }

        @Override
        public Author[] newArray(int size) {
            return new Author[size];
        }
    };
    public int sex;
    public String username;
    public String avatarUrl;
    public String nickname;

    public Author() {

    }

    private Author(Parcel in) {
        sex = in.readInt();
        username = in.readString();
        avatarUrl = in.readString();
        nickname = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(sex);
        dest.writeString(username);
        dest.writeString(avatarUrl);
        dest.writeString(nickname);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Author author = (Author) o;

        if (sex != author.sex) return false;
        if (username != null ? !username.equals(author.username) : author.username != null)
            return false;
        if (avatarUrl != null ? !avatarUrl.equals(author.avatarUrl) : author.avatarUrl != null)
            return false;
        return nickname != null ? nickname.equals(author.nickname) : author.nickname == null;
    }

    @Override
    public int hashCode() {
        int result = sex;
        result = 31 * result + (username != null ? username.hashCode() : 0);
        result = 31 * result + (avatarUrl != null ? avatarUrl.hashCode() : 0);
        result = 31 * result + (nickname != null ? nickname.hashCode() : 0);
        return result;
    }
}