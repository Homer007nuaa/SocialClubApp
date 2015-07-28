package com.socialclub.orm.util;

import java.util.UUID;

public class ORMUtil {
    public ORMUtil() {
        super();
    }
    
    static public String getGUID(){
        UUID uuid = UUID.randomUUID();
        return uuid.toString();
    }
}
