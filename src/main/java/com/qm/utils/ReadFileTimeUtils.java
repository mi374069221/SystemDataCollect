package com.qm.utils;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * 读取文件创建时间和最后修改时间
 */
public class ReadFileTimeUtils {
    /**
     * 读取文件创建时间
     */
    public static String getCreateTime(String file){
        String filePath = file;
        String strTime = null;
        try {
            Process p = Runtime.getRuntime().exec("cmd /C dir "
                    + filePath
                    + "/tc" );
            InputStream is = p.getInputStream();
            BufferedReader br = new BufferedReader(new InputStreamReader(is));
            String line;
            while((line = br.readLine()) != null){
                if(line.endsWith(".txt")){
                    strTime = line.substring(0,17);
                    break;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        //System.out.println("创建时间    " + strTime);
        return strTime;
    }
    /**
     * 读取文件修改时间的方法1
     */
    @SuppressWarnings("deprecation")
    public static String getModifiedTime_1(String file){
        File f = new File(file);
        Calendar cal = Calendar.getInstance();
        long time = f.lastModified();
        cal.setTimeInMillis(time);
        //此处toLocalString()方法是不推荐的，但是仍可输出
        return cal.getTime().toLocaleString();
        //System.out.println("修改时间[1] " + cal.getTime().toLocaleString());
    }

    /**
     * 读取修改时间的方法2
     */
    public static String getModifiedTime_2(String file){
        File f = new File(file);
        Calendar cal = Calendar.getInstance();
        long time = f.lastModified();
        //System.out.println(time);
       /* Date date = new Date();
        long time1 = date.getTime();
        System.out.println(time1);*/
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        cal.setTimeInMillis(time);
        //System.out.println("修改时间[2] " + formatter.format(cal.getTime()));
        return formatter.format(cal.getTime());
    }

    public static void main(String[] args) {
        getCreateTime("G:\\大米\\192.168.40.1.txt");
        getModifiedTime_1("G:\\大米\\192.168.40.1.txt");
        getModifiedTime_2("G:\\大米\\192.168.40.1.txt");
    }
}
