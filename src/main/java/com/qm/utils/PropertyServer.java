package com.qm.utils;

import java.io.*;
import java.util.Properties;

/**
 * 获取properties配置文件
 */
public class PropertyServer {
    private static Properties props;
    static {
        loadPropsServer();
    }


    synchronized static private void loadPropsServer(){
        //System.out.println("开始加载properties文件内容.......");
        props = new Properties();
        InputStream ins = null;
        try {
            ins = new BufferedInputStream(new FileInputStream("src\\main\\resources\\properties\\server.properties"));
            props.load(ins);
        } catch (FileNotFoundException e) {
            System.out.println("jdbc.properties文件未找到");
        } catch (IOException e) {
            System.out.println("出现IOException");
        } finally {
            try {
                if(null != ins) {
                    ins.close();
                }
            } catch (IOException e) {
                System.out.println("jdbc.properties文件流关闭出现异常");
            }
        }
    }


    /**
     * 根据key获取配置文件中的属性
     */
    public static String getPropertyServer(String key){
        if(null == props) {

            loadPropsServer();
        }
        return props.getProperty(key);
    }

    /**
     * 根据key获取配置文件中的属性，当为null时返回指定的默认值
     */
    public static String getPropertyServer(String key, String defaultValue) {
        if(null == props) {

            loadPropsServer();
        }
        return props.getProperty(key, defaultValue);
    }

}
