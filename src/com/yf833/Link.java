package com.yf833;
import java.net.URL;



public class Link {

    private URL url;
    private int score;

    public Link(URL url, int score){
        this.url = url;
        this.score = score;
    }
    public Link(URL url){
        this.url = url;
        this.score = 0;
    }



    public URL getURL(){
        return this.url;
    }

    public int getScore(){
        return this.score;
    }

    public void setScore(int newscore){
        this.score = newscore;
    }
}
