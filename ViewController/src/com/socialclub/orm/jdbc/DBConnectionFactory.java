package com.socialclub.orm.jdbc;

import com.socialclub.orm.session.Configuration;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class DBConnectionFactory {
    public DBConnectionFactory() {
        super();
    }

    protected static Connection conn = null;
    protected static Configuration conf = null;
    
    public static void setConfiguration(Configuration _conf){
        DBConnectionFactory.conf = _conf;
    }

    public static Connection getConnection() throws Exception {
        if (conn == null || (conn != null && conn.isClosed())) {
            try {
                if(null == conf){
                    throw new Exception("Configuration not set.");
                }
                // create a database connection
                String connStr = conf.getConnStr();
                Class.forName(conf.getDbDriverName());
                conn = DriverManager.getConnection(connStr);

            }catch (SQLException e) {
                System.err.println(e.getClass().getName() + ": " + e.getMessage() );
                e.printStackTrace();
            }catch (Exception e){
                System.err.println(e.getClass().getName() + ": " + e.getMessage() );
                e.printStackTrace();
            }
        }
        return conn;
    }
}

