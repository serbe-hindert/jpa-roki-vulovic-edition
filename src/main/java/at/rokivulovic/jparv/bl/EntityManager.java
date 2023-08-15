package at.rokivulovic.jparv.bl;

import at.rokivulovic.jparv.annotations.general.Attribute;
import at.rokivulovic.jparv.annotations.general.Entity;
import at.rokivulovic.jparv.annotations.general.PrimaryKey;
import at.rokivulovic.jparv.annotations.general.Sequence;
import at.rokivulovic.jparv.bl.implementations.sqlbuilders.OracleSQLBuilder;
import at.rokivulovic.jparv.bl.implementations.sqlbuilders.PostgresSQLBuilder;
import at.rokivulovic.jparv.db.Database;
import at.rokivulovic.jparv.pojos.DBAttributeInformation;
import at.rokivulovic.jparv.pojos.DBClassInformation;
import at.rokivulovic.jparv.db.Properties;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class EntityManager {
    protected static final Database DATABASE;
    protected static final Map<Class<?>, DBClassInformation> DATABASE_TABLES;

    protected SQLBuilder BUILDER;

    protected static final Map<String, String> PREDEFINED_QUERIES = Properties.loadPredefinedQueries();


    /*
     * First a new DB is instanced
     * Then all DB Relations are stored in a map to increase performance
     */
    static {
        DATABASE = new Database();
        DATABASE_TABLES = new HashMap<>();
        String packageName = Properties.getProperty("db_pojo_package_name");
        List<Class<?>> classes;
        try {
            classes = getAllClassesInClasspath(packageName);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        classes.stream()
                .filter(c -> c.isAnnotationPresent(Entity.class))
                .forEach(c -> {
                    // assign class info
                    DBClassInformation info = new DBClassInformation();
                    info.setTableName(c.getAnnotation(Entity.class).table());
                    // assign list of attributes
                    List<DBAttributeInformation> attributes = new ArrayList<>();
                    Arrays.stream(c.getDeclaredFields())
                            .forEach(f -> {
                                DBAttributeInformation attribute = new DBAttributeInformation();
                                attribute.setAttributeName(f.getAnnotation(Attribute.class).name());
                                attribute.setPrimaryKey(f.isAnnotationPresent(PrimaryKey.class));
                                attribute.setSequence(f.isAnnotationPresent(Sequence.class));

                                // create sequence if necessary
                                if (attribute.getSequence()) {
                                    try {
                                        DATABASE.getStatement().execute(SQLBuilder.createSequence(
                                                attribute.getAttributeName(),
                                                f.getAnnotation(Sequence.class).start(),
                                                f.getAnnotation(Sequence.class).step()
                                        ));
                                    } catch (SQLException e) {
                                        throw new RuntimeException("Error creating sequence: " + e);
                                    }
                                }

                                attributes.add(attribute);
                            });

                    info.setAttributes(attributes);
                    DATABASE_TABLES.put(c, info);
                });
    }

    private static final EntityManager INSTANCE = new EntityManager();

    public static EntityManager getInstance() {
        return INSTANCE;
    }

    private EntityManager() {
        switch (Properties.getProperty("db_system")) {
            case "POSTGRES": BUILDER = new PostgresSQLBuilder(); break;
            case "ORACLE": BUILDER = new OracleSQLBuilder(); break;
            default: throw new RuntimeException("Database-System is not implemented!");
        }
    }

    /* Methods for loading/reflecting all classes */
    private static List<Class<?>> getAllClassesInClasspath(String packageName) throws ClassNotFoundException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        List<Class<?>> classes = new ArrayList<>();

        try {
            Enumeration<URL> resources = classLoader.getResources(packageName.replace('.', '/'));
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                File file = new File(resource.getFile());
                if (packageName.isEmpty()) {
                    classes.addAll(findClasses(file));
                } else {
                    classes.addAll(findClasses(file, packageName));
                }

            }
        } catch (IOException e) {
            throw new RuntimeException("Error loading resources from classpath:", e);
        }

        return classes;
    }
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = packageName + '.' + file.getName().replace('/', '.');
                Class<?> c = Class.forName(className.substring(0, className.length() - 6));
                classes.add(c);
            }
        }
        return classes;
    }
    private static List<Class<?>> findClasses(File directory) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        File[] files = directory.listFiles();
        if (files == null) {
            return classes;
        }

        for (File file : files) {
            if (file.isDirectory()) {
                classes.addAll(findClasses(file, file.getName()));
            } else if (file.getName().endsWith(".class")) {
                String className = file.getName().replace('/', '.').substring(1);
                System.out.println(className);
                Class<?> c = Class.forName(className.substring(0, className.length() - 6));
                classes.add(c);
            }
        }
        return classes;
    }

    protected ResultSet get(String query) throws SQLException {
        Statement statement = DATABASE.getStatement();
        ResultSet set = statement.executeQuery(query);
        DATABASE.releaseStatement(statement);
        return set;
    }

    protected void post(String query) throws SQLException {
        Statement statement = DATABASE.getStatement();
        statement.execute(query);
        DATABASE.releaseStatement(statement);
    }

    /* ALL REQUIRED METHODS */

    /**
     * Prepare a predefined query from the "jpa-rv.queries" file
     */
    public String preparePredefinedQuery(String name, String... parameters) {
        String query = PREDEFINED_QUERIES.get(name);
        for (int i = 0; i < parameters.length; i++) {
            query = query.replace(":" + i, parameters[i]);
        }
        return query;
    }
    /* SELECT */
    /**
     * Select a set of DB-Data-Entries into a List of Java Objects
     */
    public List select(Class<?> c, String query) throws SQLException {
        return Evaluator.evaluateAll(c, DATABASE_TABLES.get(c), get(query));
    }

    /**
     * Select a singular DB-Data-Entry into a Java Object
     */
    public Object selectSingle(Class<?> c, String query) throws SQLException {
        return Evaluator.evaluateSingle(c, DATABASE_TABLES.get(c), get(query));
    }

    /**
     * Select a set of DB-Data-Entries into a List of Java Objects with no WHERE clause
     */
    public List selectAll(Class<?> c) throws SQLException {
        return Evaluator.evaluateAll(c, DATABASE_TABLES.get(c), get(BUILDER.buildSelectAllString(DATABASE_TABLES.get(c))));
    }

    /* EXECUTE */
    /**
     * Execute a custom query
     */
    public void execute(String query) throws SQLException {
        post(query);
    }

    /* EXECUTE/INSERT */
    /**
     * Inserts a Java Object into the DB, also acknowledging sequences
     */
    public void insert(Object o) throws SQLException {
        Class<?> c = o.getClass();
        String sqlString = BUILDER.buildInsertString(DATABASE_TABLES.get(c), c, o);
        post(sqlString);
    }

    /**
     * Inserts a List of Java Objects into the DB, also acknowledging sequences
     */
    public void insertAll(List list) throws SQLException {
        Class<?> c = list.get(0).getClass();
        String sqlString = BUILDER.buildInsertAllString(DATABASE_TABLES.get(c), c, list);
        post(sqlString);
    }

    /**
     * Inserts a Java Object into the DB how it currently is, completely disregarding sequences
     */
    public void hardInsert(Object o) throws SQLException {
        Class<?> c = o.getClass();
        String sqlString = BUILDER.buildHardInsertString(DATABASE_TABLES.get(c), c, o);
        post(sqlString);
    }

    /**
     * Inserts a List of Java Objects into the DB how it currently is, completely disregarding sequences
     */
    public void hardInsertAll(List list) throws SQLException {
        Class<?> c = list.get(0).getClass();
        String sqlString = BUILDER.buildHardInsertAllString(DATABASE_TABLES.get(c), c, list);
        post(sqlString);
    }

    /* EXECUTE/UPDATE */
    /**
     * Update a DB-Entry with its Java Object pondon, using the primary key(s) for specification
     */
    public void update(Object o) throws SQLException {
        Class<?> c = o.getClass();
        String sqlString = BUILDER.buildUpdateString(DATABASE_TABLES.get(c), c, o);
        post(sqlString);
    }

    /*EXECUTE/DELETE*/
    /**
     * Delete a DB-Entry that is equal to a Java Object by primary key
     */
    public void delete(Object o) throws SQLException {
        Class<?> c = o.getClass();
        String sqlString = BUILDER.buildDeleteString(DATABASE_TABLES.get(c), c, o);
        post(sqlString);
    }

    /**
     * Delete List of DB-Entries that is equal to a List of Java Object by primary keys
     */
    public void deleteAll(List list) throws SQLException {
        Class<?> c = list.get(0).getClass();
        String sqlString = BUILDER.buildDeleteAllString(DATABASE_TABLES.get(c), c, list);
        post(sqlString);
    }
}
