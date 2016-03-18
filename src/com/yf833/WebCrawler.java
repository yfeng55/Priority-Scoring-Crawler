package com.yf833;
import java.util.*;
import java.net.*;
import java.io.*;


public class WebCrawler {

    private static String url_input;                    // (-u) starting url
    private static String path_input;                   // (-docs) output path
    private static String query_input;                  // (-q) query string
    private static int maxPages;                        // (-m) max number of pages to download
    private static boolean showTrace;                   // (-t) show trace

    public static final String DISALLOW = "Disallow:";
    public static final int MAXSIZE = 20000;            // Max size (# of bits) of a file that can be downloaded

    Vector<URL> newURLs;                                // URLs to be searched (and downloaded)
    Hashtable<URL,Integer> knownURLs;                   // Set of known URLs (already downloaded)


    // constructor
    public WebCrawler(String u, String docs, String q, int m, boolean t){
        this.url_input = u;
        this.path_input = docs;
        this.query_input = q;
        this.maxPages = m;
        this.showTrace = t;

        knownURLs = new Hashtable<URL,Integer>();
        newURLs = new Vector<URL>();
    }


    // Crawler Loop: Keep popping a url off newURLs, download it, and accumulate new URLs
    public void run() throws IOException {

        initialize();

        for (int i=0; i<maxPages; i++){

            URL url = newURLs.elementAt(0);
            newURLs.removeElementAt(0);

            if(showTrace){
                System.out.println("Searching " + url.toString());
            }

            if(isRobotSafe(url)){
                String page = downloadPage(url);
                // write output to file
                Writer writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path_input + "/" + "filename.txt"), "utf-8"));
                writer.write(page);

                if(showTrace){ System.out.println(page); }

                if(page.length() != 0){
                    processPage(url, page);
                }
                if(newURLs.isEmpty()){
                    break;
                }
            }

        }
        System.out.println("Search complete.");

    }


    // initialize: set the starting point of the crawler; set proxy and port settings
    private void initialize() {

        URL url;
        try{
            url = new URL(url_input);
        }catch (MalformedURLException e) {
            System.out.println("Invalid starting URL " + url_input);
            return;
        }

        knownURLs.put(url, new Integer(1));
        newURLs.addElement(url);

        System.out.println("Starting search: Initial URL " + url.toString());

        //Behind a firewall set your proxy and port here!
        Properties props= new Properties(System.getProperties());
        props.put("http.proxySet", "true");
        props.put("http.proxyHost", "webcache-cup");
        props.put("http.proxyPort", "8080");
        Properties newprops = new Properties(props);
        System.setProperties(newprops);

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

        if(showTrace){
            System.out.println("Checking robot protocol " + urlRobot.toString());
        }

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

        if(showTrace){
            System.out.println(strCommands);
        }

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



    // adds new URL to the queue. Accept only new URL's that end in htm or html
    // oldURL is the context, newURLString is the link (either an absolute or a relative URL)
    private void addNewURL(URL oldURL, String newUrlString){

        URL url;

        if (showTrace) System.out.println("URL String " + newUrlString);

        try { url = new URL(oldURL,newUrlString);
            if (!knownURLs.containsKey(url)) {
                String filename =  url.getFile();
                int iSuffix = filename.lastIndexOf("htm");
                if ((iSuffix == filename.length() - 3) ||
                        (iSuffix == filename.length() - 4)) {
                    knownURLs.put(url,new Integer(1));
                    newURLs.addElement(url);
                    System.out.println("Found new URL " + url.toString());
                } }
        }
        catch(MalformedURLException e){
            return;
        }

    }


    // Download contents of URL
    private String downloadPage(URL url){

        try{
            // try opening the URL
            URLConnection urlConnection = url.openConnection();
            System.out.println("Downloading " + url.toString());

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

            return content;

        }catch (IOException e){
            System.out.println("ERROR: couldn't open URL ");
            return "";
        }

    }



    // Go through page finding links to URLs.  A link is signalled
    // by <a href=" ...   It ends with a close angle bracket, preceded
    // by a close quote, possibly preceded by a hatch mark (marking a
    // fragment, an internal page marker)
    private void processPage(URL url, String page){

        String lcPage = page.toLowerCase(); // Page in lower case

        int index = 0; // position in page
        int iEndAngle, ihref, iURL, iCloseQuote, iHatchMark, iEnd;

        while ((index = lcPage.indexOf("<a",index)) != -1) {
            iEndAngle = lcPage.indexOf(">",index);
            ihref = lcPage.indexOf("href",index);
            if (ihref != -1) {
                iURL = lcPage.indexOf("\"", ihref) + 1;
                if ((iURL != -1) && (iEndAngle != -1) && (iURL < iEndAngle))
                { iCloseQuote = lcPage.indexOf("\"",iURL);
                    iHatchMark = lcPage.indexOf("#", iURL);
                    if ((iCloseQuote != -1) && (iCloseQuote < iEndAngle)) {
                        iEnd = iCloseQuote;
                        if ((iHatchMark != -1) && (iHatchMark < iCloseQuote))
                            iEnd = iHatchMark;
                        String newUrlString = page.substring(iURL,iEnd);
                        addNewURL(url, newUrlString);
                    } } }
            index = iEndAngle;
        }

    }





}















