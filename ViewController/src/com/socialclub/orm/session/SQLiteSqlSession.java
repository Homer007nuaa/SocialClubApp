package com.socialclub.orm.session;

import com.socialclub.orm.annotation.DBTypeDef;
import com.socialclub.orm.annotation.ForeignColumn;
import com.socialclub.orm.annotation.Key;
import com.socialclub.orm.annotation.NonDBColumn;
import com.socialclub.orm.jdbc.DBConnectionFactory;

import com.socialclub.orm.main.Email;

import java.lang.reflect.Array;
import java.lang.reflect.Field;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import java.nio.Buffer;

import java.sql.Connection;

import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.Statement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;



public class SQLiteSqlSession implements SqlSession {
    
    private Configuration conf;
    private Connection conn;
    public SQLiteSqlSession(Configuration _conf) {
        super();
        this.conf = _conf;                                                           
        try{
            DBConnectionFactory.setConfiguration(conf);
            this.conn = DBConnectionFactory.getConnection();
        }catch(Exception e){
            System.err.println(e.getClass().getName() + ": " + e.getMessage() );
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean existTable(Class _class) {
        boolean returnValue = false;
        ResultSet rs = null;
        try {
            DatabaseMetaData meta = (DatabaseMetaData)conn.getMetaData();
            rs = meta.getTables(null, null, getTableName(_class) , null);
            if(rs.next()){
                returnValue = true;
            }else{
                returnValue = false;
            }
        } catch (SQLException e) {
            System.err.println(e);
            e.printStackTrace();
        }finally{
            try{
                rs.close();
            }catch(Exception e){
                System.err.println(e);
                e.printStackTrace();
            }
        }
        return returnValue;
    }
    
    @Override
    public int createHierachyTables(Class _class,boolean isReplace) {
        return createHierachyTables(_class,new String[0],isReplace);
    }

    private int createHierachyTables(Class _class,String[] foreignKeysDef,boolean isReplace){
        Statement stmt = null;
        String sql = null;
        String tableName = null;
        String[] tableColumnsDef = null;
        String[] keys = null;
        int returnValue = 0;
        
        if(this.existTable(_class)&&!isReplace){
            return 0;
        }

        tableName = getTableName(_class);
        Key tableKey= (Key)_class.getAnnotation(Key.class);
        if(null != tableKey){
            keys = tableKey.KeyColumns();            
        }        

        Field[] fields = _class.getDeclaredFields();
        if(null != fields){
            List cl = new ArrayList<String>();
            for(Field f : fields){
                NonDBColumn ndbc = f.getAnnotation(NonDBColumn.class);
                ForeignColumn fgc = f.getAnnotation(ForeignColumn.class);
                if(null == ndbc && null == fgc){
                    cl.add(getORTypeMapping(f));
                }else if(null != fgc){
                    String[] foreignKeys = fgc.foreignKey();
                    List childrenForeignKeysDefList = new ArrayList<String>();
                    for(String s : foreignKeys){
                        for(Field f2 : fields){
                            if(s.equals(f2.getName())){
                                childrenForeignKeysDefList.add("fk_"+getORTypeMapping(f2));
                                break;
                            }
                        }
                    }
                    
                    if(f.getType().isArray()){
                        returnValue += createHierachyTables(
                            f.getType().getComponentType(),
                            (String[])childrenForeignKeysDefList.toArray(new String[childrenForeignKeysDefList.size()]
                        ),isReplace);
                    }else{
                        returnValue += createHierachyTables(
                            f.getType(),
                            (String[])childrenForeignKeysDefList.toArray(new String[childrenForeignKeysDefList.size()]
                        ),isReplace);
                    }
                }
            }      
            if(0<foreignKeysDef.length){
                for(String s:foreignKeysDef){
                    cl.add(s);
                }
            }
            tableColumnsDef = (String[])cl.toArray(new String[cl.size()]);
        }else{
            tableColumnsDef = foreignKeysDef;
        }


        StringBuilder sqlbf = new StringBuilder();
        if(0<tableColumnsDef.length){
            sqlbf.append("CREATE TABLE ");        
            sqlbf.append(tableName.toLowerCase());   
            sqlbf.append("( ");
            sqlbf.append(tableColumnsDef[0]);
            for(int i = 1; i<tableColumnsDef.length;i++){
                sqlbf.append(", ");
                sqlbf.append(tableColumnsDef[i].toLowerCase());
            }
            if(null != keys){
                sqlbf.append(", PRIMARY KEY(");
                sqlbf.append(keys[0]);
                for(int i = 1; i<keys.length ; i++){
                    sqlbf.append(",");
                    sqlbf.append(keys[i].toLowerCase());
                }
                sqlbf.append(")");
            }
            sqlbf.append(");");
        }
        boolean existTable = this.existTable(_class);
        if(existTable && isReplace){
            dropTable(tableName.toLowerCase());
        }
        
        try{
            sql = sqlbf.toString();
            stmt = conn.createStatement();
            stmt.execute(sql);
            returnValue ++;
        }catch(SQLException e){
            System.err.println(e);
            e.printStackTrace();
        }finally{
            try{
                stmt.close();
            }catch(Exception e){
                System.err.println(e);    
                e.printStackTrace();          
            }
        }        
        return returnValue;
    }

    /**
     * @param _class
     * @return
     * @throws Exception
     */
    @Override
    public boolean createSingleTable(Class _class,boolean isReplace){
        Statement stmt = null;
        String sql = null;
        String tableName = null;
        String[] tableColumnsDef = null;
        String[] keys = null;
        boolean returnValue = false;
        
        if(this.existTable(_class)&&!isReplace){
            return false;
        }
            
        tableName = getTableName(_class);    
        Key tableKey= (Key)_class.getAnnotation(Key.class);
        if(null != tableKey){
            keys = tableKey.KeyColumns();            
        }        

        Field[] fields = _class.getDeclaredFields();
        if(null != fields){
            List cl = new ArrayList<String>();
            for(Field f : fields){
                NonDBColumn ndbc = f.getAnnotation(NonDBColumn.class);
                ForeignColumn fgc = f.getAnnotation(ForeignColumn.class);
                if(null == ndbc && null == fgc){
                    cl.add(getORTypeMapping(f));
                }
            }
            tableColumnsDef = (String[])cl.toArray(new String[cl.size()]);
        }else{
            tableColumnsDef = new String[0];
        }

        StringBuilder sqlbf = new StringBuilder();
        if(0<tableColumnsDef.length){
            sqlbf.append("CREATE TABLE ");        
            sqlbf.append(tableName.toLowerCase());   
            sqlbf.append("( ");
            sqlbf.append(tableColumnsDef[0]);
            for(int i = 1; i<tableColumnsDef.length;i++){
                sqlbf.append(", ");
                sqlbf.append(tableColumnsDef[i].toLowerCase());
            }
            if(null != keys){
                sqlbf.append(", PRIMARY KEY(");
                sqlbf.append(keys[0]);
                for(int i = 1; i<keys.length ; i++){
                    sqlbf.append(",");
                    sqlbf.append(keys[i].toLowerCase());
                }
                sqlbf.append(")");
            }
            sqlbf.append(");");
        }
        
        dropTable(tableName.toLowerCase());
        
        try{
            sql = sqlbf.toString();
            stmt = conn.createStatement();
            stmt.execute(sql);
            returnValue = true;
        }catch(SQLException e){
            System.err.println(e);
            e.printStackTrace();
        }finally{
            try{
                stmt.close();
            }catch(Exception e){
                System.err.println(e);  
                e.printStackTrace();      
            }
        }        
        return returnValue;
    }
    
    private String getORTypeMapping(Field field){
        StringBuilder sb = new StringBuilder();
        sb.append(field.getName());
        sb.append(" ");
        Class fieldType = field.getType();
        DBTypeDef dbtd = field.getAnnotation(DBTypeDef.class);
        if(null != dbtd){
            sb.append(dbtd.dbType());
            if(0 != dbtd.length()){
                sb.append("(");
                sb.append(dbtd.length());
                sb.append(")");
            }
        }else if(fieldType.equals(String.class)){
            sb.append("text");
        }else if(fieldType.equals(int.class) || fieldType.equals(boolean.class)|| fieldType.equals(Date.class) || fieldType.equals(long.class) || fieldType.equals(short.class)){
            sb.append("integer");
        }else if(fieldType.equals(float.class) || fieldType.equals(double.class)){
            sb.append("real");
        }
        return sb.toString();
    }
    
    private boolean dropTable(String tableName){
        String sql = "DROP TABLE IF EXISTS "+tableName+";";
        Statement stmt = null;
        boolean returnValue = false;
        try{
            stmt = conn.createStatement();
            stmt.execute(sql);
            returnValue = true;
        }catch(SQLException e){
            System.err.println(e);
            e.printStackTrace();
        }finally{
            try{
                stmt.close();
            }catch(Exception e){
                e.printStackTrace();                
            }
        }
        return returnValue;
    }



    @Override
    public <T> T selectOne(Class _class, String where){
        // TODO Implement this method
        T template = null;
        String sql = null;
        String tableName = getTableName(_class);  
        String[] tableColumns = null;
        String[] tableColumnTypes = null;
        String[] keys = null;
        StringBuilder sb = new StringBuilder();
        
        
        Field[] fields = _class.getDeclaredFields();
        if(null != fields){
            List cl = new ArrayList();
            List ct = new ArrayList();
            for(Field f: fields){
                NonDBColumn ndbc = f.getAnnotation(NonDBColumn.class);
                ForeignColumn fgc = f.getAnnotation(ForeignColumn.class);
                if(null == ndbc && null == fgc){
                    cl.add(f.getName());
                    if(f.getType().equals(String.class)){
                        ct.add("String");
                    }else if(f.getType().equals(int.class)){
                        ct.add("int");
                    }else if(f.getType().equals(Date.class)){
                        ct.add("Date");
                    }else if(f.getType().equals(boolean.class)){
                        ct.add("boolean");
                    }else if(f.getType().equals(double.class)){
                        ct.add("double");
                    }else if(f.getType().equals(long.class)){
                        ct.add("long");
                    }else if(f.getType().equals(short.class)){
                        ct.add("short");
                    }else if(f.getType().equals(float.class)){
                        ct.add("float");                        
                    }
                }                
            }
            tableColumns = (String[])cl.toArray(new String[cl.size()]);
            tableColumnTypes = (String[])ct.toArray(new String[ct.size()]);
        }else{
            tableColumns = new String[0];
            tableColumnTypes = new String[0];
        }
        
        if(0<tableColumns.length){
            sb.append("SELECT ");
            sb.append(tableColumns[0]);
            for(int i = 1; i < tableColumns.length;i++){
                sb.append(",");
                sb.append(tableColumns[i]);                
            }
            sb.append(" FROM ");
            sb.append(tableName);
            if(null != where && !"".equals(where)){
                sb.append(" WHERE ");
                sb.append(where);
            }
            sb.append(";");
        }
        sql = sb.toString();
        template = DBToSingleTemplate(sql,_class,tableColumns,tableColumnTypes);
        return template;
    }
    
    @Override
    public <T> T select(String sql){
        T template = null;
        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            if(rs.next()){
                template = (T)rs.getObject(1);
            }
        }catch(Exception e){
            e.printStackTrace();
        }
        return template;
    }
    
    @Override
    public <E> List<E> selectList(Class _class,String where){
        // TODO Implement this method
        List<E> l = null;
        String sql = null;
        String tableName = getTableName(_class);  
        String[] tableColumns = null;
        String[] tableColumnTypes = null;
        String[] keys = null;
        StringBuilder sb = new StringBuilder();
        
        Field[] fields = _class.getDeclaredFields();
        if(null != fields){
            List cl = new ArrayList();
            List ct = new ArrayList();
            for(Field f: fields){
                NonDBColumn ndbc = f.getAnnotation(NonDBColumn.class);
                ForeignColumn fgc = f.getAnnotation(ForeignColumn.class);
                if(null == ndbc && null == fgc){
                    cl.add(f.getName());
                    if(f.getType().equals(String.class)){
                        ct.add("String");
                    }else if(f.getType().equals(int.class)){
                        ct.add("int");
                    }else if(f.getType().equals(Date.class)){
                        ct.add("Date");
                    }else if(f.getType().equals(boolean.class)){
                        ct.add("boolean");
                    }else if(f.getType().equals(double.class)){
                        ct.add("double");
                    }else if(f.getType().equals(long.class)){
                        ct.add("long");
                    }else if(f.getType().equals(short.class)){
                        ct.add("short");
                    }else if(f.getType().equals(float.class)){
                        ct.add("float");                        
                    }
                }                
            }
            tableColumns = (String[])cl.toArray(new String[cl.size()]);
            tableColumnTypes = (String[])ct.toArray(new String[ct.size()]);
        }else{
            tableColumns = new String[0];
            tableColumnTypes = new String[0];
        }
        
        if(0<tableColumns.length){
            sb.append("SELECT ");
            sb.append(tableColumns[0]);
            for(int i = 1; i < tableColumns.length;i++){
                sb.append(",");
                sb.append(tableColumns[i]);                
            }
            sb.append(" FROM ");
            sb.append(tableName);
            if(null != where && !"".equals(where)){
                sb.append(" WHERE ");
                sb.append(where);
            }
            sb.append(";");
        }
        sql = sb.toString();
        l = DBToTemplateList(sql,_class,tableColumns,tableColumnTypes);
        return l;
    }
    @Override
    public boolean populateForeignOne(Object o,String foreignFieldName){
        boolean returnValue = false;
        try{
            returnValue =  populateForeignOne(o,o.getClass(),o.getClass().getField(foreignFieldName));
        }catch(Exception e){
                e.printStackTrace();
        }
        return returnValue;
    }
    
    private boolean populateForeignOne(Object o,Class _parentClass,Field _childClass){
        boolean returnValue = false;
        String where =  foreignKeyWhere(o,_parentClass,_childClass);
        try{
        Method m = _parentClass.getDeclaredMethod("set" + convertToUppercase(_childClass.getName()), _childClass.getType());
        Object childO = selectOne(_childClass.getType(),where);
        m.invoke(o, childO);
        returnValue=true;    
        }catch(Exception e){
            e.printStackTrace();
        }        
        return returnValue;
    }
    
    
    @Override
    public boolean populateForeignList(Object o,String foreignFieldName){
        boolean returnValue = false;
        try{
            returnValue = populateForeignList(o,o.getClass(),o.getClass().getField(foreignFieldName));
        }catch(Exception e){
                e.printStackTrace();
        }
        return returnValue;
    }
    
    private boolean populateForeignList(Object o,Class _parentClass,Field _childField){
        boolean returnValue = false;
        String where =  foreignKeyWhere(o,_parentClass,_childField);
        try{
            Method m = _parentClass.getDeclaredMethod("set" + convertToUppercase(_childField.getName()), _childField.getType());
            List children = selectList(_childField.getType().getComponentType(),where);
            Object array = Array.newInstance(_childField.getType().getComponentType(), children.size());
            //Object[] os = (Object[])children.toArray(new Object[children.size()]);
            for(int i = 0 ; i < children.size();i++){
                Array.set(array, i, children.get(i));
            }
            if(0 != children.size()){
                m.invoke(o, array);
                returnValue=true; 
            }      
        }catch(Exception e){
            e.printStackTrace();
        }
        return returnValue;
    }
    

    @Override
    public boolean insertOrUpdateSingleObject(Object childO,Object parentO){
        
        Field[] cFields = childO.getClass().getDeclaredFields();
        Field[] cTableColumns = null;
        Field[] cTableFColumns = null;
        StringBuilder sb = new StringBuilder();
        PreparedStatement pstmt = null;
        boolean returnValue = false;
        
        List cTableColumnL = new ArrayList();
        List cTableFColumnL = new ArrayList();
        
        for(Field f: cFields){
            NonDBColumn cndbc = f.getAnnotation(NonDBColumn.class);
            ForeignColumn cfgc = f.getAnnotation(ForeignColumn.class);
            if(null == cndbc && cfgc == null){
                cTableColumnL.add(f);
            }else if(cfgc != null){
                cTableFColumnL.add(f);
            }
        }
        Field foreignFiled = null;
        Field[] pfields = parentO.getClass().getDeclaredFields();
        for(Field pf : pfields){
            if(pf.getType().equals(childO.getClass())){
                foreignFiled = pf;
                break;
            }
        }

        ForeignColumn pfgc = null;
        if(null!=foreignFiled){
         pfgc = foreignFiled.getAnnotation(ForeignColumn.class);
        }
        
        String[] pForeignKeys = null;
        if(null != pfgc){
            pForeignKeys = pfgc.foreignKey();
        }else{
            pForeignKeys = new String[0];
        }
        
        cTableColumns = (Field[])cTableColumnL.toArray(new Field[cTableColumnL.size()]);
        try{
            if(0!= cTableColumns.length){
                sb.append("REPLACE INTO ");
                sb.append(getTableName(childO.getClass()));
                sb.append("(");
                sb.append(cTableColumns[0].getName());
                for(int i = 1;i< cTableColumns.length;i++){
                    sb.append(",");
                    sb.append(cTableColumns[i].getName());
                }
                for(String s : pForeignKeys){
                    sb.append(",fk_");
                    sb.append(s);
                }
                sb.append(") VALUES(?");
                for(int i = 1;i< cTableColumns.length;i++){
                    sb.append(",?");
                }
                for(String s : pForeignKeys){
                    sb.append(",?");                    
                }
                sb.append(");");
                pstmt = conn.prepareStatement(sb.toString());
                int i = 0;
                for(;i<cTableColumns.length;i++){
                    Method m = childO.getClass().getDeclaredMethod("get"+convertToUppercase(cTableColumns[i].getName()), null);
                    Object v = (Object)m.invoke(childO, new Object[0]);
                    if(v.getClass().equals(String.class)){
                        pstmt.setString(i+1,(String) v);
                    }else if(v.getClass().equals(int.class)){
                        pstmt.setLong(i+1, (Long)v);
                    }else if(v.getClass().equals(Date.class)){
                        Date d = (Date)v; 
                        pstmt.setLong(i+1, d.getTime());
                    }else if(v.getClass().equals(long.class)){
                        pstmt.setLong(i+1, (Long)v);
                    }else if(v.getClass().equals(short.class)){
                        pstmt.setLong(i+1, (Long)v);
                    }else if(v.getClass().equals(double.class)){
                        pstmt.setDouble(i+1, (Double)v);
                    }else if(v.getClass().equals(float.class)){
                        pstmt.setDouble(i+1, (Double)v);
                    }
                }
                for(int j = 0;j<pForeignKeys.length;j++){
                    Method m = parentO.getClass().getDeclaredMethod("get"+convertToUppercase(pForeignKeys[j]), null);
                    Object v = (Object)m.invoke(parentO, new Object[0]);
                    if(v.getClass().equals(String.class)){
                        pstmt.setString(i+j+1, (String)v);
                    }else if(v.getClass().equals(int.class)){
                        pstmt.setLong(i+j+1, (Long)v);
                    }else if(v.getClass().equals(Date.class)){
                        Date d = (Date)v; 
                        pstmt.setLong(i+j+1,d.getTime());
                    }else if(v.getClass().equals(long.class)){
                        pstmt.setLong(i+j+1, (Long)v);
                    }else if(v.getClass().equals(short.class)){
                        pstmt.setLong(i+j+1, (Long)v);
                    }else if(v.getClass().equals(double.class)){
                        pstmt.setDouble(i+j+1, (Double)v);
                    }else if(v.getClass().equals(float.class)){
                        pstmt.setDouble(i+j+1, (Double)v);
                    }
                }
                int r = pstmt.executeUpdate();
                returnValue = r==0?false:true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                pstmt.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return returnValue;   
    }
    
    @Override
    public int insertHierachyObject(Object o){        
        return insertHierachyObject(o,null,null);
    }
    
    private int insertHierachyObject(Object childO,Field foreignFiled,Object parentO){
        
        Field[] cFields = childO.getClass().getDeclaredFields();
        Field[] cTableColumns = null;
        Field[] cTableFColumns = null;
        StringBuilder sb = new StringBuilder();
        PreparedStatement pstmt = null;
        int returnValue = 0;
        
        List cTableColumnL = new ArrayList();
        List cTableFColumnL = new ArrayList();
        
        for(Field f: cFields){
            NonDBColumn cndbc = f.getAnnotation(NonDBColumn.class);
            ForeignColumn cfgc = f.getAnnotation(ForeignColumn.class);
            if(null == cndbc && cfgc == null){
                cTableColumnL.add(f);
            }else if(cfgc != null){
                cTableFColumnL.add(f);
            }
        }
        
        ForeignColumn pfgc = null;
        
        if(null!=foreignFiled){
         pfgc = foreignFiled.getAnnotation(ForeignColumn.class);
        }
        
        String[] pForeignKeys = null;
        if(null != pfgc){
            pForeignKeys = pfgc.foreignKey();
        }else{
            pForeignKeys = new String[0];
        }
        
        cTableColumns = (Field[])cTableColumnL.toArray(new Field[cTableColumnL.size()]);
        try{
            if(0!= cTableColumns.length){
                sb.append("REPLACE INTO ");
                sb.append(getTableName(childO.getClass()));
                sb.append("(");
                sb.append(cTableColumns[0].getName());
                for(int i = 1;i< cTableColumns.length;i++){
                    sb.append(",");
                    sb.append(cTableColumns[i].getName());
                }
                for(String s : pForeignKeys){
                    sb.append(",fk_");
                    sb.append(s);
                }
                sb.append(") VALUES(?");
                for(int i = 1;i< cTableColumns.length;i++){
                    sb.append(",?");
                }
                for(String s : pForeignKeys){
                    sb.append(",?");                    
                }
                sb.append(");");
                pstmt = conn.prepareStatement(sb.toString());
                int i = 0;
                for(;i<cTableColumns.length;i++){
                    Method m = childO.getClass().getDeclaredMethod("get"+convertToUppercase(cTableColumns[i].getName()), null);
                    Object v = (Object)m.invoke(childO, new Object[0]);
                    if(v.getClass().equals(String.class)){
                        pstmt.setString(i+1,(String) v);
                    }else if(v.getClass().equals(Integer.class)){
                        pstmt.setInt(i+1, (Integer)v);
                    }else if(v.getClass().equals(Date.class)){
                        Date d = (Date)v; 
                        pstmt.setLong(i+1, d.getTime());
                    }else if(v.getClass().equals(Long.class)){
                        pstmt.setLong(i+1, (Long)v);
                    }else if(v.getClass().equals(Short.class)){
                        pstmt.setShort(i+1, (Short)v);
                    }else if(v.getClass().equals(Double.class)){
                        pstmt.setDouble(i+1, (Double)v);
                    }else if(v.getClass().equals(Float.class)){
                        pstmt.setFloat(i+1, (Float)v);
                    }
                }
                for(int j = 0;j<pForeignKeys.length;j++){
                    Method m = parentO.getClass().getDeclaredMethod("get"+convertToUppercase(pForeignKeys[j]), null);
                    Object v = (Object)m.invoke(parentO, new Object[0]);
                    if(v.getClass().equals(String.class)){
                        pstmt.setString(i+j+1, (String)v);
                    }else if(v.getClass().equals(Integer.class)){
                        pstmt.setInt(i+j+1, (Integer)v);
                    }else if(v.getClass().equals(Date.class)){
                        Date d = (Date)v; 
                        pstmt.setLong(i+j+1,d.getTime());
                    }else if(v.getClass().equals(Long.class)){
                        pstmt.setLong(i+j+1, (Long)v);
                    }else if(v.getClass().equals(Short.class)){
                        pstmt.setShort(i+j+1, (Short)v);
                    }else if(v.getClass().equals(Double.class)){
                        pstmt.setDouble(i+j+1, (Double)v);
                    }else if(v.getClass().equals(Float.class)){
                        pstmt.setFloat(i+j+1, (Float)v);
                    }
                }
                returnValue += pstmt.executeUpdate();
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                pstmt.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        try{    
            Iterator cForeignI = cTableFColumnL.iterator();
            while(cForeignI.hasNext()){
                Field cff = (Field)cForeignI.next();
                ForeignColumn cfgc = cff.getAnnotation(ForeignColumn.class);
                if(null == cfgc){
                    continue;
                }
                if(cff.getType().isArray()){
                    Method m = childO.getClass().getDeclaredMethod("get"+convertToUppercase(cff.getName()), null);
                    Object[] os = (Object[])m.invoke(childO, new Object[0]);
                    if(null != os){
                        for(Object o1 : os){
                            returnValue+=insertHierachyObject(o1,cff,childO);
                        }
                    }
                }else{
                    Method m = childO.getClass().getDeclaredMethod("get"+convertToUppercase(cff.getName()), null);
                    Object o2 = m.invoke(childO, new Object[0]);
                    if(null != o2){
                        returnValue+=insertHierachyObject(o2,cff,childO);
                    }
                };
            }
        }catch(NoSuchMethodException e){
            e.printStackTrace();
        }catch(IllegalAccessException e){
            e.printStackTrace();
        }catch(InvocationTargetException e){
            e.printStackTrace();
        }
        return returnValue;   
    }

    @Override
    public boolean updateSingleObject(Object o){
        // TODO Implement this method
        
        Field[] cFields = o.getClass().getDeclaredFields();
        Field[] cTableColumns = null;
        Field[] keyColumns = null;
        String[] keys = null;
        String sql = null;
        Statement stmt = null;
        boolean returnValue = false;
        
        try{
            
            Key pk = o.getClass().getAnnotation(Key.class);
            if(pk != null){
                keys = pk.KeyColumns();
            }else{
                throw new Exception("please define the key for the Object.");
            }
            
            List cTableColumnL = new ArrayList();        
            List cKeyColumnL = new ArrayList();
            
            for(Field f: cFields){
                NonDBColumn cndbc = f.getAnnotation(NonDBColumn.class);
                ForeignColumn cfgc = f.getAnnotation(ForeignColumn.class);
                if(null == cndbc && cfgc == null){
                    boolean flag = true;
                    for(String s : keys){
                        if(f.getName().equals(s)){
                            cKeyColumnL.add(f);
                            flag = false;
                        }
                    }
                    if(flag){
                        cTableColumnL.add(f);
                    }
                }
            }
            cTableColumns = (Field[])cTableColumnL.toArray(new Field[cTableColumnL.size()]);
            keyColumns = (Field[])cKeyColumnL.toArray(new Field[cKeyColumnL.size()]);
            
            if(null != cTableColumns && null != keyColumns){
                StringBuilder sb = new StringBuilder();
                sb.append("UPDATE ");
                sb.append(getTableName(o.getClass()));
                sb.append(" set ");
                Field f1 = cTableColumns[0];
                Method m = o.getClass().getDeclaredMethod("get"+convertToUppercase(f1.getName()),null);
                Object v = m.invoke(o, new Object[0]);
                sb.append(f1.getName());
                sb.append("='");
                sb.append(String.valueOf(v));
                sb.append("'");
                
                for(int i = 1 ; i < cTableColumns.length; i ++){
                    Method m2 = o.getClass().getDeclaredMethod("get"+convertToUppercase(cTableColumns[i].getName()), null);
                    Object v2 = m2.invoke(o, new Object[0]);
                    if(null != v2){
                        sb.append(",");
                        sb.append(cTableColumns[i].getName());
                        sb.append("='");
                        sb.append(String.valueOf(v2));
                        sb.append("'");
                    }
                }
                
                sb.append(" where ");
    
                Field f3 = keyColumns[0];
                Method m3 = o.getClass().getDeclaredMethod("get"+convertToUppercase(f3.getName()), null);
                Object v3 = m3.invoke(o, new Object[0]);
                sb.append(f3.getName());
                sb.append("='");
                sb.append(String.valueOf(v3));
                sb.append("'");
                
                for(int i = 1 ; i < keyColumns.length; i ++){
                    Method m4 = o.getClass().getDeclaredMethod("get"+convertToUppercase(keyColumns[i].getName()), null);
                    Object v4 = m4.invoke(o, new Object[0]);
                    sb.append(" AND ");
                    sb.append(keyColumns[i].getName());
                    sb.append("='");
                    sb.append(String.valueOf(v4));
                    sb.append("'");
                }
                sql = sb.toString();
            }
            stmt = conn.createStatement();
            int i = stmt.executeUpdate(sql);
            if(1==i){
                returnValue = true;
            }
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                stmt.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return returnValue;
    }

    @Override
    public int deleteHierachyObject(Object o) {
        int returnValue = 0;
        try{
            Key keyA = o.getClass().getAnnotation(Key.class);
            if(null == keyA){
                throw new Exception("please define the primary key to the Object");
            }
            String[] keys = keyA.KeyColumns();
            StringBuilder sb = new StringBuilder();
            if(0<keys.length){
                Method m =  o.getClass().getDeclaredMethod("get"+convertToUppercase(keys[0]),null);
                Object v = m.invoke(o, null);
                if(null != v){
                    sb.append(keys[0]);
                    sb.append("='");
                    sb.append(v.toString());
                    sb.append("'");
                }
                for(int i = 1; i < keys.length; i++){
                    Method m2 =  o.getClass().getDeclaredMethod("get"+convertToUppercase(keys[i]),null);
                    Object v2 = m2.invoke(o, null);
                    sb.append(" AND ");
                    sb.append(keys[i]);
                    sb.append("='");
                    sb.append(v2.toString());
                    sb.append("'");
                }
            }
            returnValue += deleteHierachyObject(o,sb.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
        return returnValue;
    }
    
    private int deleteHierachyObject(Object o,String where){
        int returnValue = 0;
        
        try{
            Field[] fields = o.getClass().getDeclaredFields();
            List foreignKeysL = new ArrayList();
            List foreignFieldL = new ArrayList();
            Object[] keysSet = null;
            Field[] foreignFields = null;
            for(Field f:fields){
                ForeignColumn fgc = f.getAnnotation(ForeignColumn.class);
                if(null != fgc){
                    foreignKeysL.add(fgc.foreignKey());
                    foreignFieldL.add(f);
                }
            }
            
            if(0 < foreignFieldL.size()){
                keysSet  = (Object[])foreignKeysL.toArray(new Object[foreignKeysL.size()]);
                foreignFields  = (Field[])foreignFieldL.toArray(new Field[foreignFieldL.size()]);
                for(int i = 0 ; i < foreignFields.length ; i++){
                    String[] keys = (String[])keysSet[i];
                    Field[] fs = o.getClass().getDeclaredFields();
                    StringBuilder sb2 = new StringBuilder(); 
                    
                    for(Field f : fs){
                        if(f.getName().equals(keys[0])){
                            Method m =  o.getClass().getDeclaredMethod("get"+convertToUppercase(f.getName()),null);
                            Object v = m.invoke(o, null);
                            sb2.append("fk_");
                            sb2.append(f.getName());
                            sb2.append("='");
                            sb2.append(v.toString());
                            sb2.append("'");
                            break;
                        }
                    }
                    
                    for(int j = 1; j < keys.length;j++){
                        for(Field f : fs){
                            if(f.getName().equals(keys[j])){
                                Method m =  o.getClass().getDeclaredMethod("get"+convertToUppercase(f.getName()),null);
                                Object v = m.invoke(o, null);
                                sb2.append(" AND fk_");
                                sb2.append(f.getName());
                                sb2.append("='");
                                sb2.append(v.toString());
                                sb2.append("'");
                                break;
                            }
                        }
                    }
                    

                    Method m =  o.getClass().getDeclaredMethod("get"+convertToUppercase(foreignFields[i].getName()),null);
                    Object v = m.invoke(o, null);
                    if(null!= v ){
                        if(v.getClass().isArray()){
                            Object[] os = (Object[])v;
                            for(Object v2: os){
                                returnValue += deleteHierachyObject(v2,sb2.toString());
                            }
                            
                        }else{
                            returnValue += deleteHierachyObject(v,sb2.toString());
                        }
                    }
                    
                }
            }
            
        }catch(Exception e){
            e.printStackTrace();
        }
        returnValue += deleteOne(o.getClass(),where);
        return returnValue;
    }
    
    @Override
    public int deleteOne(Class _class,String where){
        int returnValue = 0;
        Statement stmt = null;
        String tableName = getTableName(_class);
        StringBuilder sb = new StringBuilder();
        sb.append("DELETE FROM ");
        sb.append(tableName);
        sb.append(" where ");
        sb.append(where);
        try{
            stmt = conn.createStatement();
            returnValue = stmt.executeUpdate(sb.toString());
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                stmt.close();
            }catch(Exception e){
                e.printStackTrace();
            }
        }
        return 0;
    }

    @Override
    public int deleteOne(Object o) {
        int returnValue = 0;
        
        try{
            Key keyA = o.getClass().getAnnotation(Key.class);
            if(null == keyA){
                throw new Exception("please define the primary key to the Object");
            }
            String[] keys = keyA.KeyColumns();
            StringBuilder sb = new StringBuilder();
            if(0<keys.length){
                Method m =  o.getClass().getDeclaredMethod("get"+convertToUppercase(keys[0]),null);
                Object v = m.invoke(o, null);
                if(null != v){
                    sb.append(keys[0]);
                    sb.append("='");
                    sb.append(v.toString());
                    sb.append("'");
                }
                for(int i = 1; i < keys.length; i++){
                    Method m2 =  o.getClass().getDeclaredMethod("get"+convertToUppercase(keys[i]),null);
                    Object v2 = m2.invoke(o, null);
                    sb.append(" AND ");
                    sb.append(keys[i]);
                    sb.append("='");
                    sb.append(v2.toString());
                    sb.append("'");
                }
            }
            returnValue = deleteOne(o.getClass(),sb.toString());
        }catch(Exception e){
            e.printStackTrace();
        }
        return returnValue;
    }

    @Override
    public void commit() {
        // TODO Implement this method
    }

    @Override
    public void commit(boolean force) {
        // TODO Implement this method
    }

    @Override
    public void rollback() {
        // TODO Implement this method
    }

    @Override
    public void rollback(boolean force) {
        // TODO Implement this method
    }

//    @Override
//    public List<BatchResult> flushStatements() {
//        // TODO Implement this method
//        return Collections.emptyList();
//    }

    @Override
    public void close() {
        // TODO Implement this method
        try{
            conn.close();
        }catch(Exception e){
        }
    }

//    @Override
//    public void clearCache() {
//        // TODO Implement this method
//    }

//    @Override
//    public org.apache.ibatis.session.Configuration getConfiguration() {
//        // TODO Implement this method
//        return null;
//    }

    @Override
    public <T> T getMapper(Class<T> type) {
        // TODO Implement this method
        return null;
    }

    @Override
    public Connection getConnection() {
        // TODO Implement this method
        return null;
    }
    
    private String getTableName(Class _class){
        return "tb_"+_class.getName().substring(_class.getName().lastIndexOf(".")+1, _class.getName().length());
    }
    
    private <T> T DBToSingleTemplate(String sql, Class _class,String[] tableColumns,String[] tableColumnTypes){
        List<T> l = DBToTemplateList(sql, _class,tableColumns,tableColumnTypes);
        if(0 == l.size()){
            return null;
        }
        return l.get(0);
    }    
    
    private <E> List<E> DBToTemplateList(String sql, Class _class,String[] tableColumns,String[] tableColumnTypes){
        List<E> templateL = new ArrayList();
        Statement stmt = null;
        ResultSet rs = null;
        try{
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);
            while(rs.next()){
                E o = (E)_class.newInstance();
                for(int i = 0; i< tableColumns.length;i++){
                   if("int".equals(tableColumnTypes[i])){
                       int v = rs.getInt(i+1);
                       Method m = _class.getDeclaredMethod("set" + convertToUppercase(tableColumns[i]), int.class);
                       m.invoke(o, v);
                   }else if("String".equals(tableColumnTypes[i])){
                       String v = rs.getString(i+1);
                       Method m = _class.getDeclaredMethod("set" + convertToUppercase(tableColumns[i]), String.class);
                       m.invoke(o, v);
                   }else if("Date".equals(tableColumnTypes[i])){
                       Long v = rs.getLong(i+1);
                       Method m = _class.getDeclaredMethod("set" + convertToUppercase(tableColumns[i]), Date.class);
                       m.invoke(o, new Date(v));                    
                   }else if("boolean".equals(tableColumnTypes[i])){
                       int v = rs.getInt(i+1);
                       Method m = _class.getDeclaredMethod("set" + convertToUppercase(tableColumns[i]), int.class);
                       m.invoke(o, (v==0?false:true));                    
                   }else if("double".equals(tableColumnTypes[i])){
                       double v = rs.getDouble(i+1);
                       Method m = _class.getDeclaredMethod("set" + convertToUppercase(tableColumns[i]), double.class);
                       m.invoke(o, v); 
                   }else if("long".equals(tableColumnTypes[i])){
                       long v = rs.getLong(i+1);
                       Method m = _class.getDeclaredMethod("set" + convertToUppercase(tableColumns[i]), long.class);
                       m.invoke(o, v);                    
                   }else if("short".equals(tableColumnTypes[i])){
                       short v = rs.getShort(i+1);
                       Method m = _class.getDeclaredMethod("set" + convertToUppercase(tableColumns[i]), short.class);
                       m.invoke(o, v);   
                   }else if("float".equals(tableColumnTypes[i])){
                       float v = rs.getFloat(i+1);
                       Method m = _class.getDeclaredMethod("set" + convertToUppercase(tableColumns[i]), float.class);
                       m.invoke(o, v);     
                   }
                }
                templateL.add(o);
            }   
        }catch(InstantiationException e){
            System.err.println("can not construct the Object, Please add a non-parameter constructor for the class");
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }finally{
            try{
                rs.close();
            }catch(Exception e){
                
            }
            try{
                stmt.close();
            }catch(Exception e){
                
            }
        }
        return templateL;
    }
    
    private String convertToUppercase(String name){
        String first = String.valueOf(name.charAt(0)).toUpperCase();
        return first+name.substring(1, name.length());
    }
    
    public String foreignKeyWhere(Object parentO,Class _parentClass,Field foreignField){
        //compose where string for foreign keys
        ForeignColumn fc = foreignField.getAnnotation(ForeignColumn.class);
        Field[] parentFs = _parentClass.getDeclaredFields();
        String [] fks = fc.foreignKey();
        StringBuilder sb = new StringBuilder();

    try {
        for(Field f : parentFs){
            if(fks[0].equals(f.getName())){
                sb.append("fk_");
                sb.append(fks[0]);
                sb.append("='");
                sb.append(String.valueOf(f.get(parentO)));
                sb.append("'");
                break;
            }
        }
            
        for(int i = 1;i<fks.length;i++){
            for(Field f : parentFs){
                if(fks[i].equals(f.getName())){
                    sb.append(" AND ");
                    sb.append("fk_");
                    sb.append(fks[i]);
                    sb.append("='");
                    sb.append(String.valueOf(f.get(parentO)));
                    sb.append("'");
                    break;
                }
            }
        }  

    } catch (IllegalAccessException e) {
        System.err.println(e);
    }
    

    return sb.toString();
    }
}
