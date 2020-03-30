package net.newsmth.dirac.user;

import android.app.job.JobParameters;
import android.app.job.JobService;
import android.os.AsyncTask;

import net.newsmth.dirac.service.ApiService;
import net.newsmth.dirac.util.RetrofitUtils;

import org.json.JSONObject;

public class LoginTask extends AsyncTask<String, Void, JSONObject> {

    private String mUsername;
    private String mPassword;
    private boolean mCheckFirst;
    private JobService mJobService;
    private JobParameters mParams;

    public LoginTask(String username, String password, boolean checkFirst, JobService jobService,
                     JobParameters params) {
        mUsername = username;
        mPassword = password;
        mCheckFirst = checkFirst;
        mJobService = jobService;
        mParams = params;
    }

    @Override
    protected JSONObject doInBackground(final String... params) {
        try {
            if (mCheckFirst) {
                if (mUsername.equals(new JSONObject(RetrofitUtils.create(ApiService.class)
                        .getStatus().blockingSingle()).optString("id"))) {
                    return null;
                }
            }
            return new JSONObject(RetrofitUtils.create(ApiService.class)
                    .login(mUsername, mPassword).blockingSingle());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        if (jsonObject != null && jsonObject.optInt("ajax_st") == 1) {
            UserManager.getInstance().save(jsonObject, mPassword);
        }
        UserManager.getInstance().loginDone();
        if (mJobService != null) {
            mJobService.jobFinished(mParams, false);
        }
    }
}
