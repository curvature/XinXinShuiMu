package net.newsmth.dirac.http;

import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;

import net.newsmth.dirac.Dirac;

import java.io.File;

import okhttp3.Cache;
import okhttp3.OkHttpClient;

public class HttpHelper {

    private static OkHttpClient instance;

    static {
        instance = new OkHttpClient.Builder()
                .followRedirects(true)
                .cache(new Cache(new File(Dirac.obtain().getCacheDir(), "response_cache"), 100000))
                .cookieJar(new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(Dirac.obtain())))
                .build();
    }

    public static OkHttpClient getInstance() {
        return instance;
    }
}
