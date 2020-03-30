package net.newsmth.dirac.data.source.local;

import android.provider.BaseColumns;

/**
 * The contract used for the db to save the main page items locally.
 */
public final class MainPagePersistenceContract {

    public MainPagePersistenceContract() {
    }

    public static abstract class MainPageItemEntry implements BaseColumns {
        public static final String TABLE_NAME = "m";
        public static final String COLUMN_NAME_ENTRY_ID = "a";
        public static final String COLUMN_NAME_SUBJECT = "b";
        public static final String COLUMN_NAME_AUTHOR = "c";
        public static final String COLUMN_NAME_BOARD_ENGLISH = "d";
        public static final String COLUMN_NAME_BOARD_CHINESE = "e";
    }
}
