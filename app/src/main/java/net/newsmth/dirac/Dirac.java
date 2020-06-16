package net.newsmth.dirac;

import android.app.Application;
import android.content.Context;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.preference.PreferenceManager;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.flurry.android.FlurryAgent;

import net.newsmth.dirac.http.HttpHelper;
import net.newsmth.dirac.user.UserManager;

public class Dirac extends Application {

    public static final String PREFERENCE_FILE_KEY = "net.newsmth.dirac.APP_WIDE_PREFERENCE";
    public static final String PREFERENCE_KEY_USER = "a";
    public static final String PREFERENCE_KEY_IP_FILE_READY = "b";
    public static final String PREFERENCE_KEY_FAVORITE_ITEM = "c";
    public static final String PREFERENCE_KEY_NEVER_USED_FAVORITE = "d";
    public static final int JOB_SERVICE_ID_LOGIN = 0;
    public static final int JOB_SERVICE_ID_IP = 1;
    private static Context sInstance;

    public static Context obtain() {
        return sInstance;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        new FlurryAgent.Builder()
                .withLogEnabled(BuildConfig.DEBUG)
                .build(this, "DHFCXKRMMMHMDVDW7VHY");
        sInstance = this;

        String nightMode = PreferenceManager.getDefaultSharedPreferences(this)
                .getString("pref_night_mode", null);

        if (nightMode == null) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        } else {
            switch (nightMode) {
                case "day":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
                case "night":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                    break;
                case "auto":
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_AUTO_TIME);
                    break;
                default:
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                    break;
            }
        }

        ImagePipelineConfig config = OkHttpImagePipelineConfigFactory
                .newBuilder(this, HttpHelper.getInstance())
                //.setDownsampleEnabled(true)
                //.experiment().setNativeCodeDisabled(true) // fresco has bug, without this line will crash, so loading issue
                .build();
        Fresco.initialize(this, config);

        UserManager.getInstance().start(true);
    }
}
