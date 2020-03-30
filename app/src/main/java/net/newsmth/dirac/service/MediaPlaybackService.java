package net.newsmth.dirac.service;

import android.os.Bundle;
import android.os.Process;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.media.MediaBrowserServiceCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by cameoh on 25/11/2017.
 */

public class MediaPlaybackService extends MediaBrowserServiceCompat {

    private static final String MEDIA_ROOT_ID = "media_root_id";
    private static final String EMPTY_MEDIA_ROOT_ID = "empty_root_id";

    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;

    @Override
    public void onCreate() {
        super.onCreate();

        // Create a MediaSessionCompat
        mMediaSession = new MediaSessionCompat(this, "MusicService");

        // Enable callbacks from MediaButtons and TransportControls
        mMediaSession.setFlags(
                MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS |
                        MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);

        // Set an initial PlaybackState with ACTION_PLAY, so media buttons can start the player
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE);
        mMediaSession.setPlaybackState(mStateBuilder.build());

        // MySessionCallback() has methods that handle callbacks from a media controller
        //mMediaSession.setCallback(new MySessionCallback());

        // Set the session's token so that client activities can communicate with it.
        setSessionToken(mMediaSession.getSessionToken());
    }

    @Nullable
    @Override
    public BrowserRoot onGetRoot(@NonNull String clientPackageName, int clientUid, @Nullable Bundle rootHints) {
        // (Optional) Control the level of access for the specified package name.
        // You'll need to write your own logic to do this.
        if (Process.SYSTEM_UID == clientUid || Process.myUid() == clientUid) {
            // Returns a root ID that clients can use with onLoadChildren() to retrieve
            // the content hierarchy.
            return new BrowserRoot(MEDIA_ROOT_ID, null);
        } else {
            // Clients can connect, but this BrowserRoot is an empty hierachy
            // so onLoadChildren returns nothing. This disables the ability to browse for content.
            return new BrowserRoot(EMPTY_MEDIA_ROOT_ID, null);
        }
    }

    @Override
    public void onLoadChildren(@NonNull String parentId, @NonNull Result<List<MediaBrowserCompat.MediaItem>> result) {
//  Browsing not allowed
        if (TextUtils.equals(EMPTY_MEDIA_ROOT_ID, parentId)) {
            result.sendResult(null);
            return;
        }

        // Assume for example that the music catalog is already loaded/cached.

        List<MediaBrowserCompat.MediaItem> mediaItems = new ArrayList<>();

        // Check if this is the root menu:
        if (MEDIA_ROOT_ID.equals(parentId)) {
            // Build the MediaItem objects for the top level,
            // and put them in the mediaItems list...
        } else {
            // Examine the passed parentMediaId to see which submenu we're at,
            // and put the children of that menu in the mediaItems list...
        }
        result.sendResult(mediaItems);
    }
}
