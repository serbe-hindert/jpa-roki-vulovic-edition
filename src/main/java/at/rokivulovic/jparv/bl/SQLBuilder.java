package at.rokivulovic.jparv.bl;

import at.rokivulovic.jparv.db.Properties;
import at.rokivulovic.jparv.pojos.DBClassInformation;
import at.rokivulovic.jparv.annotations.general.Sequence;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class SQLBuilder {

    protected static final Map<Class<?>, String> DATATYPE_MAP;

    static {
        DATATYPE_MAP = new HashMap<>();
        Map<String, String> classes = Properties.getProperties("datatype");
        for (String k : classes.keySet()) {
            try {
                DATATYPE_MAP.put(Class.forName(k), classes.get(k));
            } catch (ClassNotFoundException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Method to deal with the data type in Inserts
     */
    protected static String dealWithType(Object o, Field f) {
        f.setAccessible(true);
        final Object content;

        try {
            content = f.get(o);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        return String.format(DATATYPE_MAP.get(f.getType()), f.getType().cast(content));
    }

    /**
     * Creates a sequence for an @Sequence annotated attribute
     */
    public static String createSequence(String attributeName, Integer start, Integer step) {
        return String.format("CREATE SEQUENCE IF NOT EXISTS %s_seq INCREMENT %d START WITH %d",
                attributeName,
                step,
                start
        );
    }

    /**
     * Builds a SELECT * SQL-String with the ClassInfo of a class
     */
    public static String buildSelectAllString(DBClassInformation info) {
        StringBuilder attributeBuilder = new StringBuilder();
        StringBuilder primaryKeyBuilder = new StringBuilder();

        attributeBuilder.append(info.getAttributes().get(0).getAttributeName());
        if (info.getAttributes().get(0).getPrimaryKey()) {
            primaryKeyBuilder.append(info.getAttributes().get(0).getAttributeName());
        }

        for (int i = 1; i < info.getAttributes().size(); i++) {
            attributeBuilder.append(",").append(info.getAttributes().get(i).getAttributeName());
            if (info.getAttributes().get(i).getPrimaryKey()) {
                primaryKeyBuilder.append(",").append(info.getAttributes().get(i).getAttributeName());
            }
        }

        return String.format("SELECT %s FROM %s ORDER BY %s",
                attributeBuilder,
                info.getTableName(),
                primaryKeyBuilder
        );
    }



    /**
     * Build an insert string, also acknowledging sequences
     */
    public static String buildInsertString(DBClassInformation info, Class<?> c, Object o) {
        StringBuilder keyBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();

        Field[] fields = c.getDeclaredFields();

        keyBuilder.append(info.getAttributes().get(0).getAttributeName());
        if (fields[0].isAnnotationPresent(Sequence.class)) {
            valueBuilder.append("nextval('").append(info.getAttributes().get(0).getAttributeName()).append("_seq')");
        } else {
            valueBuilder.append(dealWithType(o, fields[0]));
        }

        for (int i = 1; i < fields.length; i++) {
            keyBuilder.append(",");
            valueBuilder.append(",");
            keyBuilder.append(info.getAttributes().get(i).getAttributeName());
            if (fields[i].isAnnotationPresent(Sequence.class)) {
                valueBuilder.append("nextval('").append(info.getAttributes().get(i).getAttributeName()).append("_seq')");
            } else {
                valueBuilder.append(dealWithType(o, fields[i]));
            }
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                info.getTableName(),
                keyBuilder,
                valueBuilder
        );
    }

    /**
     * Build an insert string for a list, also acknowledging sequences
     */
    public static String buildInsertAllString(DBClassInformation info, Class<?> c, List list) {
        StringBuilder keyBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();

        Field[] fields = c.getDeclaredFields();

        keyBuilder.append(info.getAttributes().get(0).getAttributeName());
        valueBuilder.append("(");
        if (fields[0].isAnnotationPresent(Sequence.class)) {
            valueBuilder.append("nextval('").append(info.getAttributes().get(0).getAttributeName()).append("_seq')");
        } else {
            valueBuilder.append(dealWithType(list.get(0), fields[0]));
        }

        for (int i = 1; i < fields.length; i++) {
            keyBuilder.append(",");
            valueBuilder.append(",");
            keyBuilder.append(info.getAttributes().get(i).getAttributeName());
            if (fields[i].isAnnotationPresent(Sequence.class)) {
                valueBuilder.append("nextval('").append(info.getAttributes().get(i).getAttributeName()).append("_seq')");
            } else {
                valueBuilder.append(dealWithType(list.get(0), fields[i]));
            }
        }
        valueBuilder.append(")");

        for (int i = 1; i < list.size(); i++) {
            valueBuilder.append(",(");
            if (fields[0].isAnnotationPresent(Sequence.class)) {
                valueBuilder.append("nextval('").append(info.getAttributes().get(0).getAttributeName()).append("_seq')");
            } else {
                valueBuilder.append(dealWithType(list.get(i), fields[0]));
            }

            for (int j = 1; j < fields.length; j++) {
                valueBuilder.append(",");
                if (fields[j].isAnnotationPresent(Sequence.class)) {
                    valueBuilder.append("nextval('").append(info.getAttributes().get(j).getAttributeName()).append("_seq')");
                } else {
                    valueBuilder.append(dealWithType(list.get(i), fields[j]));
                }
            }

            valueBuilder.append(")");
        }

        return String.format("INSERT INTO %s (%s) VALUES %s",
                info.getTableName(),
                keyBuilder,
                valueBuilder
        );
    }

    /**
     * Build an insert string, completely disregarding sequences
     */
    public static String buildHardInsertString(DBClassInformation info, Class<?> c, Object o) {
        StringBuilder keyBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();

        Field[] fields = c.getDeclaredFields();

        keyBuilder.append(info.getAttributes().get(0).getAttributeName());
        valueBuilder.append(dealWithType(o, fields[0]));

        for (int i = 1; i < fields.length; i++) {
            keyBuilder.append(",");
            valueBuilder.append(",");

            keyBuilder.append(info.getAttributes().get(i).getAttributeName());
            valueBuilder.append(dealWithType(o, fields[i]));
        }

        return String.format("INSERT INTO %s (%s) VALUES (%s)",
                info.getTableName(),
                keyBuilder,
                valueBuilder
        );
    }

    public static String buildHardInsertAllString(DBClassInformation info, Class<?> c, List list) {
        StringBuilder keyBuilder = new StringBuilder();
        StringBuilder valueBuilder = new StringBuilder();

        Field[] fields = c.getDeclaredFields();

        valueBuilder.append("(");

        keyBuilder.append(info.getAttributes().get(0).getAttributeName());
        valueBuilder.append(dealWithType(list.get(0), fields[0]));

        for (int i = 1; i < fields.length; i++) {
            keyBuilder.append(",");
            valueBuilder.append(",");

            keyBuilder.append(info.getAttributes().get(i).getAttributeName());
            valueBuilder.append(dealWithType(list.get(0), fields[i]));
        }

        valueBuilder.append(")");

        for (int i = 1; i < list.size(); i++) {
            valueBuilder.append(",(").append(dealWithType(list.get(i), fields[0]));

            for (int j = 1; j < fields.length; j++) {
                keyBuilder.append(",");
                valueBuilder.append(",");

                keyBuilder.append(info.getAttributes().get(j).getAttributeName());
                valueBuilder.append(dealWithType(list.get(i), fields[j]));
            }

            valueBuilder.append(")");
        }



        return String.format("INSERT INTO %s (%s) VALUES %s",
                info.getTableName(),
                keyBuilder,
                valueBuilder
        );
    }

    public static String buildUpdateString(DBClassInformation info, Class<?> c, Object o) {
        StringBuilder setBuilder = new StringBuilder();
        StringBuilder primaryKeyBuilder = new StringBuilder();
        Field[] fields = c.getDeclaredFields();

        setBuilder.append(info.getAttributes().get(0).getAttributeName() + "=" + dealWithType(o, fields[0]));
        if (info.getAttributes().get(0).getPrimaryKey()) {
            primaryKeyBuilder.append(info.getAttributes().get(0).getAttributeName()).append("=").append(dealWithType(o, fields[0]));
        }

        for (int i = 1; i < fields.length; i++) {
            setBuilder.append(",");
            setBuilder.append(info.getAttributes().get(i).getAttributeName() + "=" + dealWithType(o, fields[i]));

            if (info.getAttributes().get(i).getPrimaryKey()) {
                primaryKeyBuilder.append(" AND ");
                primaryKeyBuilder.append(info.getAttributes().get(i).getAttributeName()).append("=").append(dealWithType(o, fields[i]));
            }
        }

        return String.format("UPDATE %s SET %s WHERE %s",
                info.getTableName(),
                setBuilder,
                primaryKeyBuilder
        );
    }

    public static String buildDeleteString(DBClassInformation info, Class<?> c, Object o) {
        StringBuilder primaryKeyBuilder = new StringBuilder();
        Field[] fields = c.getDeclaredFields();
        primaryKeyBuilder.append(info.getAttributes().get(0).getAttributeName()).append("=").append(dealWithType(o, fields[0]));
        for (int i = 1; i < fields.length; i++) {
            if (info.getAttributes().get(i).getPrimaryKey()) {
                primaryKeyBuilder.append(" AND ");
                primaryKeyBuilder.append(info.getAttributes().get(i).getAttributeName()).append("=").append(dealWithType(o, fields[i]));
            }
        }

        return String.format("DELETE FROM %s WHERE %s",
                info.getTableName(),
                primaryKeyBuilder
        );
    }

    public static String buildDeleteAllString(DBClassInformation info, Class<?> c, List list) {
        StringBuilder primaryKeyBuilder = new StringBuilder();
        Field[] fields = c.getDeclaredFields();
        primaryKeyBuilder.append("(").append(info.getAttributes().get(0).getAttributeName()).append("=").append(dealWithType(list.get(0), fields[0]));
        for (int i = 1; i < fields.length; i++) {
            if (info.getAttributes().get(i).getPrimaryKey()) {
                primaryKeyBuilder.append(" AND ");
                primaryKeyBuilder.append(info.getAttributes().get(i).getAttributeName()).append("=").append(dealWithType(list.get(0), fields[i]));
            }
        }
        primaryKeyBuilder.append(")");

        for (int i = 1; i < list.size(); i++) {
            primaryKeyBuilder.append(" OR (").append(info.getAttributes().get(0).getAttributeName()).append("=").append(dealWithType(list.get(i), fields[0]));
            for (int j = 1; j < fields.length; j++) {
                if (info.getAttributes().get(j).getPrimaryKey()) {
                    primaryKeyBuilder.append(" AND ");
                    primaryKeyBuilder.append(info.getAttributes().get(j).getAttributeName()).append("=").append(dealWithType(list.get(i), fields[j]));
                }
            }
            primaryKeyBuilder.append(")");
        }

        return String.format("DELETE FROM %s WHERE %s",
                info.getTableName(),
                primaryKeyBuilder
        );
    }
}
