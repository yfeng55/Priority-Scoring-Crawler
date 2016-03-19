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
            url_text = regexMatcher.group(1);
        }

        return url_text;
    }


    //get a string that's plus/minus five words from the link text (excluding the link itself)
    public static String getLinkText_Five(String page, int linkstart, int linkend){

        String result = "";

        String[] leftthalf_page = page.substring(0, linkstart).split(" +");
        String[] righthalf_page = page.substring(linkend).split(" +");

        for(int i=leftthalf_page.length-1; i>leftthalf_page.length-6; i--){
            result += leftthalf_page[i] + " ";
        }
        for(int i=0; i<5; i++){
            result += righthalf_page[i] + " ";
        }

        return result;

    }


}
