package com.socialclub.orm.session;

public class Configuration {
    
    private String connStr;
    private String dbDriverName;
    private String databasePath;
    
    public Configuration() {
        super();
    }

    public Configuration setConnStr(String connStr) {
        this.connStr = connStr;
        return this;
    }

    public String getConnStr() {
        return connStr;
    }


    public Configuration setDbDriverName(String drivaer) {
        this.dbDriverName = drivaer;
        return this;
    }

    public String getDbDriverName() {
        return dbDriverName;
    }


    public Configuration setDatabasePath(String databasePath) {
        this.databasePath = databasePath;
        return this;
    }

    public String getDatabasePath() {
        return databasePath;
    }
}
