package net.newsmth.dirac.data;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

public class TextOrImage implements Parcelable {
    public static final int TYPE_TEXT = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_AUDIO = 2; // 音频附件
    public static final Parcelable.Creator<TextOrImage> CREATOR = new Parcelable.Creator<TextOrImage>() {
        @Override
        public TextOrImage createFromParcel(Parcel in) {
            return new TextOrImage(in);
        }

        @Override
        public TextOrImage[] newArray(int size) {
            return new TextOrImage[size];
        }
    };
    public final int type;
    public CharSequence data;
    public boolean parsed;
    public String url;
    public Author artist; // 音频作者，实际是上传者

    public TextOrImage(int type, CharSequence data) {
        this.type = type;
        this.data = data;
    }

    private TextOrImage(Parcel in) {
        type = in.readInt();
        data = TextUtils.CHAR_SEQUENCE_CREATOR.createFromParcel(in);
        parsed = in.readInt() > 0;
        url = in.readString();
        artist = in.readParcelable(getClass().getClassLoader());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(type);
        TextUtils.writeToParcel(data, dest, flags);
        dest.writeInt(parsed ? 1 : 0);
        dest.writeString(url);
        dest.writeParcelable(artist, flags);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TextOrImage that = (TextOrImage) o;

        if (type != that.type) return false;
        if (data != null ? !data.equals(that.data) : that.data != null) return false;
        if (url != null ? !url.equals(that.url) : that.url != null) return false;
        return artist != null ? artist.equals(that.artist) : that.artist == null;
    }

    @Override
    public int hashCode() {
        int result = type;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        result = 31 * result + (url != null ? url.hashCode() : 0);
        result = 31 * result + (artist != null ? artist.hashCode() : 0);
        return result;
    }
}