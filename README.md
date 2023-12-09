# jpa-roki-vulovic-edition
JPA-RV-Edition is like normal JPA but without the bloat

Have you ever wanted to use JPA, but without the all the bloat it provides?
JPA Roki Vulovic Edition is much more slim and easier to use than JPA. This is a java 20 program.
<br> To use it, just specify all your database structure like below, and then use the EntityManager class to interact with the database.


## ~HOW TO USE~: does not worK :( just download
maven: add this as a repository in you pom.xml and import the dependency
Like this in your pom.xml:

```
<repositories>
    <repository>
      <id>java-net-repo</id>
      <url>https://github.com/serbe-hindert/jpa-roki-vulovic-edition/raw/maven2</url>
    </repository>
</repositories>
    
<dependencies>
    <dependency>
      <groupId>at.rokivulovic</groupId>
      <artifactId>jparv</artifactId>
      <version>1.0</version>
    </dependency>
</dependencies>
```

Then create the files 'jpa-rv.properties' and 'jpa-rv.queries' in your projects resource folder

#### jpa-rv.properties structure
```
#######################
# Database Properties #
#######################
# Define the Database-System: [POSTGRES, ORACLE]
db_system=POSTGRES
db_url=jdbc:postgresql://localhost:5432/rvtestdb
db_driver=org.postgresql.Driver
db_username=postgres
db_password=postgres
###################
# Java Properties #
###################
# set this to specify where the @Entitiy-annotated POJOs lie (starting performance increase), leave empty to ignore
db_pojo_package_name=at.rokivulovic.test
# Datatype(s) format used in the Program
datatype.java.lang.String='%s'
datatype.java.lang.Integer=%d
datatype.java.lang.Float=%f
datatype.java.lang.Double=%f
datatype.java.util.Date=TO_DATE(%s,'dd-mm-yyyy')
```

#### jpa-rv.queries structure
```
# Put your predefined queries here
# Example:
# query1=SELECT test_id, test_name FROM test WHERE test_id=:0 OR test_id=:1
```
