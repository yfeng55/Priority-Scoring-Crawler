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


        int i=leftthalf_page.length-1; int icount=0;
        while(icount<5 && i>0){
            if(leftthalf_page[i].matches("^[a-zA-Z0-9_)(\n]+$") && !leftthalf_page[i].equals("\n")){
                result += leftthalf_page[i] + " ";
                icount++;
            }
            i--;
        }


        int j=0; int jcount=0;
        while(jcount<5 && j<righthalf_page.length){
            if(righthalf_page[j].matches("^[a-zA-Z0-9_)(\n]+$") && !leftthalf_page[i].equals("\n")){
                result += righthalf_page[j] + " ";
                jcount++;
            }
            j++;
        }

        return result;

    }




}
