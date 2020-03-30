package net.newsmth.dirac.util;

public abstract class ParseUtils {

    /**
     * @param href 形式为 xxxx?key1=value1&key2=value2
     * @return
     */
    public static String parseLastParameter(String href) {
        return href.substring(href.lastIndexOf("=") + 1);
    }

    /**
     * @param href 形式为 /article/Picture/12345
     * @return
     */
    public static String parseLastSegment(String href) {
        return href.substring(href.lastIndexOf("/") + 1);
    }

}
