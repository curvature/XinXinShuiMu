package net.newsmth.dirac.http.parser;

import android.util.Xml;

import net.newsmth.dirac.http.response.TypedResponse;
import net.newsmth.dirac.util.EnhancedXmlPullParser;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;

import okhttp3.ResponseBody;

public abstract class XmlParser<T> {

    private final String mCharset;

    public XmlParser() {
        mCharset = null;
    }

    public XmlParser(String charset) {
        mCharset = charset;
    }

    public final TypedResponse<T> parseResponse(ResponseBody responseBody)
            throws IOException, XmlPullParserException {
        TypedResponse<T> res = new TypedResponse<>();
        try {
            EnhancedXmlPullParser parser = new EnhancedXmlPullParser(Xml.newPullParser());
            parser.setFeature(Xml.FEATURE_RELAXED, true);
            parser.setInput(responseBody.byteStream(), mCharset);
            parser.nextTag();
            res.data = parseXml(parser);
            return res;
        } finally {
            if (responseBody != null) {
                responseBody.close();
            }
        }
    }

    /**
     * 解析XML，此方法在后台线程被调用。
     *
     * @param parser 保证不为null
     * @return 返回正常解析出的对象，若解析失败，抛出异常
     */

    public abstract T parseXml(EnhancedXmlPullParser parser) throws IOException, XmlPullParserException;
}
