package net.newsmth.dirac.data.source.local;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class MainPageDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;

    public static final String DATABASE_NAME = "m";

    private static final String TEXT_TYPE = " TEXT";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + MainPagePersistenceContract.MainPageItemEntry.TABLE_NAME + " (" +
                    MainPagePersistenceContract.MainPageItemEntry._ID + TEXT_TYPE + " PRIMARY KEY," +
                    MainPagePersistenceContract.MainPageItemEntry.COLUMN_NAME_ENTRY_ID + TEXT_TYPE + COMMA_SEP +
                    MainPagePersistenceContract.MainPageItemEntry.COLUMN_NAME_SUBJECT + TEXT_TYPE + COMMA_SEP +
                    MainPagePersistenceContract.MainPageItemEntry.COLUMN_NAME_AUTHOR + TEXT_TYPE + COMMA_SEP +
                    MainPagePersistenceContract.MainPageItemEntry.COLUMN_NAME_BOARD_ENGLISH + TEXT_TYPE + COMMA_SEP +
                    MainPagePersistenceContract.MainPageItemEntry.COLUMN_NAME_BOARD_CHINESE + TEXT_TYPE +
                    " )";

    public MainPageDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Not required as at version 1
    }
}
