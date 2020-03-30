package net.newsmth.dirac.data;

public class MainPageItem {

    public long headerId;
    public String headerText;
    private String id;
    private String subject;
    private String author;
    private String boardEnglish;
    private String boardChinese;

    public MainPageItem() {

    }

    public MainPageItem(String subject) {
        this.subject = subject;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBoardEnglish() {
        return boardEnglish;
    }

    public void setBoardEnglish(String boardEnglish) {
        this.boardEnglish = boardEnglish;
    }

    public String getBoardChinese() {
        return boardChinese;
    }

    public void setBoardChinese(String boardChinese) {
        this.boardChinese = boardChinese;
    }

    @Override
    public String toString() {
        return "MainPageItem{" +
                "id='" + id + '\'' +
                ", subject='" + subject + '\'' +
                ", author='" + author + '\'' +
                ", boardEnglish='" + boardEnglish + '\'' +
                ", boardChinese='" + boardChinese + '\'' +
                '}';
    }
}
