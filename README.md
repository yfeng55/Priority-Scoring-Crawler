# Priority-Scoring Web Crawler
A web crawler that prioritizes downloading pages whose content is relevant to the provided query. Scoring works as follows:
* If any of the k words in q is a substring of the link text, then score = k*50.
* If any words in q is a substring of m's URL then score = 40.
* Else return 4*|U| + |V-U|
    * U is the set of words in q that occur in p within five words of m
    * V is the set of words in q that occur in p


### Input Flags:
    (-u) A URL from which to start the crawl (presumably, a web page that deals with the subject)
    (-q) A query: a set of words, possibly null
    (-docs) A path name for a directory to save the downloaded pages
    (-m) A maximum number of pages to download. This should default to 50
    (-t) A flag for generating a trace. This defaults to false


---


### Instructions for compiling and running:

#### 1. Compiling:
```sh
javac -cp "./jars/*" *.java
```

#### 2. Runing:
```sh
java -cp ".:./jars/*" Main -u [starting url] -docs [output path] -q [query] -m [max pages] -t
```

#### *Note: multiword queries must be put in quotations. For example:
```sh
-q “species whale whales”
```





