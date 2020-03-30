package net.newsmth.dirac.util;

import androidx.annotation.NonNull;

import net.newsmth.dirac.data.Author;
import net.newsmth.dirac.data.TextOrImage;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

public class EnhancedXmlPullParser implements XmlPullParser {

    @NonNull
    private final XmlPullParser mParser;

    public EnhancedXmlPullParser(@NonNull XmlPullParser parser) {
        mParser = parser;
    }

    public void next(int step) throws IOException, XmlPullParserException {
        for (int i = 0; i < step; ++i) {
            mParser.next();
        }
    }

    public String getCssClass() {
        return mParser.getAttributeValue(null, "class");
    }

    public void nextAndThrowOnEnd() throws IOException, XmlPullParserException {
        if (mParser.next() == XmlPullParser.END_DOCUMENT) {
            throw new XmlPullParserException("Document end early");
        }
    }

    public boolean jumpToTagStart(String tag) throws XmlPullParserException, IOException {
        while (mParser.getEventType() != XmlPullParser.START_TAG
                || !tag.equals(mParser.getName())) {
            if (mParser.next() == XmlPullParser.END_DOCUMENT) {
                return false;
            }
        }
        return true;
    }

    public boolean jumpToTagStartUntilTagEnd(String tag, String endTag) throws XmlPullParserException, IOException {
        while (mParser.getEventType() != XmlPullParser.START_TAG
                || !tag.equals(mParser.getName())) {
            if (mParser.next() == XmlPullParser.END_DOCUMENT) {
                return false;
            }
            if (mParser.getEventType() == XmlPullParser.END_TAG
                    && endTag.equals(mParser.getName())) {
                return false;
            }
        }
        return true;
    }

    public boolean jumpToTagStartUntilTagStartWithClass(String tag, String startTag, String startClass) throws XmlPullParserException, IOException {
        while (mParser.getEventType() != XmlPullParser.START_TAG
                || !tag.equals(mParser.getName())) {
            if (mParser.next() == XmlPullParser.END_DOCUMENT) {
                return false;
            }
            if (mParser.getEventType() == XmlPullParser.START_TAG
                    && startTag.equals(mParser.getName())
                    && startClass.equals(mParser.getAttributeValue(null, "class"))) {
                return false;
            }
        }
        return true;
    }

    public boolean jumpToTagStartWithClassUntilTagStartWithClass(String tag, String clazz, String startTag, String startClass) throws XmlPullParserException, IOException {
        while (mParser.getEventType() != XmlPullParser.START_TAG
                || !tag.equals(mParser.getName())
                || !clazz.equals(mParser.getAttributeValue(null, "class"))) {
            if (mParser.next() == XmlPullParser.END_DOCUMENT) {
                return false;
            }
            if (mParser.getEventType() == XmlPullParser.START_TAG
                    && startTag.equals(mParser.getName())
                    && startClass.equals(mParser.getAttributeValue(null, "class"))) {
                return false;
            }
        }
        return true;
    }

    public boolean jumpToTagEnd(String tag) throws XmlPullParserException, IOException {
        while (mParser.getEventType() != XmlPullParser.END_TAG
                || !tag.equals(mParser.getName())) {
            if (mParser.next() == XmlPullParser.END_DOCUMENT) {
                return false;
            }
        }
        return true;
    }

    public boolean jumpToTagStartWithClass(String tag, String cssClass) throws XmlPullParserException, IOException {
        while (mParser.getEventType() != XmlPullParser.START_TAG
                || !tag.equals(mParser.getName())
                || !cssClass.equals(mParser.getAttributeValue(null, "class"))) {
            if (mParser.next() == XmlPullParser.END_DOCUMENT) {
                return false;
            }
        }
        return true;
    }

