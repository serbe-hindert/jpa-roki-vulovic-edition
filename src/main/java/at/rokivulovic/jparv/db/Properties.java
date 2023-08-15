package at.rokivulovic.jparv.db;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.*;

public class Properties {
    private static final java.util.Properties PROPERTIES = new java.util.Properties();

    static {
        File propertyFile = Path.of("src", "main", "resources", "jpa-rv.properties").toFile();
        try {
            FileInputStream fileInputStream = new FileInputStream(propertyFile);
            PROPERTIES.load(fileInputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Map<String, String> loadPredefinedQueries() {
        File queryFile = Path.of("src", "main", "resources", "jpa-rv.queries").toFile();
        final java.util.Properties QUERIES = new java.util.Properties();
        FileInputStream fileInputStream;
        try {
            fileInputStream = new FileInputStream(queryFile);
            QUERIES.load(fileInputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Map<String, String> queries = new HashMap<>();

        for (Map.Entry<?, ?> e : QUERIES.entrySet()) {
            queries.put((String) e.getKey(), (String) e.getValue());
        }
        return queries;
    }

    public static String getProperty(String key) {
        return PROPERTIES.getProperty(key);
    }

    public static Map<String, String> getProperties(String key) {
        List<String> keys = new LinkedList<>();
        Map<String, String> properties = new HashMap<>();
        Iterator<?> i = PROPERTIES.propertyNames().asIterator();
        do {
            String next = (String) i.next();
            if (next.contains(key + ".")) {
                keys.add(next);
            }
        } while (i.hasNext());
        for (String k : keys) {
            properties.put(k.replace(key + ".", ""), PROPERTIES.getProperty(k));
        }
        return properties;
    }
}
