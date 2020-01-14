package com.qm.utils;

import java.io.*;
import java.util.Properties;

/**
 * 获取配置
 */
public class PropertiesUtils {


    public  static Properties getPropertiesFromUserDir(String prop){
        Properties properties = new Properties();
        String rootPath = System.getProperty("user.dir");

        InputStream in = null;
        try {
            in =new BufferedInputStream(new FileInputStream(rootPath+"/conf/" + prop)) ;
            //in=PropertiesUtils.class.getClassLoader().getResourceAsStream((rootPath+"/conf/" + prop));
           properties.load(new InputStreamReader(in, "utf-8"));
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
//        TOPIC = (String) properties.get("topic");//
    }

    public static void main(String[] args) {
        System.out.println(PropertiesUtils.getPropertiesFromUserDir("config.properties").getProperty("app.name"));
    }
}
