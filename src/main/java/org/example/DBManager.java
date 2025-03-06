package org.example;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DBManager {
    private String url;
    private String user;
    private String password;

    public DBManager(String dbName, String user, String password) {
        this.url = "jdbc:postgresql://localhost:5432/" + dbName;
        this.user = user;
        this.password = password;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    public Connection getConnection(String dbName) throws SQLException {
        String dbUrl = "jdbc:postgresql://localhost:5432/" + dbName + "?options=-c%20search_path=public";
        return DriverManager.getConnection(dbUrl, user, password);
    }

    public void initProcedures() throws Exception {
        String dbName = getDatabaseName();
        String script = SQLScript.readSQLScript("/procedures.sql");
        SQLScript.executeSQLScript(dbName, script, user, password);
    }

    private String getDatabaseName() {
        return url.substring(url.lastIndexOf("/") + 1);
    }

    public void createDatabase(String newDbName) throws SQLException {
        try (Connection conn = getConnection("postgres");
             CallableStatement stmt = conn.prepareCall("CALL create_database(?)")) {
            stmt.setString(1, newDbName);
//            stmt.setString(2, password);
            stmt.execute();
        }
    }

    public void dropDatabase(String dbName) throws SQLException {
        try (Connection conn = getConnection("postgres");
             CallableStatement stmt = conn.prepareCall("CALL drop_database(?)")) {
            stmt.setString(1, dbName);
//            stmt.setString(2, password);
            stmt.execute();
        }
    }

    public void createTable(String tableName) throws SQLException {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("CALL create_table(?)")) {
            stmt.setString(1, tableName);
            stmt.execute();
        }
    }

    public void clearTable(String tableName) throws SQLException {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("CALL clear_table(?)")) {
            stmt.setString(1, tableName);
            stmt.execute();
        }
    }

    public void addString(String tableName, int customerId, String customerName, int goodId, String goodName, int price) throws SQLException {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("CALL add_data(?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, tableName);
            stmt.setInt(2, customerId);
            stmt.setString(3, customerName);
            stmt.setInt(4, goodId);
            stmt.setString(5, goodName);
            stmt.setInt(6, price);
            stmt.execute();
        }
    }

    public List<OnlineStore> searchGood(String tableName, String goodName) throws SQLException {
        List<OnlineStore> goods = new ArrayList<>();
        String sql = "SELECT * FROM search_by_good_name(?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            stmt.setString(2, goodName);
            try (ResultSet rs = stmt.executeQuery()) {
                while(rs.next()){
                    OnlineStore onlineStore = new OnlineStore(
                            rs.getInt("customerid"),
                            rs.getString("customerName"),
                            rs.getInt("goodId"),
                            rs.getString("goodName"),
                            rs.getInt("price")
                    );
                    goods.add(onlineStore);
                }
            }
        }
        return goods;
    }

    public void updateGood(String tableName, int customerId, String customerName, int goodId, String goodName, int price) throws SQLException {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("CALL update_data(?, ?, ?, ?, ?, ?)")) {
            stmt.setString(1, tableName);
            stmt.setInt(2, customerId);
            stmt.setString(3, customerName);
            stmt.setInt(4, goodId);
            stmt.setString(5, goodName);
            stmt.setInt(6, price);
            stmt.execute();
        }
    }

    public void deleteGood(String tableName, int goodId) throws SQLException {
        try (Connection conn = getConnection();
             CallableStatement stmt = conn.prepareCall("CALL delete_by_good_id(?, ?)")) {
            stmt.setString(1, tableName);
            stmt.setInt(2, goodId);
            stmt.execute();
        }
    }
}
