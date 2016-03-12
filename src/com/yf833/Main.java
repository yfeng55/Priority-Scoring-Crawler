package com.yf833;

// A minimal Web Crawler written in Java
// Usage: From command line
//     java WebCrawler <URL> [N]
//  where URL is the url to start the crawl, and N (optional)
//  is the maximum number of pages to download.


public class Main {


    public static void main(String[] args){

        WebCrawler wc = new WebCrawler();
        wc.run(args);


    }

}




