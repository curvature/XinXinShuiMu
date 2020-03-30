package net.newsmth.dirac.provider;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class BoardDbHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "export";

    private static final String TEXT_TYPE = " TEXT";

    private static final String COMMA_SEP = ",";

    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + BoardDatabase.Tables.BOARDS + " (" +
                    NewsmthContract.Boards.BOARD_NAME_ENGLISH + TEXT_TYPE + " PRIMARY KEY," +
                    NewsmthContract.Boards.BOARD_NAME_CHINESE + TEXT_TYPE + COMMA_SEP +
                    NewsmthContract.Boards.BOARD_MANAGER + TEXT_TYPE +
                    " )";

    public BoardDbHelper(Context context) {
        super(context, DATABASE_NAME, null, BoardDatabase.DATABASE_VERSION);
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
