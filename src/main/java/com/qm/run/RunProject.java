package com.qm.run;

import com.google.gson.Gson;
import com.qm.utils.GetWinDataInfoUtlis;
import org.hyperic.sigar.CpuPerc;
import java.io.*;
import java.util.Map;

/**
 * 程序的入口
 */
public class RunProject {
    public static void main(String[] args) {
        try {
            // 相对路径，如果没有则要建立一个新的output.txt文件
            File writeName = new File("G:\\data.txt");
            if(!writeName.exists()) {
                // 创建新文件,有同名的文件的话直接覆盖
                writeName.createNewFile();
            }
            FileWriter writer = new FileWriter(writeName);
            BufferedWriter out = new BufferedWriter(writer);

            //创建json对象
            Gson json = new Gson();

            // System信息，从jvm获取
            System.setProperty("java.library.path", "so");

            GetWinDataInfoUtlis getWinDataInfo = new GetWinDataInfoUtlis();

            Map<String, String> propertyMap = getWinDataInfo.property();
            //将基本数据转换为json格式并写入文件
            out.write(json.toJson(propertyMap));
            out.newLine();
            /*for (String s : property.keySet()) {
                System.out.println(s+":"+property.get(s));
                //out.write(s+":"+property.get(s));
            }*/

            Map<String, String> memoryMap = getWinDataInfo.memory();
            //将内存数据转换为json格式并写入文件
            out.write(json.toJson(memoryMap));
            out.newLine();

            Map<String, String> cpuMap = getWinDataInfo.cpu();
            //将cpu数据转换为json格式并写入文件
            out.write(json.toJson(cpuMap));
            out.newLine();

            Map<String, String> osMap = getWinDataInfo.os();
            //将操作系统数据转换为json格式并写入文件
            out.write(json.toJson(osMap));
            out.newLine();

            Map<String, String> whoMap = getWinDataInfo.who();
            //将用户数据转换为json并写入文件
            out.write(json.toJson(whoMap));
            out.newLine();

            Map<String, String> fileMap = getWinDataInfo.file();
            //将磁盘信息转换为json并写入文件
            out.write(json.toJson(fileMap));
            out.newLine();

            Map<String, String> netMap = getWinDataInfo.net();
            //将网络数据转换为json并写入文件
            out.write(json.toJson(netMap));
            out.newLine();

            out.flush(); // 把缓存区内容压入文件
            out.close();
            System.out.println("数据写入完成。");
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }




}
