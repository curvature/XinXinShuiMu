package net.newsmth.dirac.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Locale;

import static android.provider.BaseColumns._ID;

public class NewsmthProvider extends ContentProvider {

    // Creates a UriMatcher object.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        sUriMatcher.addURI(NewsmthContract.CONTENT_AUTHORITY, NewsmthContract.PATH_BOARDS, 1);
    }

    private volatile BoardDatabase mOpenHelper;
    private volatile SQLiteDatabase mDb;

    @Override
    public boolean onCreate() {
        mOpenHelper = new BoardDatabase(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        if (sUriMatcher.match(uri) != 1) {
            return null;
        }

        if (mDb == null) {
            mDb = mOpenHelper.getWritableDatabase();
        }

        Cursor c = mDb.query(BoardDatabase.Tables.BOARDS,
                projection,
                selection,
                selectionArgs,
                null, null, sortOrder);

        String query = selectionArgs[0];

        // CursorAdapter requires _id column
        MatrixCursor res = new MatrixCursor(new String[]{
                _ID,
                NewsmthContract.Boards.BOARD_NAME_ENGLISH,
                NewsmthContract.Boards.BOARD_NAME_CHINESE,
                NewsmthContract.Boards.BOARD_HIGHLIGHT
        }, c.getCount());

        long id = 1L;

        if (c.moveToFirst()) {
            do {
                res.addRow(
                        new Object[]{
                                id++,
                                c.getString(0),
                                c.getString(1),
                                highlight(c.getString(1), c.getString(0), query.substring(1, query.length() - 1))
                        });
            } while (c.moveToNext());
        }
        return res;
    }

    private String highlight(String boardChinese, String boardEnglish, String query) {
        if (TextUtils.isEmpty(query)) {
            return boardChinese + "/" + boardEnglish;
        }
        return highlight(query, boardChinese) + "/" + highlight(query, boardEnglish);
    }

    private String highlight(String needle, String haystack) {
        String lowerCaseNeedle = needle.toLowerCase(Locale.US);
        String lowerCaseHaystack = haystack.toLowerCase(Locale.US);
        StringBuilder builder = new StringBuilder();
        int start = 0;
        while (true) {
            int hit = lowerCaseHaystack.indexOf(lowerCaseNeedle, start);
            if (hit == -1) {
                builder.append(haystack, start, haystack.length());
                break;
            } else {
                builder.append(haystack, start, hit).append("{");
                start = hit + needle.length();
                builder.append(haystack.substring(hit, start)).append("}");
            }
        }
        return builder.toString();
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        if (sUriMatcher.match(uri) == 1) {
            return NewsmthContract.CONTENT_TYPE_BASE + NewsmthContract.PATH_BOARDS;
        } else {
            return null;
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        return null;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        return 0;
    }

    /**
     * 更新版面数据，先清空表再批量插入
     *
     * @param uri
     * @param values
     * @return
     */
    @Override
    public int bulkInsert(@NonNull Uri uri, @NonNull ContentValues[] values) {
        if (sUriMatcher.match(uri) != 1) {
            return 0;
        }

        if (values.length < 100) {
            return 0;
        }

        if (mDb == null) {
            mDb = mOpenHelper.getWritableDatabase();
        }

        mDb.beginTransaction();

        try {
            for (ContentValues value : values) {
                mDb.replace(BoardDatabase.Tables.BOARDS, null, value);
            }
            mDb.setTransactionSuccessful();
            getContext().getContentResolver().notifyChange(uri, null);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            mDb.endTransaction();
        }

        return values.length;
    }

}
