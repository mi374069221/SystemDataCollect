package com.qm.test;

import com.qm.utils.PropertiesUtils;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Test {
    private static Properties props;
    private static void load() {
        InputStream in = PropertiesUtils.class.getClassLoader().getResourceAsStream("src\\main\\resources\\properties\\config.properties" );
        props = new Properties() ;
        try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getValue(String key) {

        return props.getProperty(key);
    }



    public static void main(String[] args) {
        System.out.println(Test.getValue("app.name"));
    }
}