    /**
     * @param tag
     * @param pair 属性name，value对，可以有多组
     * @return
     * @throws XmlPullParserException
     * @throws IOException
     */
    public boolean jumpToTagStartWithAttributes(String tag, @NonNull String... pair) throws XmlPullParserException, IOException {
        while (mParser.getEventType() != XmlPullParser.START_TAG
                || !tag.equals(mParser.getName())
                || mismatch(pair)) {
            if (mParser.next() == XmlPullParser.END_DOCUMENT) {
                return false;
            }
        }
        return true;
    }

    public boolean isNotEnd() throws XmlPullParserException {
        return mParser.getEventType() != XmlPullParser.END_DOCUMENT;
    }

    /**
     * 任意一个不match则返回true
     */
    private boolean mismatch(@NonNull String... pair) {
        for (int i = 0; i < pair.length; i += 2) {
            if (!pair[i + 1].equals(mParser.getAttributeValue(null, pair[i]))) {
                return true;
            }
        }
        return false;
    }

    public boolean jumpToTagStartWithClassUntilTagEnd(String tag, String cssClass, String endTag) throws XmlPullParserException, IOException {
        while (mParser.getEventType() != XmlPullParser.START_TAG
                || !tag.equals(mParser.getName())
                || !cssClass.equals(mParser.getAttributeValue(null, "class"))) {
            if (mParser.next() == XmlPullParser.END_DOCUMENT) {
                return false;
            }
            if (mParser.getEventType() == XmlPullParser.END_TAG
                    && endTag.equals(mParser.getName())) {
                return false;
            }
        }
        return true;
    }

    public boolean jumpToPredicate(ThrowingPredicate<XmlPullParser> predicate) throws IOException, XmlPullParserException {
        while (!predicate.apply(mParser)) {
            if (mParser.next() == XmlPullParser.END_DOCUMENT) {
                return false;
            }
        }
        return true;
    }

    public String getHtmlUntilTagEnd(String tag)
            throws XmlPullParserException, IOException {
        StringBuilder sb = new StringBuilder();
        while (true) {
            switch (next()) {
                case XmlPullParser.END_TAG:
                    if (tag.equals(getName())) {
                        return sb.toString();
                    }
                    if (!"br".equals(getName())) {
                        sb.append("</").append(getName()).append(">");
                    }
                    break;
                case XmlPullParser.START_TAG:
                    StringBuilder attrs = new StringBuilder();
                    for (int i = 0; i < getAttributeCount(); i++) {
                        attrs.append(getAttributeName(i)).append("=\"")
                                .append(getAttributeValue(i)).append("\" ");
                    }
                    if (attrs.length() > 0) {
                        attrs.setLength(attrs.length() - 1);
                    }
                    sb.append("<").append(getName()).append(" ")
                            .append(attrs.toString()).append(">");
                    break;
                case XmlPullParser.END_DOCUMENT:
                    return sb.toString();
                default:
                    sb.append(getText());
                    break;
            }
        }
    }

