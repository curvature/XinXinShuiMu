package net.newsmth.dirac.data.source.local;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import net.newsmth.dirac.data.MainPageItem;
import net.newsmth.dirac.data.source.MainPageDataSource;
import net.newsmth.dirac.data.source.local.MainPagePersistenceContract.MainPageItemEntry;

import java.util.ArrayList;
import java.util.List;


/**
 * Concrete implementation of a data source as a db.
 * <p/>
 * Note: this is a singleton and we are opening the database once and not closing it. The framework
 * cleans up the resources when the application closes so we don't need to close the db.
 */
public class MainPageLocalDataSource implements MainPageDataSource {

    private static MainPageLocalDataSource INSTANCE;

    private SQLiteDatabase mDb;

    // Prevent direct instantiation.
    private MainPageLocalDataSource(@NonNull Context context) {
        mDb = new MainPageDbHelper(context).getWritableDatabase();
    }

    public static MainPageLocalDataSource getInstance(@NonNull Context context) {
        if (INSTANCE == null) {
            INSTANCE = new MainPageLocalDataSource(context.getApplicationContext());
        }
        return INSTANCE;
    }

    @Override
    public List<MainPageItem> getMainPageItems() {
        List<MainPageItem> mainPageItems = new ArrayList<>();
        try {

            String[] projection = {
                    MainPageItemEntry.COLUMN_NAME_ENTRY_ID,
                    MainPageItemEntry.COLUMN_NAME_SUBJECT,
                    MainPageItemEntry.COLUMN_NAME_AUTHOR,
                    MainPageItemEntry.COLUMN_NAME_BOARD_ENGLISH,
                    MainPageItemEntry.COLUMN_NAME_BOARD_CHINESE
            };

            Cursor c = mDb.query(
                    MainPageItemEntry.TABLE_NAME, projection, null, null, null, null, null);

            if (c != null && c.getCount() > 0) {
                while (c.moveToNext()) {
                    MainPageItem item = new MainPageItem();
                    String itemId = c
                            .getString(c.getColumnIndexOrThrow(MainPageItemEntry.COLUMN_NAME_ENTRY_ID));
                    item.setId(itemId);
                    String subject = c
                            .getString(c.getColumnIndexOrThrow(MainPageItemEntry.COLUMN_NAME_SUBJECT));
                    item.setSubject(subject);
                    String author =
                            c.getString(c.getColumnIndexOrThrow(MainPageItemEntry.COLUMN_NAME_AUTHOR));
                    item.setAuthor(author);
                    item.setBoardEnglish(
                            c.getString(c.getColumnIndexOrThrow(MainPageItemEntry.COLUMN_NAME_BOARD_ENGLISH)));
                    item.setBoardChinese(
                            c.getString(c.getColumnIndexOrThrow(MainPageItemEntry.COLUMN_NAME_BOARD_CHINESE)));

                    mainPageItems.add(item);
                }
            }
            if (c != null) {
                c.close();
            }

        } catch (IllegalStateException e) {
            // Send to analytics, log etc
        }
        return mainPageItems;
    }

    @Override
    public void saveMainPageItems(List<MainPageItem> mainPageItems) {
        mDb.delete(MainPageItemEntry.TABLE_NAME, null, null);
        for (MainPageItem item : mainPageItems) {
            try {
                ContentValues values = new ContentValues();
                values.put(MainPageItemEntry.COLUMN_NAME_ENTRY_ID, item.getId());
                values.put(MainPageItemEntry.COLUMN_NAME_SUBJECT, item.getSubject());
                values.put(MainPageItemEntry.COLUMN_NAME_AUTHOR, item.getAuthor());
                values.put(MainPageItemEntry.COLUMN_NAME_BOARD_ENGLISH, item.getBoardEnglish());
                values.put(MainPageItemEntry.COLUMN_NAME_BOARD_CHINESE, item.getBoardChinese());

                mDb.insert(MainPageItemEntry.TABLE_NAME, null, values);
            } catch (IllegalStateException e) {
                // Send to analytics, log etc
            }
        }
    }

}
