package net.newsmth.dirac.favorite;

import android.content.Intent;
import android.view.View;

import net.newsmth.dirac.Dirac;
import net.newsmth.dirac.activity.BoardActivity;

import org.json.JSONException;
import org.json.JSONObject;

import static net.newsmth.dirac.activity.BoardActivity.EXTRA_BOARD;
import static net.newsmth.dirac.activity.BoardActivity.EXTRA_BOARD_CHINESE;

public class FavoriteItem {

    public final String board;
    public final String boardChinese;
    public final int id;

    public FavoriteItem(String board, String boardChinese) {
        this.board = board;
        this.boardChinese = boardChinese;
        id = View.generateViewId();
    }

    static FavoriteItem fromJson(JSONObject o) throws JSONException {
        return new FavoriteItem(o.getString("a"), o.getString("b"));
    }

    JSONObject toJson() throws JSONException {
        return new JSONObject().put("a", board).put("b", boardChinese);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FavoriteItem that = (FavoriteItem) o;

        if (board != null ? !board.equals(that.board) : that.board != null) return false;
        return boardChinese != null ? boardChinese.equals(that.boardChinese) : that.boardChinese == null;

    }

    @Override
    public int hashCode() {
        int result = board != null ? board.hashCode() : 0;
        result = 31 * result + (boardChinese != null ? boardChinese.hashCode() : 0);
        return result;
    }

    public Intent getIntent() {
        return new Intent(Dirac.obtain(), BoardActivity.class)
                .setAction(Intent.ACTION_VIEW)
                .putExtra(EXTRA_BOARD, board)
                .putExtra(EXTRA_BOARD_CHINESE, boardChinese);
    }

}
