
import java.util.*;
import java.net.*;
import java.io.*;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;


public class WebCrawler {

    private static String url_input;                    // (-u) starting url
    private static String path_input;                   // (-docs) output path
    private static String query_input;                  // (-q) query string
    private static int maxPages;                        // (-m) max number of pages to download
    private static boolean showTrace;                   // (-t) show trace

    public static final String DISALLOW = "Disallow:";
    public static final int MAXSIZE = 20000;            // Max size (# of bits) of a file that can be downloaded

    PriorityQueue<Link> newURLs;                         // URLs to be searched (and downloaded)
    HashSet<URL> knownURLs;                             // Set of known URLs (already downloaded)
    HashSet<URL> downloadedURLs;

    public static int downloadedPages = 0;              // keep a count of how many pages have been downlaoded

    // constructor
    public WebCrawler(String u, String docs, String q, int m, boolean t){
        this.url_input = u;
        this.path_input = docs;
        this.query_input = q.toLowerCase();
        this.maxPages = m;
        this.showTrace = t;
    }


    // Crawler Loop: Keep popping a url off newURLs, download it, and accumulate new URLs
    public void run() throws IOException {

        initialize();   // add starting point url to the priorityqueue

        while(!newURLs.isEmpty() && downloadedPages<maxPages){

            Link link = newURLs.poll();
            if(showTrace){ System.out.println("\nDownloading: " + link.getURL().toString() + " Score = " + link.getScore()); }

            if(isRobotSafe(link.getURL())){

                String page = getPage(link.getURL());


                // write output to file
                File outputfile = new File(path_input + "/" + link.getURL().getPath() + ".html");
                FileUtils.writeStringToFile(outputfile, page);
                downloadedPages++;
                downloadedURLs.add(link.getURL());

                if(page.length() != 0 && downloadedPages<maxPages){
                    processPage(link, page);
                }


            }

        }

//        System.out.println("Search complete.");

    }


    // initialize: set the starting point of the crawler; set proxy and port settings
    private void initialize() {

        knownURLs = new HashSet<URL>();
        downloadedURLs = new HashSet<URL>();
        Comparator<Link> comparator = new Comparator<Link>() {
            @Override
            public int compare(Link l1, Link l2) {

                if (l1.getScore() == l2.getScore()) {
                    return l1.getArrival() < l2.getArrival() ? -1 : 1;
                }

                return (int) (l2.getScore() - l1.getScore());
            }
        };
        newURLs = new PriorityQueue<Link>(11, comparator);


        URL url;
        try{
            url = new URL(url_input);
        }catch (MalformedURLException e) {
            System.out.println("Invalid starting URL " + url_input);
            return;
        }

        knownURLs.add(url);
        newURLs.add(new Link(url, newURLs.size()));

//        System.out.println("Starting search: Initial URL " + url.toString());

        //Behind a firewall set your proxy and port here!
        Properties props= new Properties(System.getProperties());
        props.put("http.proxySet", "true");
        props.put("http.proxyHost", "webcache-cup");
        props.put("http.proxyPort", "8080");
        Properties newprops = new Properties(props);
        System.setProperties(newprops);

    }





    // adds new URL to the queue. Accept only new URL's that end in htm or html
    // oldURL is the context, newURLString is the link (either an absolute or a relative URL)
    private void addNewURL(Link link, String newUrlString, int score){

        URL url;
//        if (showTrace) { System.out.println("URL String " + newUrlString); }

        try {

            url = new URL(link.getURL(), newUrlString);

            if (!knownURLs.contains(url)) {

                String filename =  url.getFile();
                int iSuffix = filename.lastIndexOf("htm");

                if ((iSuffix == filename.length() - 3) || (iSuffix == filename.length() - 4)) {

                    if(showTrace){
                        System.out.println("Adding to queue: " + url.toString() + " Score = " + score);
                    }
                    newURLs.add(new Link(url, score, newURLs.size()));

                }
                knownURLs.add(url);

            }
            else if(!downloadedURLs.contains(url)){
                String filename =  url.getFile();
                int iSuffix = filename.lastIndexOf("htm");

                if ((iSuffix == filename.length() - 3) || (iSuffix == filename.length() - 4)) {

                    if(score > 0 && showTrace){ System.out.println("Adding " + score + " to score of " + url.toString()); }


                    //find the link in the queue and get the old score
                    int oldscore = 0;
                    Link[] newURLs_arr = newURLs.toArray(new Link[newURLs.size()]);
                    for(Link a : newURLs_arr){
//                        System.out.print(a.toString());
                        if(a.getURL().equals(url)){
//                            System.out.println("OLD SCORE: " + a.getScore());
                            oldscore = a.getScore();
                        }
                    }
//                    System.out.println();

                    newURLs.remove(new Link(url, score, newURLs.size()));


                    newURLs.add(new Link(url, oldscore + score, newURLs.size()));

                }
            }


        }
        catch(MalformedURLException e){
            return;
        }

    }


