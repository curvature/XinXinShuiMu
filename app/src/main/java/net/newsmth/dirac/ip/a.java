package net.newsmth.dirac.ip;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;

import net.newsmth.dirac.Dirac;
import net.newsmth.dirac.service.ApiService;
import net.newsmth.dirac.util.RetrofitUtils;

import java.io.File;
import java.io.IOException;

import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class a extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        Observable.fromCallable(() -> {
            File f = new File(Dirac.obtain().getFilesDir(), "qqwry.dat");
            if (f.exists() && !f.delete()) {
                jobFinished(params, false);
                throw new Exception();
            }
            return true;
        })
                .subscribeOn(Schedulers.io())
                .subscribe(it ->
                        RetrofitUtils.create(ApiService.class)
                                .getIpData()
                                .subscribe(responseBody -> {
                                    try (Source src = Okio.source(responseBody.byteStream());
                                         BufferedSink dst = Okio.buffer(Okio.sink(new File(Dirac.obtain().getFilesDir(), "qqwry.dat")))) {
                                        dst.writeAll(src);
                                    } catch (IOException e) {
                                        jobFinished(params, false);
                                        return;
                                    }
                                    Dirac.obtain().getSharedPreferences(Dirac.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                                            .edit().putBoolean(Dirac.PREFERENCE_KEY_IP_FILE_READY, true).commit();
                                    jobFinished(params, false);
                                }, e -> jobFinished(params, false))
                );
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
