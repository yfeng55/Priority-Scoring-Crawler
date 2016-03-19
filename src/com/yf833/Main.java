package com.yf833;

// A minimal Web Crawler written in Java
// Usage: From command line
//     java WebCrawler <URL> [N]
// ex: http://cs.nyu.edu/courses/spring16/CSCI-GA.2580-001/MarineMammal/

//  where URL is the url to start the crawl, and N (optional)
//  is the maximum number of pages to download.


import java.io.IOException;


public class Main {

    private static String url_input;                // -u
    private static String path_input;               // -docs
    private static String query_input = "";         // -q
    private static int maxPages = 50;               // -m
    private static boolean showTrace = false;       // -t



    public static void main(String[] args) throws IOException {

        //get input values from string arguments
        getInput(args);

        WebCrawler crawler = new WebCrawler(url_input, path_input, query_input, maxPages, showTrace);
        crawler.run();


//        System.out.println(Util.getHrefText("<a href=\"bla/bla.html\"> HELLO WORLD </a>"));
//        System.out.println(Util.getLinkText("<a href=\"bla/bla.html\"> HELLO WORLD </a>"));


//        String page = "a b c d e f g h <a href = 'asdf/asdf.html'>link</a> i j k l m n o p q r s t u v";
//        System.out.println(Util.getLinkText_Five(page, 16, 52));


    }



    private static void getInput(String[] args){
        for(int i=0; i<args.length; i++){
            switch(args[i]){
                case "-u":
                    url_input = args[i+1];
                    i++;
                    break;
                case "-docs":
                    path_input = args[i+1];
                    i++;
                    break;
                case "-q":
                    query_input = args[i+1];
                    i++;
                    break;
                case "-m":
                    maxPages = Integer.parseInt(args[i+1]);
                    i++;
                    break;
                case "-t":
                    showTrace = true;
                    break;
                default:
                    throw new IllegalArgumentException("Must provide arguments: " + "-u <url> -q <query> -docs <path> -m <max pages> -t");
            }
        }
//        System.out.println("-u: " + url_input);
//        System.out.println("-docs: " + path_input);
//        System.out.println("-q: " + query_input);
//        System.out.println("-m: " + maxPages);
//        System.out.println("-t: " + showTrace);
    }

}




