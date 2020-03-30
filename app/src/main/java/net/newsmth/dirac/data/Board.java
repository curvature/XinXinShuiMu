package net.newsmth.dirac.data;

public class Board implements Comparable<Board> {
    public String section;
    public String boardChinese;
    public String boardEnglish;
    public String boardManager;

    public Board() {

    }

    public Board(String section) {
        this.section = section;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Board board = (Board) o;
        if (section != null ? !section.equals(board.section) : board.section != null) return false;
        if (boardChinese != null ? !boardChinese.equals(board.boardChinese) : board.boardChinese != null)
            return false;
        if (boardEnglish != null ? !boardEnglish.equals(board.boardEnglish) : board.boardEnglish != null)
            return false;
        return boardManager != null ? boardManager.equals(board.boardManager) : board.boardManager == null;
    }

    @Override
    public int hashCode() {
        int result = section != null ? section.hashCode() : 0;
        result = 31 * result + (boardChinese != null ? boardChinese.hashCode() : 0);
        result = 31 * result + (boardEnglish != null ? boardEnglish.hashCode() : 0);
        result = 31 * result + (boardManager != null ? boardManager.hashCode() : 0);
        return result;
    }

    @Override
    public int compareTo(Board another) {
        return boardEnglish.compareTo(another.boardEnglish);
    }
}
