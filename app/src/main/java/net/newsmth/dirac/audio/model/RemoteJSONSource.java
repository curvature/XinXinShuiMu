package net.newsmth.dirac.audio.model;

import android.support.v4.media.MediaMetadataCompat;
import android.text.TextUtils;

import net.newsmth.dirac.data.TextOrImage;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Utility class to get a list of MusicTrack's based on a server-side JSON
 * configuration.
 */
public class RemoteJSONSource implements MusicProviderSource {

    public static MediaMetadataCompat build(TextOrImage item) {
        String title = item.data.toString();

        String artist = item.artist.username;

        String source = item.url;
        //String iconUrl = json.getString(JSON_IMAGE);


        // Since we don't have a unique ID in the server, we fake one using the hashcode of
        // the music source. In a real world app, this could come from the server.
        String id = String.valueOf(item.hashCode());

        // Adding the music source to the MediaMetadata (and consequently using it in the
        // mediaSession.setMetadata) is not a good idea for a real world music app, because
        // the session metadata can be accessed by notification listeners. This is done in this
        // sample for convenience only.
        //noinspection ResourceType
        MediaMetadataCompat.Builder builder = new MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, id)
                .putString(MusicProviderSource.CUSTOM_METADATA_TRACK_SOURCE, source)
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, artist)
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, title);

        if (!TextUtils.isEmpty(item.artist.avatarUrl)) {
            builder.putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ART_URI, item.artist.avatarUrl);
        }
        return builder.build();
    }

    @Override
    public Iterator<MediaMetadataCompat> iterator() {
        List<MediaMetadataCompat> tracks = new ArrayList<>();
        for (TextOrImage item : MusicProvider.mList) {
            tracks.add(build(item));
        }
        return tracks.iterator();
    }
}
