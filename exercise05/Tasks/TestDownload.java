// For week 5
// sestoft@itu.dk * 2014-09-19

import java.util.concurrent.Future;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;

public class TestDownload {

  private static final ExecutorService executor 
    = Executors.newWorkStealingPool();

  private static final String[] urls = 
  { "http://www.itu.dk", "http://www.di.ku.dk", "http://www.miele.de",
    "http://www.microsoft.com", "http://www.amazon.com", "http://www.dr.dk",
    "http://www.vg.no", "http://www.tv2.dk", "http://www.google.com",
    "http://www.ing.dk", "http://www.dtu.dk", "http://www.eb.dk", 
    "http://www.nytimes.com", "http://www.guardian.co.uk", "http://www.lemonde.fr",   
    "http://www.welt.de", "http://www.dn.se", "http://www.heise.de", "http://www.wsj.com", 
    "http://www.bbc.co.uk", "http://www.dsb.dk", "http://www.bmw.com", "https://www.cia.gov" 
  };


  public static void main(String[] args) throws IOException {
    //String url = "https://www.wikipedia.org/";
    //String page = getPage(url, 10);
    //System.out.printf("%-30s%n%s%n", url, page);


    /* 5.3.2
    Timer timer = new Timer();
    Map<String,String> map = getPages(urls, 200);
    for(Map.Entry<String, String> entry : map.entrySet()) {
        System.out.println(entry.getKey() + ": " + entry.getValue().length());
    }
    */
      sequentialExperiment(5);
      parallelExperiment(5);
  }

  // 5.3.3
  private static void sequentialExperiment(int reps){
    Timer timer = new Timer();
    for(int i = 0; i < reps; i++){
        try { getPages(urls, 200);} catch (Exception e){System.out.println(e);}
        System.out.printf("Sequential rep %d, time: %f\n", i, timer.check());
    }
  }

  // 5.3.4
  private static void parallelExperiment(int reps){
    Timer timer = new Timer();
    for(int i = 0; i < reps; i++){
        try { getPagesParallel(urls, 200);} catch (Exception e){}
        System.out.printf("Parallel rep %d, time: %f\n", i, timer.check());
    }
  }


  public static Map<String,String> getPages(String[] urls, int maxlines) throws IOException {
    Map<String,String> map = new HashMap<String,String>();
    for(String s : urls){
        try {
            map.put(s, getPage(s, maxlines));
        } catch(Exception e){}
    }
    return map;
  }

  public static Map<String,String> getPagesParallel(String[] urls, int maxlines) throws IOException {
    Map<String,String> map = new ConcurrentHashMap<String,String>();

    ArrayList<Future<?>> futures = new ArrayList<Future<?>>();
    for(String s : urls){
      futures.add(executor.submit(() -> { 
          try{
            map.put(s, getPage(s, maxlines));
        } catch(Exception e){}
      }));

    }
    try {
      for (Future<?> fut : futures)
        fut.get();
    } catch (Exception exn) { 
      System.out.println("Interrupted: " + exn);
    }

    return map;
  }

  public static String getPage(String url, int maxLines) throws IOException {
    // This will close the streams after use (JLS 8 para 14.20.3):
    try (BufferedReader in 
         = new BufferedReader(new InputStreamReader(new URL(url).openStream()))) {
      StringBuilder sb = new StringBuilder();
      for (int i=0; i<maxLines; i++) {
        String inputLine = in.readLine();
        if (inputLine == null)
          break;
        else
          sb.append(inputLine).append("\n");
      }
      return sb.toString();
    }
  }
}

