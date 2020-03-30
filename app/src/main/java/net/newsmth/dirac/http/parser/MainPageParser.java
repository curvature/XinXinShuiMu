package net.newsmth.dirac.http.parser;

import net.newsmth.dirac.Dirac;
import net.newsmth.dirac.R;
import net.newsmth.dirac.data.MainPageItem;
import net.newsmth.dirac.util.EnhancedXmlPullParser;
import net.newsmth.dirac.util.ParseUtils;
import net.newsmth.dirac.util.ThrowingPredicate;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse http://www.newsmth.net/nForum/mainpage
 */
public class MainPageParser extends XmlParser<List<MainPageItem>> {

    public MainPageParser() {
        super("GBK");
    }

    @Override
    public List<MainPageItem> parseXml(EnhancedXmlPullParser parser) throws IOException, XmlPullParserException {
        while (!atTopTenDiv(parser)) {
            parser.nextAndThrowOnEnd();
        }

        List<MainPageItem> res = new ArrayList<>();

        readTopTen(parser, res);

        ThrowingPredicate<XmlPullParser> predicate = new ThrowingPredicate<XmlPullParser>() {
            @Override
            public boolean apply(XmlPullParser parser) throws XmlPullParserException {
                if (parser.getEventType() == XmlPullParser.START_TAG
                        && "div".equals(parser.getName())) {
                    String cssClass = parser.getAttributeValue(null, "class");
                    return cssClass != null && cssClass.startsWith("b_section block ");
                }
                return false;
            }
        };

        while (parser.jumpToPredicate(predicate)) {
            parser.jumpToTagStart("h3");
            parser.jumpToTagStart("a");
            res.add(new MainPageItem(parser.nextText()));
            parser.jumpToTagStart("ul");
            while (parser.jumpToTagStartUntilTagEnd("li", "ul")) {
                MainPageItem item = readMainPageItem(parser);
                if (item != null) {
                    res.add(item);
                }
            }
        }

        return res;
    }

    private boolean atTopTenDiv(XmlPullParser parser) throws IOException, XmlPullParserException {
        return parser.getEventType() == XmlPullParser.START_TAG
                && "div".equals(parser.getName())
                && "top10".equals(parser.getAttributeValue(null, "id"));
    }

    private void readTopTen(EnhancedXmlPullParser parser, List<MainPageItem> res) throws IOException, XmlPullParserException {
        res.add(new MainPageItem(Dirac.obtain().getString(R.string.top_ten_hot)));
        parser.jumpToTagStart("ul");
        for (int i = 0; i < 10; ++i) {
            parser.jumpToTagStart("div");
            res.add(readTopTenItem(parser));
        }
    }

    private MainPageItem readMainPageItem(EnhancedXmlPullParser parser) throws IOException, XmlPullParserException {
        // 可能出现分割线<li class="hr" />
        if (parser.next() == XmlPullParser.START_TAG
                && "a".equals(parser.getName())) {
            MainPageItem res = new MainPageItem();
            parser.jumpToTagStart("a");
            res.setBoardEnglish(ParseUtils.parseLastSegment(parser.getAttributeValue(null, "href")));
            res.setBoardChinese(removeHeadAndTail(parser.nextText()));
            parser.next();
            parser.jumpToTagStart("a");
            res.setId(ParseUtils.parseLastSegment(parser.getAttributeValue(null, "href")));
            res.setSubject(parser.nextText());
            return res;
        }
        return null;
    }

    private MainPageItem readTopTenItem(EnhancedXmlPullParser parser) throws IOException, XmlPullParserException {
        MainPageItem res = new MainPageItem();

        parser.jumpToTagStart("a");

        res.setBoardEnglish(ParseUtils.parseLastSegment(parser.getAttributeValue(null, "href")));
        res.setBoardChinese(parser.getAttributeValue(null, "title"));

        parser.next();
        parser.jumpToTagStart("a");

        res.setId(ParseUtils.parseLastSegment(parser.getAttributeValue(null, "href")));

        res.setSubject(parser.getAttributeValue(null, "title"));
        return res;
    }

    private String removeHeadAndTail(String s) {
        return s.substring(1, s.length() - 1);
    }
}
