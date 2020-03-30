package net.newsmth.dirac.http.parser;

import net.newsmth.dirac.data.Board;
import net.newsmth.dirac.util.EnhancedXmlPullParser;
import net.newsmth.dirac.util.ParseUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Parse www.newsmth.net/nForum/section/xxx
 */
public class BoardParser extends XmlParser<List<Board>> {

    public BoardParser() {
        super("GBK");
    }

    @Override
    public List<Board> parseXml(EnhancedXmlPullParser parser) throws IOException, XmlPullParserException {
        List<Board> res = new LinkedList<>();
        if (parser.jumpToTagStartWithClass("table", "board-list corner")
                && parser.jumpToTagStart("tbody")) {
            while (parser.jumpToTagStartUntilTagEnd("tr", "tbody")) {
                readBoard(parser, res);
            }
        }
        return res;
    }

    private void readBoard(EnhancedXmlPullParser parser, List<Board> res) throws IOException, XmlPullParserException {
        if (parser.jumpToTagStartWithClass("td", "title_1")
                && parser.jumpToTagStart("a")) {
            Board board = new Board();
            String href = parser.getAttributeValue(null, "href");
            board.boardEnglish = ParseUtils.parseLastSegment(href);
            board.boardChinese = parser.nextText();
            if (href.startsWith("/nForum/section/")) {
                board.section = board.boardEnglish;
            } else {
                parser.jumpToTagStartWithClass("td", "title_2");
                if (parser.jumpToTagStartUntilTagEnd("a", "td")) {
                    board.boardManager = parser.nextText();
                }
            }

            res.add(board);
        }
    }
}
