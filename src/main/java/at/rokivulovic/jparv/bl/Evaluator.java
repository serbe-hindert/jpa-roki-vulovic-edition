package at.rokivulovic.jparv.bl;

import at.rokivulovic.pojos.DBClassInformation;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class Evaluator {

    private Evaluator() {

    }

    public static Object evaluateSingle(Class c, DBClassInformation info, ResultSet set) throws SQLException {
        Object o;
        try {
            o = c.getConstructor().newInstance();
            if (set.next()) {
                Field[] fields = c.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    fields[i].set(o, set.getObject(info.getAttributes().get(i).getAttributeName()));
                }
            }
            o = c.cast(o);
        } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
            throw new RuntimeException("Error parsing DB-Entity to Object: " + e);
        }

        return o;
    }

    public static List evaluateAll(Class c, DBClassInformation info, ResultSet set) throws SQLException {
        List results = new ArrayList();
        while (set.next()) {
            try {
                Constructor constructor = c.getConstructor();
                constructor.setAccessible(true);

                Object o = constructor.newInstance();
                Field[] fields = c.getDeclaredFields();
                for (int i = 0; i < fields.length; i++) {
                    fields[i].setAccessible(true);
                    fields[i].set(o, set.getObject(info.getAttributes().get(i).getAttributeName()));
                }
                results.add(o);
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException e) {
                throw new RuntimeException("Error parsing DB-Entity to Object: " + e);
            }
        }
        return results;
    }
}
