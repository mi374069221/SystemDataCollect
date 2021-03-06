package com.qm.test;

import java.io.*;

import java.util.Iterator;
import java.util.Properties;

/**
 * 获取配置文件
 */
public class GetConfigUtils {


    public static void main(String[] args) throws Exception {
        Properties pps = new Properties();
        InputStream in = new BufferedInputStream(new FileInputStream("src\\main\\resources\\properties\\config.properties"));
        pps.load(in);
        Iterator<String> iterator = pps.stringPropertyNames().iterator();
        while (iterator.hasNext()){
            String key = iterator.next();
            System.out.println(key+":"+pps.getProperty(key));
        }

        ///保存属性到b.properties文件
        FileOutputStream oFile = new FileOutputStream("b.properties", true);//true表示追加打开
        pps.setProperty("phone", "10086");
        pps.store(oFile, "The New properties file");
        oFile.close();
    }
}
