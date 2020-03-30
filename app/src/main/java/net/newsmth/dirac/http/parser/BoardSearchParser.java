package net.newsmth.dirac.http.parser;

import net.newsmth.dirac.http.exception.BadFormatException;
import net.newsmth.dirac.model.ThreadSummary;
import net.newsmth.dirac.util.EnhancedXmlPullParser;
import net.newsmth.dirac.util.ParseUtils;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Parse http://www.newsmth.net/nForum/board/Test?p=2
 */
public class BoardSearchParser extends XmlParser<List<ThreadSummary>> {

    public BoardSearchParser() {
        super("GBK");
    }

    @Override
    public List<ThreadSummary> parseXml(EnhancedXmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.jumpToTagStartWithClass("table", "board-list tiz")) {
            if (parser.jumpToTagEnd("thead")) {
                if (parser.jumpToTagStart("tr")) {
                    if (parser.jumpToTagStart("td")) {

                        /**
                         *
                         * Check for empty result
                         *
                         <table class="board-list tiz" cellpadding="0" cellspacing="0">
                         <thead>
                         <tr>
                         <th class="title_8">序号</th>
                         <th class="title_14">状态</th>
                         <th class="title_9 middle">主题</th>
                         <th class="title_10">发帖时间</th>
                         <th class="title_12">|&ensp;作者</th>
                         <th class="title_11 middle">回复</th>
                         <th class="title_10">最新回复</th>
                         <th class="title_12">|&ensp;作者</th>
                         </tr>
                         </thead>
                         <tr>
                         <td colspan="7" style="text-align:center">没有搜索到任何主题</td>
                         </tr>
                         </table>
                         *
                         *
                         */

                        if ("7".equals(parser.getAttributeValue(null, "colspan"))) {
                            return Collections.emptyList();
                        }

                        /**
                         *
                         * <tr>
                         <td class="title_8">1.</td>
                         <td class="title_14">
                         <a target="_blank" href="/nForum/article/Movie/2887879" title="在新窗口打开此主题">
                         <samp class="tag ico-pos-article-normal"></samp>
                         </a>
                         </td>
                         <td class="title_9">
                         <a href="/nForum/article/Movie/2887879">重温了一遍兄弟连，里面居然有法鲨和Hardy！</a>
                         </td>
                         <td class="title_10">09:50:58&emsp;</td>
                         <td class="title_12">|&ensp;
                         <a href="/nForum/user/query/chanz" class="c63f">chanz</a>
                         </td>
                         <td class="title_11 middle">0</td>
                         <td class="title_10">
                         <a href="/nForum/article/Movie/2887879?p=1#a0" title="跳转至最后回复">09:50:58&emsp;</a>
                         </td>
                         <td class="title_12">|&ensp;
                         <a href="/nForum/user/query/chanz" class="c09f">chanz</a>
                         </td>
                         </tr>

                         </table>结尾
                         *
                         *
                         */
                        List<ThreadSummary> res = new ArrayList<>();

                        while (readThread(parser, res)) ;

                        return res;
                    }
                }
            }
        }
        throw new BadFormatException();
    }


    private boolean readThread(EnhancedXmlPullParser parser, List<ThreadSummary> res) throws IOException, XmlPullParserException {
        ThreadSummary post = new ThreadSummary();

        if (!parser.jumpToTagStartWithClass("td", "title_14")
                || !parser.jumpToTagStartUntilTagEnd("samp", "td")) {
            return false;
        }

        if ("tag ico-pos-article-top".equals(parser.getAttributeValue(null, "class"))) {
            post.isTop = true;
        }

        parser.jumpToTagStartWithClass("td", "title_9");
        parser.jumpToTagStart("a");
        post.id = ParseUtils.parseLastSegment(parser.getAttributeValue(null, "href"));
        post.subject = parser.nextText();

        parser.jumpToTagStartWithClass("a", "c63f");
        post.author = parser.nextText();

        parser.jumpToTagStartWithClass("td", "title_11 middle");
        post.count = Integer.parseInt(parser.nextText());

        res.add(post);

        return parser.jumpToTagStartUntilTagEnd("tr", "table");
    }
}
