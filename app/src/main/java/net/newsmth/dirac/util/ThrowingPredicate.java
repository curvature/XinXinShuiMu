package net.newsmth.dirac.util;

import org.xmlpull.v1.XmlPullParserException;

public interface ThrowingPredicate<T> {

    boolean apply(T t) throws XmlPullParserException;
}