    public List<TextOrImage> parseContentListUntil(String tag, Author author)
            throws XmlPullParserException, IOException {
        List<TextOrImage> res = new ArrayList<>();
        final StringBuilder sb = new StringBuilder();
        boolean behindATagStart = false;
        while (true) {
            switch (next()) {
                case XmlPullParser.END_TAG:
                    if (tag.equals(getName())) {
                        if (sb.length() > 0) {
                            res.add(new TextOrImage(TextOrImage.TYPE_TEXT, sb.toString()));
                        }
                        return res;
                    }
                    if (!"br".equals(getName())) {
                        sb.append("</").append(getName()).append(">");
                    }
                    behindATagStart = false;
                    break;
                case XmlPullParser.START_TAG:
                    // 我要like按钮，跳过
                    if ("div".equals(getName()) && "t-pre-bottom t-btn".equals(getCssClass())) {
                        skip();
                        break;
                    } else if ("div".equals(getName()) && "a-audio".equals(getCssClass())) {
            /*
             音频附件，格式为
             <font color="blue">附件(3.5MB)</font>&nbsp;
             <a href="//att.newsmth.net/nForum/att/Karaoke/1199915/232" target="_blank">朋友别哭_(V4).mp3(在新窗口打开)</a>
             <br />
             <br />
             <div class="a-audio" _src="//att.newsmth.net/nForum/att/Karaoke/1199915/232">
             */

                        int i = sb.lastIndexOf("target=\"_blank\">");
                        if (i < 0) break;
                        int j = sb.lastIndexOf("</a>");
                        if (j < 0 || i > j) break;
                        String title = sb.substring(i + 16, j);
                        if (title.endsWith("(在新窗口打开)")) {
                            title = title.substring(0, title.length() - 8);
                        }
                        i = sb.lastIndexOf("<font color=\"blue\">", i);
                        if (i < 0) break;
                        sb.setLength(i);
                        TextOrImage audio = new TextOrImage(TextOrImage.TYPE_AUDIO, title);
                        audio.url = getAttributeValue(null, "_src");
                        if (audio.url == null) break;
                        audio.url = prependHead(audio.url);
                        audio.artist = author;
                        res.add(audio);
                        behindATagStart = false;
                    } else if ("img".equals(getName()) && behindATagStart) {
                        int hit = sb.lastIndexOf("<br>");
                        if (hit > 0) {
                            sb.setLength(hit);
                            res.add(new TextOrImage(TextOrImage.TYPE_TEXT, sb.toString()));
                        }
                        sb.setLength(0);
                        res.add(new TextOrImage(TextOrImage.TYPE_IMAGE,
                                prependHead(removeTail(getAttributeValue(null, "src")))));
                        jumpToTagEnd("a");
                        behindATagStart = false;
                    } else {
                        StringBuilder attrs = new StringBuilder();
                        for (int i = 0; i < getAttributeCount(); i++) {
                            attrs.append(getAttributeName(i)).append("=\"")
                                    .append(getAttributeValue(i)).append("\" ");
                        }
                        if (attrs.length() > 0) {
                            attrs.setLength(attrs.length() - 1);
                            attrs.insert(0, " ");
                        }
                        sb.append("<").append(getName()).append(attrs.toString()).append(">");
                        behindATagStart = "a".equals(getName());
                    }
                    break;
                case XmlPullParser.END_DOCUMENT:
                    if (sb.length() > 0) {
                        res.add(new TextOrImage(TextOrImage.TYPE_TEXT, sb.toString()));
                    }
                    return res;
                default:
                    sb.append(getText());
                    behindATagStart = false;
                    break;
            }
        }
    }

    /**
     * 注意仅可用于没有省略end tag的地方，因为实现完全依靠开闭tag计数
     *
     * @throws XmlPullParserException
     * @throws IOException
     */
    private void skip() throws XmlPullParserException, IOException {
        if (getEventType() != XmlPullParser.START_TAG) {
            throw new IllegalStateException();
        }
        int depth = 1;
        while (depth != 0) {
            switch (next()) {
                case XmlPullParser.END_TAG:
                    depth--;
                    break;
                case XmlPullParser.START_TAG:
                    depth++;
                    break;
            }
        }
    }

    private String prependHead(String s) {
        if (s.startsWith("//")) {
            return RetrofitUtils.getScheme() + s;
        } else if (s.startsWith("/")) {
            return RetrofitUtils.getScheme() + "//www.newsmth.net" + s;
        }
        return s;
    }

    // for gif, url ends with large is *jpeg*, remove to get gif
    private String removeTail(String s) {
        if (s.startsWith("//att.newsmth.net/nForum/att/") && s.endsWith("/large")) {
            return s.substring(0, s.length() - 6);
        }
        return s;
    }

    @Override
    public void setFeature(String name, boolean state) throws XmlPullParserException {
        mParser.setFeature(name, state);
    }

    @Override
    public boolean getFeature(String name) {
        return mParser.getFeature(name);
    }

