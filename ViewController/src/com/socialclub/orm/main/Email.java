package com.socialclub.orm.main;

import com.socialclub.orm.annotation.Key;

@Key(KeyColumns = {"id"})
public class Email {
    
    public String id;
    public String name;
    public String address;


    public Email(String id, String name, String address) {
        super();
        this.id = id;
        this.name = name;
        this.address = address;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

}
