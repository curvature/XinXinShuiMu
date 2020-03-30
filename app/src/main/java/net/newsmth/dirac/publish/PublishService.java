package net.newsmth.dirac.publish;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import net.newsmth.dirac.Dirac;
import net.newsmth.dirac.R;
import net.newsmth.dirac.activity.EditPostActivity;
import net.newsmth.dirac.util.RetrofitUtils;

import org.json.JSONException;
import org.json.JSONObject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class PublishService extends Service {

    private static final int COMMAND_SEND = 1;
    private static final int COMMAND_PROGRESS = 2;
    private static final String EXTRA_COMMAND = "a";
    private static final String EXTRA_BOARD = "b";
    private static final String EXTRA_SUBJECT = "c";
    private static final String EXTRA_BODY = "d";
    private static final String EXTRA_POST_ID = "e";
    private static final String EXTRA_SUCCESS = "f";
    private static final String CHANNEL_ID = "net.newsmth.dirac.publish.PUBLISH_CHANNEL_ID";
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder builder;

    public static void sendPost(Context context, String board, String subject, String body, String id) {
        context.startService(new Intent(context, PublishService.class)
                .putExtra(EXTRA_COMMAND, COMMAND_SEND)
                .putExtra(EXTRA_BOARD, board)
                .putExtra(EXTRA_SUBJECT, subject)
                .putExtra(EXTRA_BODY, body)
                .putExtra(EXTRA_POST_ID, id));
    }

    public static void sendProgress(Context context, boolean success) {
        context.startService(new Intent(context, PublishService.class)
                .putExtra(EXTRA_COMMAND, COMMAND_PROGRESS)
                .putExtra(EXTRA_SUCCESS, success));
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // Notification channels are only supported on Android O+.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
        switch (intent.getIntExtra(EXTRA_COMMAND, 0)) {
            case COMMAND_SEND:
                if (builder == null) {
                    builder = new NotificationCompat.Builder(this, CHANNEL_ID);
                }
                builder.setSmallIcon(R.drawable.ic_publish_black_24dp)
                        .setContentTitle(getString(R.string.publishing))
                        .setContentText(intent.getStringExtra(EXTRA_SUBJECT))
                        .setOngoing(true)
                        .setProgress(0, 0, true);

                Intent notificationIntent = new Intent(this, EditPostActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);
                //builder.setContentIntent(pendingIntent);

                mNotificationManager.notify(1, builder.build());

                RetrofitUtils.create(net.newsmth.dirac.service.PublishService.class)
                        .publish(intent.getStringExtra(EXTRA_BOARD),
                                intent.getStringExtra(EXTRA_SUBJECT),
                                intent.getStringExtra(EXTRA_BODY),
                                intent.getStringExtra(EXTRA_POST_ID))
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(
                                resp -> {
                                    boolean success = false;
                                    try {
                                        JSONObject blob = new JSONObject(resp);
                                        success = blob.optInt("ajax_st") == 1;
                                    } catch (JSONException e) {

                                    }
                                    PublishService.sendProgress(Dirac.obtain(), success);
                                },
                                e -> PublishService.sendProgress(Dirac.obtain(), false)
                        );
                break;
            case COMMAND_PROGRESS:
                builder.setProgress(0, 0, false).setOngoing(false);
                if (intent.getBooleanExtra(EXTRA_SUCCESS, false)) {
                    builder.setSmallIcon(R.drawable.ic_done_black_24dp)
                            .setContentTitle(getString(R.string.published));
                } else {
                    builder.setSmallIcon(R.drawable.ic_error_black_24dp)
                            .setContentTitle(getString(R.string.publish_failed));
                }
                mNotificationManager.notify(1, builder.build());
                stopSelf();
                break;
            default:
                break;
        }
        return START_NOT_STICKY;
    }

    /**
     * Creates Notification Channel. This is required in Android O+ to display notifications.
     */
    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        if (mNotificationManager.getNotificationChannel(CHANNEL_ID) == null) {
            NotificationChannel notificationChannel =
                    new NotificationChannel(CHANNEL_ID,
                            getString(R.string.channel_publish),
                            NotificationManager.IMPORTANCE_LOW);

            //notificationChannel.setDescription();

            mNotificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
