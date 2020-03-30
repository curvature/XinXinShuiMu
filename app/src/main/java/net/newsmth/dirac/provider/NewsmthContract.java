package net.newsmth.dirac.provider;

import android.net.Uri;
import android.provider.BaseColumns;

public final class NewsmthContract {

    public static final String CONTENT_TYPE_BASE = "vnd.android.cursor.dir/vnd.net.newsmth.dirac.provider.";

    public static final String CONTENT_ITEM_TYPE_BASE = "vnd.android.cursor.item/vnd.net.newsmth.dirac.provider.";

    public static final String CONTENT_AUTHORITY = "net.newsmth.dirac.provider";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_BOARDS = "b";

    private NewsmthContract() {
    }

    interface BoardsColumns {
        String BOARD_NAME_CHINESE = "a";
        String BOARD_NAME_ENGLISH = "b";
        String BOARD_MANAGER = "c";
        String BOARD_HIGHLIGHT = "d";
    }

    public static class Boards implements BaseColumns, BoardsColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_BOARDS).build();

        public static final String CONTENT_TYPE_ID = "b";

        public static final String[] DEFAULT_PROJECTION = new String[]{
                BOARD_NAME_ENGLISH,
                BOARD_NAME_CHINESE,
                BOARD_MANAGER
        };
    }
}
