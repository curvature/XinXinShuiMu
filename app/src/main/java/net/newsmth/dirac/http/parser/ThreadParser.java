package net.newsmth.dirac.http.parser;

import net.newsmth.dirac.http.exception.BadFormatException;
import net.newsmth.dirac.http.exception.NewsmthException;
import net.newsmth.dirac.model.ThreadSummary;
import net.newsmth.dirac.util.EnhancedXmlPullParser;
import net.newsmth.dirac.util.ParseUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse http://www.newsmth.net/nForum/board/Test?p=2
 */
public class ThreadParser extends XmlParser<List<ThreadSummary>> {

    public ThreadParser() {
        super("GBK");
    }

    @Override
    public List<ThreadSummary> parseXml(EnhancedXmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.jumpToTagStartWithAttributes("section", "id", "body", "class", "corner")) {
            if (parser.jumpToTagStart("div")) {
                if ("t-pre".equals(parser.getAttributeValue(null, "class"))) {
                    if (parser.jumpToTagStart("tbody")) {
                        List<ThreadSummary> res = new ArrayList<>();
                        while (parser.jumpToTagStartUntilTagEnd("tr", "tbody")) {
                            readThread(parser, res);
                        }
                        return res;
                    }
                } else if (parser.jumpToTagStartWithClass("div", "error")) {
                    // 可能是未登录错误，版面不存在错误
                    if (parser.jumpToTagEnd("samp")) {
                        parser.nextAndThrowOnEnd();
                        if (parser.getEventType() == XmlPullParser.TEXT) {
                            throw new NewsmthException(parser.getText());
                        }
                    }
                }
            }
        }
        throw new BadFormatException();
    }


    private void readThread(EnhancedXmlPullParser parser, List<ThreadSummary> res) throws IOException, XmlPullParserException {
        ThreadSummary post = new ThreadSummary();
        if ("top".equals(parser.getAttributeValue(null, "class"))) {
            post.isTop = true;
        }
        parser.jumpToTagStartWithClass("td", "title_9");
        parser.jumpToTagStart("a");
        post.id = ParseUtils.parseLastSegment(parser.getAttributeValue(null, "href"));
        post.subject = parser.nextText();

        if (parser.next() == XmlPullParser.START_TAG
                && "samp".equals(parser.getName())
                && "tag-att ico-pos-article-attach".equals(parser.getAttributeValue(null, "class"))) {
            post.hasAttachment = true;
        }

        parser.jumpToTagStartWithClass("a", "c63f");
        post.author = parser.nextText();

        parser.jumpToTagStart("td");
        parser.next();
        parser.jumpToTagStart("td");
        parser.next();
        parser.jumpToTagStart("td");
        post.count = Integer.parseInt(parser.nextText());

        res.add(post);
    }

    private String parseTime(String text) {
        return text.substring(text.indexOf("(") + 1, text.indexOf(")"));
    }
}
