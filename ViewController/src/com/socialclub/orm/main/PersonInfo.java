package com.socialclub.orm.main;

import com.socialclub.orm.annotation.ForeignColumn;
import com.socialclub.orm.annotation.Key;
import com.socialclub.orm.annotation.NonDBColumn;

import java.util.Date;

@Key(KeyColumns = {"id"})
public class PersonInfo {
    
    public String id;
    public short sh;
    public long lo;
    @ForeignColumn(foreignKey = {"id"})
    public Email email;
    public String name;
    public float age;
    public double height;
    public Date date;
    
    @NonDBColumn
    public boolean flag;


    public PersonInfo() {

    }
    
    public PersonInfo(String id, short sh, long lo, Email email, String name, float age, double height,Date date) {
        super();
        this.id = id;
        this.sh = sh;
        this.lo = lo;
        this.email = email;
        this.name = name;
        this.age = age;
        this.height = height;
        this.date = date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setSh(short sh) {
        this.sh = sh;
    }

    public short getSh() {
        return sh;
    }

    public void setLo(long lo) {
        this.lo = lo;
    }

    public long getLo() {
        return lo;
    }

    public void setEmail(Email email) {
        this.email = email;
    }

    public Email getEmail() {
        return email;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAge(float age) {
        this.age = age;
    }

    public float getAge() {
        return age;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getHeight() {
        return height;
    }
}
