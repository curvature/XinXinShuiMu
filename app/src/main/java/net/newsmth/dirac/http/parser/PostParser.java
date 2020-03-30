package net.newsmth.dirac.http.parser;

import android.text.Html;

import net.newsmth.dirac.data.Author;
import net.newsmth.dirac.data.Post;
import net.newsmth.dirac.data.TextOrImage;
import net.newsmth.dirac.http.exception.BadFormatException;
import net.newsmth.dirac.http.exception.NewsmthException;
import net.newsmth.dirac.ip.IpQueryHelper;
import net.newsmth.dirac.util.EmoticonGetter;
import net.newsmth.dirac.util.EnhancedXmlPullParser;
import net.newsmth.dirac.util.ParseUtils;
import net.newsmth.dirac.util.RetrofitUtils;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

/**
 * Parse http://www.newsmth.net/nForum/article/Test/911834?p=2
 */
public class PostParser extends XmlParser<ArrayList<Post>> {

    private final boolean parseHtml;

    public PostParser(boolean parseHtml) {
        super("GBK");
        this.parseHtml = parseHtml;
    }

    @Override
    public ArrayList<Post> parseXml(EnhancedXmlPullParser parser) throws IOException, XmlPullParserException {
        if (parser.jumpToTagStartWithClass("div", "b-content corner")) {
            parser.nextAndThrowOnEnd();
            if (parser.jumpToTagStart("div")) {
                if ("error".equals(parser.getAttributeValue(null, "class"))) {
                    // 可能是未登录错误，文章不存在错误
                    if (parser.jumpToTagEnd("samp")) {
                        parser.nextAndThrowOnEnd();
                        if (parser.getEventType() == XmlPullParser.TEXT) {
                            throw new NewsmthException(parser.getText());
                        }
                    }
                } else {
                    // FIXME check if has valid posts
                    ArrayList<Post> res = new ArrayList<>();
                    while (parser.jumpToTagStartWithClassUntilTagStartWithClass("table", "article", "ul", "pagination")) {
                        readPost(parser, res);
                    }

                    if (res.size() > 0) {
                        parser.jumpToTagStart("i");
                        try {
                            res.get(0).totalPost = Integer.parseInt(parser.nextText());
                        } catch (NumberFormatException e) {
                            throw new BadFormatException();
                        }
                    }

                    return res;
                }
            }
        }

        throw new BadFormatException();
    }