    @Override
    public void setProperty(String name, Object value) throws XmlPullParserException {
        mParser.setProperty(name, value);
    }

    @Override
    public Object getProperty(String name) {
        return mParser.getProperty(name);
    }

    @Override
    public void setInput(Reader in) throws XmlPullParserException {
        mParser.setInput(in);
    }

    @Override
    public void setInput(InputStream inputStream, String inputEncoding) throws XmlPullParserException {
        mParser.setInput(inputStream, inputEncoding);
    }

    @Override
    public String getInputEncoding() {
        return mParser.getInputEncoding();
    }

    @Override
    public void defineEntityReplacementText(String entityName, String replacementText) throws XmlPullParserException {
        mParser.defineEntityReplacementText(entityName, replacementText);
    }

    @Override
    public int getNamespaceCount(int depth) throws XmlPullParserException {
        return mParser.getNamespaceCount(depth);
    }

    @Override
    public String getNamespacePrefix(int pos) throws XmlPullParserException {
        return mParser.getNamespacePrefix(pos);
    }

    @Override
    public String getNamespaceUri(int pos) throws XmlPullParserException {
        return mParser.getNamespaceUri(pos);
    }

    @Override
    public String getNamespace(String prefix) {
        return mParser.getNamespace(prefix);
    }

    @Override
    public int getDepth() {
        return mParser.getDepth();
    }

    @Override
    public String getPositionDescription() {
        return mParser.getPositionDescription();
    }

    @Override
    public int getLineNumber() {
        return mParser.getLineNumber();
    }

    @Override
    public int getColumnNumber() {
        return mParser.getColumnNumber();
    }

    @Override
    public boolean isWhitespace() throws XmlPullParserException {
        return mParser.isWhitespace();
    }

    @Override
    public String getText() {
        return mParser.getText();
    }

    @Override
    public char[] getTextCharacters(int[] holderForStartAndLength) {
        return mParser.getTextCharacters(holderForStartAndLength);
    }

    @Override
    public String getNamespace() {
        return mParser.getNamespace();
    }

    @Override
    public String getName() {
        return mParser.getName();
    }

    @Override
    public String getPrefix() {
        return mParser.getPrefix();
    }

    @Override
    public boolean isEmptyElementTag() throws XmlPullParserException {
        return mParser.isEmptyElementTag();
    }

    @Override
    public int getAttributeCount() {
        return mParser.getAttributeCount();
    }

    @Override
    public String getAttributeNamespace(int index) {
        return mParser.getAttributeNamespace(index);
    }

    @Override
    public String getAttributeName(int index) {
        return mParser.getAttributeName(index);
    }

    @Override
    public String getAttributePrefix(int index) {
        return mParser.getAttributePrefix(index);
    }

    @Override
    public String getAttributeType(int index) {
        return mParser.getAttributeType(index);
    }

    @Override
    public boolean isAttributeDefault(int index) {
        return mParser.isAttributeDefault(index);
    }

    @Override
    public String getAttributeValue(int index) {
        return mParser.getAttributeValue(index);
    }

    @Override
    public String getAttributeValue(String namespace, String name) {
        return mParser.getAttributeValue(namespace, name);
    }

    @Override
    public int getEventType() throws XmlPullParserException {
        return mParser.getEventType();
    }

    @Override
    public int next() throws XmlPullParserException, IOException {
        return mParser.next();
    }

    @Override
    public int nextToken() throws XmlPullParserException, IOException {
        return mParser.nextToken();
    }

    @Override
    public void require(int type, String namespace, String name) throws XmlPullParserException, IOException {
        mParser.require(type, namespace, name);
    }

    @Override
    public String nextText() throws XmlPullParserException, IOException {
        return mParser.nextText();
    }

    @Override
    public int nextTag() throws XmlPullParserException, IOException {
        return mParser.nextTag();
    }
}