    // Download contents of URL
    private String getPage(URL url){

        try{
            // try opening the URL
            URLConnection urlConnection = url.openConnection();

            urlConnection.setAllowUserInteraction(false);

            InputStream urlStream = url.openStream();
            // search the input stream for links
            // first, read in the entire URL
            byte b[] = new byte[1000];
            int numRead = urlStream.read(b);
            String content = new String(b, 0, numRead);

            while ((numRead != -1) && (content.length() < MAXSIZE)) {
                numRead = urlStream.read(b);
                if (numRead != -1) {
                    String newContent = new String(b, 0, numRead);
                    content += newContent;
                }
            }

            if(showTrace){
                System.out.println("Received: " + url);
            }
            return content;

        }catch (IOException e){
            System.out.println("ERROR: couldn't open URL ");
            return "";
        }

    }


    // Go through page finding links to URLs.
    // A link is signalled by <a href=" ...   It ends with a close angle bracket, preceded
    // by a close quote, possibly preceded by a hatch mark (marking a fragment, an internal page marker)
    private void processPage(Link link, String page){

        String lcPage = page.toLowerCase(); // Page in lower case

        int index = 0; // position in page (index of '<a' character)
        int iEndAngle, ihref, iURL, iCloseQuote, iHatchMark, iEnd, anchorEnd;

        while((index = lcPage.indexOf("<a",index)) != -1) {

            iEndAngle = lcPage.indexOf(">",index);
            ihref = lcPage.indexOf("href",index);
            anchorEnd = lcPage.indexOf("</a>", index) + 4;

            if (ihref != -1) {

                iURL = lcPage.indexOf("\"", ihref) + 1;

                if ((iURL != -1) && (iEndAngle != -1) && (iURL < iEndAngle)){

                    iCloseQuote = lcPage.indexOf("\"",iURL);
                    iHatchMark = lcPage.indexOf("#", iURL);

                    if ((iCloseQuote != -1) && (iCloseQuote < iEndAngle)) {
                        iEnd = iCloseQuote;

                        if ((iHatchMark != -1) && (iHatchMark < iCloseQuote)){
                            iEnd = iHatchMark;
                        }

                        String newUrlString = page.substring(iURL,iEnd);

                        //get the score of the link
                        String linkhtml = page.substring(index, anchorEnd);
                        int linkscore = score(linkhtml, page, query_input, index, anchorEnd);
                        addNewURL(link, newUrlString, linkscore);
                    }
                }
            }

            index = iEndAngle;
        }


    }


