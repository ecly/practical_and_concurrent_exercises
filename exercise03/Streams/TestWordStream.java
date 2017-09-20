// Week 3
// sestoft@itu.dk * 2015-09-09

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.IntSummaryStatistics;
import java.util.List;
import java.util.Map;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.IntStream;

public class TestWordStream {
    public static void main(String[] args) {
        String filename = "words";
        Stream<String> words = readWords(filename);

        //2
        //words.limit(100).forEach(x -> System.out.println(x));
        //3
        //words.filter(x -> x.length() == 22).forEach(x -> System.out.println(x));
        //4
        //System.out.println(words.filter(x -> x.length() == 22).findFirst().get());
        //5
        //words.filter(x -> isPalindrome(x)).forEach(x -> System.out.println(x));
        //6
        //words.parallel().filter(x -> isPalindrome(x)).forEach(x -> System.out.println(x));
        //7
        //System.out.println(words.mapToInt(x -> x.length()).summaryStatistics());
        //8
        //words.collect(Collectors.groupingBy(x -> x.length(), Collectors.counting())) 
        //    .forEach((v,k) -> System.out.printf("%d %d-letter words\n", k, v));
        //9
        //words.limit(100).forEach(x -> System.out.println(mapToString(letters(x))));
        //10
        //System.out.println(words.mapToInt(x -> letters(x).getOrDefault('e', 0)).sum());
        //11
        //words.collect(Collectors.groupingBy(x -> letters(x), Collectors.joining(",", "[", "]")))
            //.forEach((v,k) -> System.out.printf("%s=%s\n", mapToString(v), k));
        //12
        //words.parallel().collect(Collectors.groupingBy(x -> letters(x), Collectors.joining(",", "[", "]")))
            //.forEach((v,k) -> System.out.printf("%s=%s\n", mapToString(v), k));
        //13
        words.parallel().collect(Collectors.groupingByConcurrent(x -> letters(x), Collectors.joining(",", "[", "]")))
            .forEach((v,k) -> System.out.printf("%s=%s\n", mapToString(v), k));
    }

    public static Stream<String> readWords(String filename) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            //1
            return reader.lines();
        } catch (IOException exn) {
            return Stream.<String>empty();
        }
    }

    // Utility method for nicely formatting maps
    private static String mapToString(Map<Character, Integer> map){
        StringBuilder builder = new StringBuilder("{");
        Iterator<Map.Entry<Character, Integer>> it = map.entrySet().iterator();
        while(it.hasNext()) {
            Map.Entry<Character, Integer> entry = it.next();
            builder = builder.append(entry.getKey()).append("=").append(entry.getValue());
            if(it.hasNext()) builder = builder.append(", ");
        }
        return builder.append("}").toString();
    }

    //https://stackoverflow.com/questions/7569335/reverse-a-string-in-java
    public static boolean isPalindrome(String s) {
        return s.equals(new StringBuilder(s).reverse().toString());
    }

    public static Map<Character,Integer> letters(String s) {
        Map<Character,Integer> res = new TreeMap<>();
        for(Character c: s.toLowerCase().toCharArray())
            res.put(c, res.getOrDefault(c, 0) + 1);
        return res;
    }
}
