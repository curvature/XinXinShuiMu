package net.newsmth.dirac.favorite;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import androidx.annotation.RequiresApi;

import net.newsmth.dirac.Dirac;
import net.newsmth.dirac.R;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class FavoriteManager {

    private static final FavoriteManager instance = new FavoriteManager();
    private final List<FavoriteItem> mList;
    private boolean mNeverUsedFavorite;

    private FavoriteManager() {
        mList = new LinkedList<>();
        SharedPreferences sp = Dirac.obtain().getSharedPreferences(Dirac.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE);
        String s = sp.getString(Dirac.PREFERENCE_KEY_FAVORITE_ITEM, null);
        if (s != null) {
            try {
                JSONArray itemArray = new JSONArray(s);
                for (int i = 0; i < itemArray.length(); ++i) {
                    mList.add(FavoriteItem.fromJson(itemArray.getJSONObject(i)));
                }
            } catch (JSONException e) {

            }
        }
        mNeverUsedFavorite = sp.getBoolean(Dirac.PREFERENCE_KEY_NEVER_USED_FAVORITE, true);


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            publishShortcuts();
        }
    }

    public static FavoriteManager getInstance() {
        return instance;
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private void publishShortcuts() {
        ShortcutManager shortcutManager = Dirac.obtain().getSystemService(ShortcutManager.class);
        if (shortcutManager != null) {
            List<ShortcutInfo> res = new ArrayList<>(3);
            for (FavoriteItem favoriteItem : mList) {
                res.add(new ShortcutInfo.Builder(Dirac.obtain(), favoriteItem.board)
                        .setShortLabel(favoriteItem.boardChinese)
                        .setLongLabel(favoriteItem.boardChinese + "/" + favoriteItem.board)
                        .setIcon(Icon.createWithResource(Dirac.obtain(), R.drawable.ic_favorite_24dp))
                        .setIntent(favoriteItem.getIntent())
                        .setRank(res.size())
                        .build());
                if (res.size() == 3) {
                    break;
                }
            }
            shortcutManager.setDynamicShortcuts(res);
        }
    }

    public boolean neverUsedFavorite() {
        if (mNeverUsedFavorite) {
            mNeverUsedFavorite = false;
            Dirac.obtain().getSharedPreferences(Dirac.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                    .edit().putBoolean(Dirac.PREFERENCE_KEY_NEVER_USED_FAVORITE, false).apply();
            return mList.size() == 0;
        }
        return false;
    }

    public List<FavoriteItem> getFavoriteItems() {
        return mList;
    }

    public boolean contains(String board) {
        for (FavoriteItem item : mList) {
            if (item.board.equals(board)) {
                return true;
            }
        }
        return false;
    }

    public void add(FavoriteItem item) {
        mList.add(0, item);
        save();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            publishShortcuts();
        }
    }

    public void remove(FavoriteItem item) {
        mList.remove(item);
        save();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            publishShortcuts();
        }
    }

    private void save() {
        if (mList.size() == 0) {
            Dirac.obtain().getSharedPreferences(Dirac.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                    .edit().remove(Dirac.PREFERENCE_KEY_FAVORITE_ITEM).apply();
        } else {
            JSONArray array = new JSONArray();
            for (FavoriteItem item : mList) {
                try {
                    array.put(item.toJson());
                } catch (JSONException e) {
                }
            }
            Dirac.obtain().getSharedPreferences(Dirac.PREFERENCE_FILE_KEY, Context.MODE_PRIVATE)
                    .edit().putString(Dirac.PREFERENCE_KEY_FAVORITE_ITEM, array.toString()).apply();
        }
    }

    public FavoriteItem find(int id) {
        for (FavoriteItem item : mList) {
            if (id == item.id) {
                return item;
            }
        }
        return null;
    }

}