    private int score(String link, String page, String query, int linkstart, int linkend){

        //convert page to lowercase
        page = page.toLowerCase();

        String link_text = Util.getLinkText(link.toLowerCase());
        String url_text = Util.getHrefText(link.toLowerCase());

        String[] query_arr = query.split(" ");


        // CASE 1: if query is null, return 0
        if(query == null || query.isEmpty()){
            return 0;
        }


        // CASE 2: if any of the words in query are substrings of the link text
        // return k*50 where 50 is the # of query word substrings in the link text
        boolean linkTextContainsWords = false;
        int k = 0;
        for(String word : query_arr){
            // check if linkText contains any of the words in query_arr
            if(link_text.contains(word)){
                linkTextContainsWords = true;
                k++;
            }
        }

        if(linkTextContainsWords){
            return k*50;
        }


        // CASE 3: if any of the words in query are a substring of the URL itself
        // return 40
        boolean urlContainsWords = false;
        for(String word : query_arr){
            // check if url contains any of the words in query_arr
            if(url_text.contains(word)){
                urlContainsWords = true;
            }
        }
        if(urlContainsWords){
            return 40;
        }



        // CASE 4:
        HashSet<String> u = new HashSet<String>();
        HashSet<String> v = new HashSet<String>();

        String link_plus_minus_fivewords = Util.getLinkText_Five(page, linkstart, linkend);

        //calculate value of u
        for(String queryterm : query_arr){

            // Check all occurrences of the word, not just first occurrence //

            String[] link_plus__minus_fivewords_arr = link_plus_minus_fivewords.split(" +");
            for(String word : link_plus__minus_fivewords_arr){

                if(word.contains(queryterm)){

                    //check that word is not a substring of another word
                    char nextChar;
                    if(queryterm.length() != word.length()){
                        nextChar = word.charAt(queryterm.length());
                    }else{
                        nextChar = ' ';
                    }

//                    System.out.println("queryterm: " + queryterm + " | word: " + word + " | nextChar: " + nextChar);

                    if(nextChar == ' ' || nextChar == '\n' || nextChar == ',' || nextChar == ';' || nextChar == '.' || nextChar == ')'){
                        u.add(queryterm);
                    }

                }

            }


        }

        String parsed_page = Jsoup.parse(page).text();

        //calculate value of v
        for(String queryterm : query_arr){

            // Check all occurrences of the word //

            String[] parsed_page_arr = parsed_page.split(" +");
            for(String word : parsed_page_arr){
                if(word.contains(queryterm)){

                    //check that word is not a substring of another word
                    char nextChar;
                    if(queryterm.length() != word.length()){
                        nextChar = word.charAt(queryterm.length());
                    }else{
                        nextChar = ' ';
                    }

                    if(nextChar == ' ' || nextChar == '\n' || nextChar == ',' || nextChar == ';' || nextChar == '.' || nextChar == ')'){
                        v.add(queryterm);
                    }

                }
            }


        }

        //calculate the set v-u
        v.removeAll(u);

        return (4 * u.size()) + v.size();



    }










    // Check that the robot exclusion protocol does not disallow downloading url
    private boolean isRobotSafe(URL url) {
        String strHost = url.getHost();

        // form URL of the robots.txt file
        String strRobot = "http://" + strHost + "/robots.txt";
        URL urlRobot;
        try{
            urlRobot = new URL(strRobot);
        }catch(MalformedURLException e){
            return false;                   // something weird is happening, so don't trust it
        }

//        if(showTrace){ System.out.println("Checking robot protocol " + urlRobot.toString()); }

        String strCommands;
        try {
            InputStream urlRobotStream = urlRobot.openStream();

            // read in entire file
            byte b[] = new byte[1000];
            int numRead = urlRobotStream.read(b);
            strCommands = new String(b, 0, numRead);

            while (numRead != -1){
                numRead = urlRobotStream.read(b);
                if(numRead != -1){
                    String newCommands = new String(b, 0, numRead);
                    strCommands += newCommands;
                }
            }
            urlRobotStream.close();

        }catch(IOException e){
            return true;                    // if there is no robots.txt file, it is OK to search
        }

//        if(showTrace){ System.out.println(strCommands); }

        // assume that this robots.txt refers to us and search for "Disallow:" commands.
        String strURL = url.getFile();
        int index = 0;

        while ((index = strCommands.indexOf(DISALLOW, index)) != -1){
            index += DISALLOW.length();
            String strPath = strCommands.substring(index);
            StringTokenizer st = new StringTokenizer(strPath);

            if (!st.hasMoreTokens()){ break; }
            String strBadPath = st.nextToken();

            // if the URL starts with a disallowed path, it is not safe
            if (strURL.indexOf(strBadPath) == 0){ return false; }
        }

        return true;
    }




}