    private void readPost(EnhancedXmlPullParser parser, List<Post> res) throws IOException, XmlPullParserException {
        parser.jumpToTagStartWithClass("span", "a-u-name");

        // 对非deliver用户，存在a标签，需跳过
        while (parser.next() != XmlPullParser.TEXT) {
        }

        Author author = new Author();
        author.username = parser.getText();


        parser.jumpToTagStart("samp");
        author.sex = parseSex(parser.getAttributeValue(null, "title"));

        Post post = new Post();
        post.author = author;

        parser.jumpToTagStartWithClass("a", "a-post");
        post.id = ParseUtils.parseLastSegment(parser.getAttributeValue(null, "href"));

        parser.jumpToTagStartWithClass("span", "a-pos");
        post.floor = parseFloor(parser.nextText());

        switch (author.username) {
            case "deliver":
                author.nickname = "自动发信系统";
                break;
            case "SYSOP":
                author.nickname = "System Operator";
                break;
            default:
                break;
        }

        if (parser.jumpToTagStartUntilTagStartWithClass("img", "td", "a-content")) {
            author.avatarUrl = prependHead(parser.getAttributeValue(null, "src"));
            if (parser.jumpToTagStartWithClassUntilTagStartWithClass("div", "a-u-uid", "td", "a-content")) {
                author.nickname = parser.nextText();
                parser.jumpToTagStartWithClass("td", "a-content");
            }
        }

        /**
         * 使用br标签定位，因为昵称可能含有a标签，next到标题的步数不确定
         * <td class="a-content"><p>发信人: caoyangorg (<a target="_blank" href="http://www.caoyang.org">http://www.caoyang.org</a>), 信区: FamilyLife <br /> 标&nbsp;&nbsp;题: Re: 老婆快30了还不想要孩子 <br />
         */
        if (!parser.jumpToTagStartUntilTagEnd("br", "td")) {
            /**
             * 有可能帖子内容为空 http://www.newsmth.net/nForum/article/ITExpress/1665838?p=2
             <td class="a-content">
             <p></p>
             </td>
             */
            post.contentList = Collections.emptyList();
            post.buildReplyQuote(null);
            res.add(post);
            return;
        }
        parser.next(2);
        // FIXME 标题也可能含有a标签
        post.title = parseTitle(parser.getText());

        parser.jumpToTagStart("br");
        parser.next(2);
        post.time = parseTime(parser.getText());

        parser.next(5);

        post.contentList = parser.parseContentListUntil("td", author);

        boolean first = true;
        int imageCount = 0;
        LinkedList<String> firstFourLines = new LinkedList<>();
        for (int i = post.contentList.size() - 1; i >= 0; --i) {
            TextOrImage textOrImage = post.contentList.get(i);
            if (textOrImage.type == TextOrImage.TYPE_TEXT) {
                if (first) {
                    String text = textOrImage.data.toString();
                    int start = indexOfSourceStart(text);
                    if (start >= 0) {
                        int end;
                        if (i == post.contentList.size() - 1) {
                            end = text.indexOf("</p>", start);
                        } else {
                            end = text.indexOf("</font>", start);
                            if (end >= 0) {
                                end = text.indexOf("</font>", end + 1);
                            }
                        }
                        if (end >= 0) {
                            first = false;
                            textOrImage.data = text.substring(0, start) + text.substring(end);
                            post.ip = parseIp(text, start);
                            if (IpQueryHelper.getInstance().isEnabled()) {
                                IpQueryHelper.IPZone loc = IpQueryHelper.getInstance().queryIp(post.ip);
                                if (loc != null) {
                                    post.ipLocation = loc.toString();
                                }
                            }
                        }
                    }
                }

                int lineCount = 0;
                Scanner scanner = new Scanner(textOrImage.data.toString()
                        .replaceAll(" <br> ", "\n")
                        .replaceAll(" <br>", "\n")
                        .replaceAll("<br> ", "\n")
                        .replaceAll("<br>", "\n"));
                while (scanner.hasNextLine()) {
                    String line = scanner.nextLine();
                    if (firstFourLines.isEmpty() && line.startsWith(" ")) {
                        line = line.substring(1);
                    }
                    line = line.replaceAll("&nbsp;", " ");
                    if (line.trim().isEmpty()) {
                        continue;
                    }
                    if (line.startsWith(": ") || line.startsWith("【 在 ")
                            || "--".equals(line) || line.contains("<")) {
                        break;
                    }
                    firstFourLines.add(lineCount, ": " + line);
                    ++lineCount;
                    if (firstFourLines.size() > 4) {
                        firstFourLines.removeLast();
                    }
                    if (lineCount == 4) {
                        break;
                    }
                }
                scanner.close();
                if (parseHtml) {
                    textOrImage.data = Html.fromHtml(textOrImage.data.toString(),
                            new EmoticonGetter(), null);
                }
                textOrImage.parsed = parseHtml;
            } else {
                ++imageCount;
            }
        }
        if (imageCount > 0) {
            post.imageUrlArray = new String[imageCount];
            imageCount = 0;
            for (TextOrImage textOrImage : post.contentList) {
                if (textOrImage.type == TextOrImage.TYPE_IMAGE) {
                    post.imageUrlArray[imageCount] = textOrImage.data.toString();
                    ++imageCount;
                }
            }
        }
        post.buildReplyQuote(firstFourLines);
        res.add(post);
    }

    private String prependHead(String s) {
        if (s.startsWith("//")) return RetrofitUtils.getScheme() + s;
        return s;
    }

    private String removeHeadAndTail(String s) {
        return s.substring(1, s.length() - 1);
    }

    private int parseFloor(String text) {
        if (text.startsWith("第") && text.endsWith("楼")) {
            return Integer.parseInt(removeHeadAndTail(text));
        }
        return 0;
    }

    private int parseSex(String text) {
        if (text.startsWith("男")) {
            return Author.SEX_MALE;
        } else if (text.startsWith("女")) {
            return Author.SEX_FEMALE;
        }
        // deliver 性别是隐藏
        return Author.SEX_UNKNOWN;
    }

    private String parseTitle(String text) {
        return text.substring(17);
    }

    private String parseTime(String text) {
        return sanitizeTime(text.substring(text.indexOf("(") + 1, text.indexOf(")")));
    }

    private String sanitizeTime(String text) {
        return text.replaceAll("&nbsp;", " ").replace("  ", " ");
    }

    private int indexOfSourceStart(String body) {
        return body.lastIndexOf("※") - 19;
    }

    private String parseIp(String body, int startIndex) {
        int ipStartIndex = body.indexOf("FROM:", startIndex) + 6;
        return body.substring(ipStartIndex, body.indexOf("]", ipStartIndex));
    }
}
