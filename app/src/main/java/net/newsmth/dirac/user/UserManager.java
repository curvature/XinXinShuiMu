package net.newsmth.dirac.user;

import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.TextUtils;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import net.newsmth.dirac.Dirac;
import net.newsmth.dirac.data.User;
import net.newsmth.dirac.util.HashUtils;

import org.json.JSONException;
import org.json.JSONObject;

public class UserManager {

    public static final String USER_ACTION = "net.newsmth.dirac.USER_CHANGE";
    private static volatile UserManager instance;
    private Context context;
    private User user;
    private LoginTask pendingTask;
    private int jobId;

    private UserManager() {
        context = Dirac.obtain();
        SharedPreferences sp = context.getSharedPreferences(Dirac.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String userJson = HashUtils.decode(sp.getString(Dirac.PREFERENCE_KEY_USER, null));
        if (!TextUtils.isEmpty(userJson)) {
            try {
                user = User.parse(new JSONObject(userJson));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    public static UserManager getInstance() {
        if (instance == null) {
            synchronized (UserManager.class) {
                if (instance == null) {
                    instance = new UserManager();
                }
            }
        }
        return instance;
    }

    public User getUser() {
        return user;
    }

    public boolean needLogin() {
        return user == null || TextUtils.isEmpty(user.password);
    }

    public void save(JSONObject userJson, String password) {
        user = User.parse(userJson);
        user.password = password;
        try {
            userJson.put("pw", password);
        } catch (JSONException e) {
        }
        context.getSharedPreferences(Dirac.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                .edit().putString(Dirac.PREFERENCE_KEY_USER, HashUtils.encode(userJson.toString()))
                .apply();
        notifyUserChanged();
    }

    public void start(boolean immediate) {
        if (!needLogin()) {
            if (immediate) {
                login(false);
            }
            schedule();
        }
    }

    private void schedule() {
        if (jobId <= 0) {
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobId = jobScheduler.schedule(new JobInfo.Builder(Dirac.JOB_SERVICE_ID_LOGIN,
                    new ComponentName(context, BackgroundLoginService.class))
                    .setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                    .setPeriodic(900000).build());
        }
    }

    public void login(boolean checkFirst) {
        login(checkFirst, null, null);
    }

    public void login(boolean checkFirst, JobService jobService, JobParameters params) {
        if (pendingTask == null && !needLogin()) {
            pendingTask = new LoginTask(user.username, user.password, checkFirst, jobService, params);
            pendingTask.execute();
        }
    }

    public void loginDone() {
        pendingTask = null;
    }

    public void notifyUserChanged() {
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(USER_ACTION));
    }

    public void logout() {
        user = null;
        context.getSharedPreferences(Dirac.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                .edit().remove(Dirac.PREFERENCE_KEY_USER).apply();
        if (jobId > 0) {
            ((JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE)).cancel(jobId);
        }
        if (pendingTask != null) {
            pendingTask.cancel(true);
            pendingTask = null;
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent(USER_ACTION));
    }

    public String getToken() {
        return null;
    }

    public void queryStatus() {
        context.startService(new Intent(context, BackgroundLoginService.class));
    }
}