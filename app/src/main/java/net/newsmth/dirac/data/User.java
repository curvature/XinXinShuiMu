package net.newsmth.dirac.data;

import android.text.Html;
import android.text.TextUtils;

import net.newsmth.dirac.util.RetrofitUtils;

import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class User {
    public String username;
    public String password;
    public String nickname;
    public String avatarUrl;
    public String sex;
    public String identity;
    public String level;
    public String lastLoginIp;
    public String lastLoginTime;
    public String loginTotal;
    public String postTotal;
    public String points;

    public CharSequence status;

    public User() {

    }

    public static User parse(JSONObject blob) {
        User user = new User();
        user.username = blob.optString("id");
        user.nickname = blob.optString("user_name");
        user.password = blob.optString("pw");
        user.avatarUrl = blob.optString("face_url");
        if (user.avatarUrl.startsWith("//")) {
            user.avatarUrl = RetrofitUtils.getScheme() + user.avatarUrl;
        }
        user.status = blob.optString("status");
        if (!TextUtils.isEmpty(user.status)) {
            user.status = Html.fromHtml(user.status.toString());
        }
        user.level = blob.optString("life");
        user.identity = blob.optString("level");
        user.lastLoginIp = blob.optString("last_login_ip");
        long stamp = blob.optLong("last_login_time");
        user.lastLoginTime = new SimpleDateFormat("EEE MMM d HH:mm:ss", Locale.US).format(new Date(stamp * 1000));
        user.loginTotal = blob.optString("login_count");
        user.postTotal = blob.optString("post_count");
        user.points = blob.optString("score_user");
        return user;
    }
}
