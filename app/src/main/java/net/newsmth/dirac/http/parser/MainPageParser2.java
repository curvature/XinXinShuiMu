package net.newsmth.dirac.http.parser;

import net.newsmth.dirac.Dirac;
import net.newsmth.dirac.R;
import net.newsmth.dirac.data.MainPageItem;
import net.newsmth.dirac.util.EnhancedXmlPullParser;
import net.newsmth.dirac.util.ParseUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Parse www.newsmth.net/mainpage.html
 */
public class MainPageParser2 extends XmlParser<List<MainPageItem>> {

    public MainPageParser2() {
        super("GBK");
    }

    @Override
    public List<MainPageItem> parseXml(EnhancedXmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.jumpToTagStartWithClass("table", "HotTable")) {
            List<MainPageItem> res = new ArrayList<>();
            readTopTen(parser, res);
            while (parser.jumpToTagStartWithClass("span", "SectionName")) {
                parser.jumpToTagStart("a");
                res.add(new MainPageItem(parser.nextText()));
                for (int i = 0; i < 10; ++i) {
                    parser.jumpToTagStartWithClass("td", "SectionItem");
                    res.add(readMainPageItem(parser));
                }
            }
            return res;
        }
        return null;
    }

    private void readTopTen(EnhancedXmlPullParser parser, List<MainPageItem> res) throws IOException, XmlPullParserException {
        res.add(new MainPageItem(Dirac.obtain().getString(R.string.top_ten_hot)));
        for (int i = 0; i < 10; ++i) {
            parser.jumpToTagStartWithClass("td", "HotTitle");
            res.add(readTopTenItem(parser));
        }
    }

    private MainPageItem readMainPageItem(EnhancedXmlPullParser parser) throws IOException, XmlPullParserException {
        MainPageItem res = new MainPageItem();
        parser.jumpToTagStart("a");
        res.setBoardEnglish(ParseUtils.parseLastParameter(parser.getAttributeValue(null, "href")));
        res.setBoardChinese(parser.nextText());
        parser.jumpToTagStart("a");
        res.setId(ParseUtils.parseLastParameter(parser.getAttributeValue(null, "href")));
        res.setSubject(parser.nextText());
        return res;
    }

    private MainPageItem readTopTenItem(EnhancedXmlPullParser parser) throws IOException, XmlPullParserException {
        MainPageItem res = new MainPageItem();
        parser.jumpToTagStart("a");
        res.setBoardEnglish(ParseUtils.parseLastParameter(parser.getAttributeValue(null, "href")));
        res.setBoardChinese(parser.nextText());
        parser.jumpToTagStart("a");
        res.setId(ParseUtils.parseLastParameter(parser.getAttributeValue(null, "href")));
        res.setSubject(parser.nextText());
        parser.jumpToTagStart("a");
        res.setAuthor(parser.nextText());
        return res;
    }
}
