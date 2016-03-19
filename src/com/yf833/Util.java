package com.yf833;
import java.util.regex.Matcher;
import java.util.regex.Pattern;




public class Util {

    //extract text between anchor tags
    public static String getLinkText(String htmltext){

        String link_text = "";

        Pattern titleFinder = Pattern.compile("<a[^>]*>(.*?)</a>", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher regexMatcher = titleFinder.matcher(htmltext);
        while (regexMatcher.find()) {
            // matched text: regexMatcher.group(1)
            link_text = regexMatcher.group(1);
        }

        return link_text;
    }


    //extract URL from link text
    public static String getHrefText(String htmltext){

        String url_text = "";

        Pattern titleFinder = Pattern.compile("href=[\\'\"]?([^\\'\" >]+)", Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
        Matcher regexMatcher = titleFinder.matcher(htmltext);
        while (regexMatcher.find()) {
            // matched text: regexMatcher.group(1)
            url_text = regexMatcher.group(1);
        }

        return url_text;
    }


}
