package at.rokivulovic.jparv.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {
    private Connection connection;
    private CachedConnection cachedConnection;

    private String dbUrl = Properties.getProperty("db_url");
    private String dbDriver = Properties.getProperty("db_driver");
    private String dbUsername = Properties.getProperty("db_username");
    private String dbPassword = Properties.getProperty("db_password");

    public Database() {
        try {
            Class.forName(dbDriver);
            connect();
            cachedConnection = new CachedConnection(connection);
        } catch (ClassNotFoundException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void connect() throws SQLException {
        disconnect();
        connection = DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
    }

    public void disconnect() throws SQLException {
        if (connection != null) {
            connection.close();
            connection = null;
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public Statement getStatement() throws SQLException {
        return cachedConnection.getStatement();
    }

    public void releaseStatement(Statement statement) {
        cachedConnection.releaseStatement(statement);
    }

}
