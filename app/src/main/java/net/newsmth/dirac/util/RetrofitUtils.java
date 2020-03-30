package net.newsmth.dirac.util;

import androidx.preference.PreferenceManager;

import net.newsmth.dirac.Dirac;
import net.newsmth.dirac.http.HttpHelper;

import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class RetrofitUtils {

    private static volatile boolean useHttps;

    static {
        useHttps = PreferenceManager.getDefaultSharedPreferences(Dirac.obtain())
                .getBoolean("pref_use_https", true);
    }

    public static void setUseHttps(boolean use) {
        useHttps = use;
    }

    public static String getScheme() {
        return useHttps ? "https:" : "http:";
    }

    public static <T> T create(final Class<T> service) {
        if (useHttps) {
            return create("https://www.newsmth.net/", service);
        }
        return create("http://www.newsmth.net/", service);
    }

    public static <T> T create(final String url, final Class<T> service) {
        return new Retrofit.Builder()
                .client(HttpHelper.getInstance())
                .baseUrl(url)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build().create(service);
    }
}
