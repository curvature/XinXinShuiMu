package net.newsmth.dirac.provider;

import android.content.Context;

import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

public class BoardDatabase extends SQLiteAssetHelper {

    public static final int DATABASE_VERSION = 4;

    private static final String DATABASE_NAME = "b";

    public BoardDatabase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        setForcedUpgrade();
    }

    interface Tables {
        String BOARDS = "b";
    }

}
