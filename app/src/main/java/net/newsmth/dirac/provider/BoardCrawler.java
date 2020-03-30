package net.newsmth.dirac.provider;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import net.newsmth.dirac.Dirac;
import net.newsmth.dirac.data.Board;
import net.newsmth.dirac.http.parser.BoardParser;
import net.newsmth.dirac.service.ApiService;
import net.newsmth.dirac.util.RetrofitUtils;

import java.io.File;
import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import okio.BufferedSink;
import okio.Okio;
import okio.Source;

public class BoardCrawler {


    int i;

    public BoardCrawler() {

    }

    private Observable<Board> expand(@NonNull Board board) {
        if (TextUtils.isEmpty(board.section)) {
            return Observable.just(board);
        }
        return RetrofitUtils.create(ApiService.class)
                .getSection(board.section).
                        flatMap(responseBody -> Observable.fromIterable(
                                new BoardParser().parseResponse(responseBody).data))
                .flatMap(this::expand);
    }

    public void get(final ProgressCallback callback, final Runnable success, final Runnable error) {
        List<Board> folder = new LinkedList<>();
        for (int i = 0; i < 10; ++i) {
            folder.add(new Board(String.valueOf(i)));
        }
        folder.add(new Board("Closed"));
        folder.add(new Board("Merged"));
        folder.add(new Board("Removed"));

        Observable.fromIterable(folder)
                .subscribeOn(Schedulers.io())
                .flatMap(this::expand)
                .distinct()
                .subscribeOn(AndroidSchedulers.mainThread())
                .doOnNext(it -> callback.onProgress(++i))
                .toSortedList()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        it -> {
                            saveAll(it);
                            success.run();
                        },
                        throwable -> error.run()
                );
    }

    private void saveAll(List<Board> boards) {
        ContentValues[] values = new ContentValues[boards.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = new ContentValues();
            Board blob = boards.get(i);
            values[i].put(NewsmthContract.Boards.BOARD_NAME_ENGLISH, blob.boardEnglish);
            values[i].put(NewsmthContract.Boards.BOARD_NAME_CHINESE, blob.boardChinese);
            values[i].put(NewsmthContract.Boards.BOARD_MANAGER, blob.boardManager);
        }

        Dirac.obtain().getContentResolver().bulkInsert(NewsmthContract.Boards.CONTENT_URI, values);
        //saveToDisk(values);
    }

    private void saveToDisk(ContentValues[] values) {
        try (SQLiteDatabase db = new BoardDbHelper(Dirac.obtain()).getWritableDatabase()) {
            for (ContentValues value : values) {
                db.insert(BoardDatabase.Tables.BOARDS, null, value);
            }
            copyFile(new File("/data/data/" + Dirac.obtain().getPackageName() + "/databases/export"),
                    new File("/sdcard/b"));
        }
    }

    private void copyFile(File fromFile, File toFile) {
        try (Source a = Okio.source(fromFile); BufferedSink b = Okio.buffer(Okio.sink(toFile))) {
            b.writeAll(a);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public interface ProgressCallback {
        void onProgress(int progress);
    }
}
