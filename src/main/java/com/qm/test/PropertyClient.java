package com.qm.test;

import java.io.*;
import java.util.Properties;

/**
 * 获取properties配置文件
 */
public class PropertyClient {
    private static Properties props;
    static {
        loadProps();
    }

    synchronized static private void loadProps(){
        //System.out.println("开始加载properties文件内容.......");
        props = new Properties();
        InputStream in = null;
        try {
            // 第一种，通过类加载器进行获取properties文件流-->
           // in = PropertyClient.class.getClassLoader().getResourceAsStream("src\\main\\resources\\properties\\config.properties");
            // 第二种，通过类进行获取properties文件流-->
            in = new BufferedInputStream(new FileInputStream("src\\main\\resources\\properties\\config.properties"));
            props.load(in);
        } catch (FileNotFoundException e) {
            System.out.println("jdbc.properties文件未找到");
        } catch (IOException e) {
            System.out.println("出现IOException");
        } finally {
            try {
                if(null != in) {
                    in.close();
                }
            } catch (IOException e) {
                System.out.println("jdbc.properties文件流关闭出现异常");
            }
        }
        //System.out.println("加载properties文件内容完成...........");
        //System.out.println("properties文件内容：" + props);
    }


    /**
     * 根据key获取配置文件中的属性
     */
    public static String getPropertyClient(String key){
        if(null == props) {
            loadProps();

        }
        return props.getProperty(key);
    }

    /**
     * 根据key获取配置文件中的属性，当为null时返回指定的默认值
     */
    public static String getPropertyClient(String key, String defaultValue) {
        if(null == props) {
            loadProps();
        }
        return props.getProperty(key, defaultValue);
    }


}
