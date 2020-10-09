package app.os.sql.drivers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.*;

public class MySQLDriver extends SQLDriver {
    private static final Logger logger = LoggerFactory.getLogger(MySQLDriver.class.getName());

    private Connection connection;
    private Statement statement;

    public MySQLDriver(final String DB_HOST, final String DB_NAME, final String USERNAME, final String PASSWORD, final String PORT) {
        super(DB_HOST, DB_NAME, USERNAME, PASSWORD, PORT);

        // connect: success -> work, error -> return
        logger.info("Trying connect to database...");

        long startConnecting = System.currentTimeMillis();
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = getRemoteConnection(DB_NAME, USERNAME, PASSWORD, DB_HOST, PORT);
        } catch (Exception throwables) {
            // some error -> return
            logger.error(String.format("Error! Can't connect to database. Error: %s", throwables.getMessage()));
            return;
        }

        // success message
        logger.info(String.format("Connected successfully in %dms", (System.currentTimeMillis() - startConnecting)));
    }

    private static Connection getRemoteConnection(String dbName,
                                                  String userName,
                                                  String password,
                                                  String hostname,
                                                  String port) {
        try {
            String jdbcUrl = "jdbc:mysql://" + hostname + ":" + port + "/" + dbName + "?user=" + userName + "&password=" + password;

            logger.trace("Getting remote connection with connection string from environment variables.");
            Connection con = DriverManager.getConnection(jdbcUrl);
            logger.info("Remote connection successful.");

            return con;
        } catch (SQLException e) {
            logger.warn(e.toString());
        }

        return null;
    }

    @Override
    public Map<String, List<Object>> getValuesFromTable(String tableName, String[] columns) {
        try {
            // create and execute query
            String sql = "SELECT * FROM " + tableName + ";";
            ResultSet queryResult;

            statement = null; // reset statement
            statement = connection.createStatement(); // get new statement
            queryResult = statement.executeQuery(sql); // execute

            // save result
            Map<String, List<Object>> receivedData = new HashMap<>(); // result map
            while (queryResult.next()) {
                for (String columnLabel : columns) {
                    // get data
                    List<Object> columnData = new ArrayList<>();
                    columnData.add(queryResult.getObject(columnLabel));

                    // put to result
                    if (receivedData.containsKey(columnLabel)) { // add to already created list
                        List<Object> current = receivedData.get(columnLabel);
                        current.addAll(columnData);

                        receivedData.put(columnLabel, current);
                    } else { // create new list
                        receivedData.put(columnLabel, columnData);
                    }
                }
            }

            return receivedData;
        } catch (Exception throwables) {
            logger.error("Error! Can't load data from database! Message: " + throwables.getMessage());
            return null;
        }
    }

    @Override
    public boolean insertDataToTable(String tableName, InsertData insertData) {
        String columnsRef = Arrays.toString(insertData.getColumns());
        columnsRef = columnsRef.substring(1);
        columnsRef = columnsRef.substring(0, columnsRef.length() - 1);

        StringBuilder valuesBuilder = new StringBuilder();
        for (String val : insertData.getData())
            valuesBuilder.append("'").append(val).append("'").append(",");

        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder
                .append("INSERT INTO ")
                .append(tableName)
                .append(" (")
                .append(columnsRef)
                .append(") VALUES (")
                .append(valuesBuilder.toString(), 0, valuesBuilder.toString().length() - 1)
                .append(");");


        try {
            statement = null;
            statement = connection.createStatement();
            statement.execute(sqlBuilder.toString());

            return true;
        } catch (Exception throwables) {
            logger.error(String.format("Exception! Message: %s.", throwables.getMessage()));
            return false;
        }
    }
}
