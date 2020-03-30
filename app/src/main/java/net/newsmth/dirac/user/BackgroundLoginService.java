package net.newsmth.dirac.user;

import android.app.job.JobParameters;
import android.app.job.JobService;

public class BackgroundLoginService extends JobService {

    @Override
    public boolean onStartJob(JobParameters params) {
        UserManager.getInstance().login(true, this, params);
        return true;
    }

    @Override
    public boolean onStopJob(JobParameters params) {
        return false;
    }
}
