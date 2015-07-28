package com.socialclub.orm.session;

import java.io.Closeable;

import java.lang.reflect.Field;

import java.sql.Connection;

import java.util.List;
import java.util.Map;


//import org.apache.ibatis.executor.BatchResult;
//import org.apache.ibatis.session.Configuration;
//import org.apache.ibatis.session.ResultHandler;
//import org.apache.ibatis.session.RowBounds;

public interface SqlSession extends Closeable{
    
    boolean existTable(Class _class);

    boolean createSingleTable(Class _class,boolean isReplace);
    
    int createHierachyTables(Class _class,boolean isReplace);

    <T> T selectOne(Class _class, String where);

    <T> T select(String sql);
    
    <E> List<E> selectList(Class _class,String where);
    
    boolean populateForeignOne(Object o,String foreignFieldName); 
    
    boolean populateForeignList(Object o,String foreignFieldName);
    
    boolean insertOrUpdateSingleObject(Object childO,Object parentO);    
    
    int insertHierachyObject(Object o);

//    int insert(String statement, Object parameter);

    boolean updateSingleObject(Object o);

//    int update(String statement, Object parameter);

//    <E> List<E> selectList(String statement, Object parameter, RowBounds rowBounds);

//    <K, V> Map<K, V> selectMap(String statement, String mapKey);

//    <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey);

//    <K, V> Map<K, V> selectMap(String statement, Object parameter, String mapKey, RowBounds rowBounds);

//    void select(String statement, Object parameter, ResultHandler handler);

//    void select(String statement, ResultHandler handler);

//    void select(String statement, Object parameter, RowBounds rowBounds, ResultHandler handler);



    int deleteHierachyObject(Object o);

    int deleteOne(Object o);
    int deleteOne(Class _class,String where);

    void commit();

    void commit(boolean force);

    void rollback();

    void rollback(boolean force);

//    List<BatchResult> flushStatements();

    @Override
    void close();

//    void clearCache();
    
//    Configuration getConfiguration();

    <T> T getMapper(Class<T> type);

    Connection getConnection();
}
