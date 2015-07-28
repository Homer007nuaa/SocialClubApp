package com.socialclub.orm.main;

import com.socialclub.orm.jdbc.DBConnectionFactory;
import com.socialclub.orm.session.Configuration;
import com.socialclub.orm.session.SqlSession;
import com.socialclub.orm.session.SqlSessionFactory;

import com.socialclub.orm.util.ORMUtil;

import java.sql.Connection;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Test {
    public Test() {
        super();
    }
    
    public static void main(String[] args) throws Exception {
        
        Configuration conf = new Configuration();
        String path = "E:\\Projects\\MAF_ORM\\SocialClubORM\\src\\";
        conf.setConnStr("jdbc:sqlite:"+path.replace('\\','/')+"demo.db").setDbDriverName("org.sqlite.JDBC");
        SqlSession session = SqlSessionFactory.getDefaultSqlSession(conf);
        try{
//            boolean b = session.createSingleTable(Activity.class,true);
            boolean flag = session.existTable(PersonInfo.class);
            boolean flag2 = session.existTable(Activity.class);
            boolean flag3 = session.existTable(Email.class);
            int i = session.createHierachyTables(Activity.class,true);
            flag = session.existTable(PersonInfo.class);
            flag2 = session.existTable(Activity.class);
            flag3 = session.existTable(Email.class);
            
            Email el1 = new Email("1a88ec5f-1c6b-42e5-8988-5f9727b7603d","demo1@oracle.com","demo1@oracle.com");            
            Email el2 = new Email("2587371f-50d6-436e-8d19-c1a12d28cde4","demo2@oracle.com","demo2@oracle.com");            
            Email el3 = new Email("c9262dc7-202c-4f88-9148-c5630596aeb8","demo3@oracle.com","demo3@oracle.com");            
            Email el4 = new Email(ORMUtil.getGUID(),"demo4@oracle.com","demo4@oracle.com");
            
            PersonInfo p1 = new PersonInfo("87e6875c-4087-4c1d-b54a-d7aa2fe54ec4",(short)1,11,el1,"name", 25,11.1,new Date(System.currentTimeMillis()));
            PersonInfo p2 = new PersonInfo("07e8dbb1-e402-47f9-b843-c2bd9b7563cc",(short)2,22,el2,"name", 25,11.1,new Date(System.currentTimeMillis()));
            PersonInfo p3 = new PersonInfo("7c935a94-1ac9-478d-913a-f81e733fbbe4",(short)3,33,el3,"name", 25,11.1,new Date(System.currentTimeMillis()));
            PersonInfo p4 = new PersonInfo(ORMUtil.getGUID(),(short)4,44,el4,"name", 25,11.1,new Date(System.currentTimeMillis()));
            
            List pl = new ArrayList();
            
            pl.add(p1);            
            pl.add(p2);            
            pl.add(p3);   
            
            PersonInfo[] pa = (PersonInfo[])pl.toArray(new PersonInfo[pl.size()]);
            
            Activity act = new Activity("7c98c3bd-81b2-4f43-88e3-cdab180d106a", "Swimming", "Shenzhen NanShan", new Date(System.currentTimeMillis()), pa,
                    p3);
            
            session.insertHierachyObject(act);
            
            el1.setAddress("updated21@oracle.com");
            boolean r1 = session.updateSingleObject(el1);
            int n = session.deleteOne(el1);
            int n2 = session.deleteOne(el1.getClass(),"name = 'demo3@oracle.com'");
            int n3 = session.deleteHierachyObject(act);
            
//            String s = session.select("select name from tb_email where name like 'demo4%'");
            System.out.println(1);
            
//            List<PersonInfo> l = session.selectList(PersonInfo.class,"id = '1' or id = '2'");
//            PersonInfo[] os = (PersonInfo[])l.toArray(new PersonInfo[l.size()]);
//            
//            PersonInfo o1 = session.selectOne(PersonInfo.class,"id = '1' and sh = 1");
//            //session.populateForeignOne((Object)o1,"email");
//            
//            PersonInfo o2 = session.selectOne(PersonInfo.class,"id = 2 and sh = '2'");
//            //session.populateForeignOne((Object)o2,"emails");
//            
//            PersonInfo o3 = session.selectOne(PersonInfo.class,"id = '3' and lo = '3'");
//            //session.populateForeginList((Object)o3,"email");
//            
//            short n =4;
//            float f = 4.44f;
//            PersonInfo o4 = new PersonInfo(n, n, n, null,"demo"+n,f ,f,new Date(System.currentTimeMillis()));
//            session.insertSingleObject(o4);

        }catch(Exception e){
            System.err.println(e);
        }
    }
}
