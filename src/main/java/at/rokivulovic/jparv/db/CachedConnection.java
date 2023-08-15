package at.rokivulovic.jparv.db;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;

public class CachedConnection {
    private LinkedList<Statement> statementQueue = new LinkedList<>();

    private Connection connection;

    public CachedConnection(Connection connection) {
        this.connection = connection;
    }

    public Statement getStatement() throws SQLException {
        if (connection == null) {
            throw new RuntimeException("Connection: not connected!");
        }
        if (statementQueue.isEmpty()) {
            return connection.createStatement();
        }
        return statementQueue.poll();
    }

    public void releaseStatement(Statement statement) {
        if (connection == null) {
            throw new RuntimeException("Connection: not connected!");
        }
        if (statement == null) {
            throw new RuntimeException("Statement: Statement is null!");
        }
        statementQueue.offer(statement);
    }
}
