
package com.student.registration.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {

    private static HikariDataSource dataSource;

    private DBConnection() {
    }

    private static synchronized void initializePool()
            throws SQLException {

        if (dataSource != null && !dataSource.isClosed()) {
            return;
        }

        Properties properties = new Properties();

        try (InputStream input = DBConnection.class
                .getClassLoader()
                .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new SQLException(
                        "db.properties file not found in resources.");
            }

            properties.load(input);

        } catch (IOException e) {
            throw new SQLException(
                    "Unable to load database properties.", e);
        }

        HikariConfig config = new HikariConfig();

        config.setJdbcUrl(properties.getProperty("db.url"));
        config.setUsername(properties.getProperty("db.username"));
        config.setPassword(properties.getProperty("db.password"));

        config.setMaximumPoolSize(10);
        config.setMinimumIdle(2);
        config.setPoolName("StudentRegistrationPool");

        try {
            dataSource = new HikariDataSource(config);
        } catch (RuntimeException e) {
            throw new SQLException(
                    "Unable to initialize the database connection pool.",
                    e);
        }
    }

    public static Connection getConnection() throws SQLException {
        initializePool();
        return dataSource.getConnection();
    }

    public static synchronized void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
        }
    }
}
