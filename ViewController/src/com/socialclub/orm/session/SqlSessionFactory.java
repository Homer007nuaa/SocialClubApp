package com.socialclub.orm.session;

import java.util.Properties;

public class SqlSessionFactory {      
    
    public SqlSessionFactory() {
        super();
    }
 
    public static SqlSession getDefaultSqlSession(Configuration conf){
        
        return new SQLiteSqlSession(conf);
    }
    
}
