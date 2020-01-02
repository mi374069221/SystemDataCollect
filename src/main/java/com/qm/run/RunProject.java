package com.qm.run;

import com.google.gson.Gson;
import com.qm.utils.FileTransferClientUtil;
import com.qm.utils.GetWinDataInfoUtlis;
import com.qm.utils.PropertyUtil;
import sun.security.x509.IPAddressName;

import java.io.*;
import java.util.Map;

/**
 * 程序的入口
 */
public class RunProject {
    public static void main(String[] args) {
        try {
            while (true){
                /*一：获取服务器数据*/
                //建立一个output.txt文件
                //File writeName = new File("G:\\data.txt");
                GetWinDataInfoUtlis getWinDataInfo = new GetWinDataInfoUtlis();
                Map<String, String> propertyMap = getWinDataInfo.property();
               // System.out.println(propertyMap.get("Ip"));
                String dataType = PropertyUtil.getProperty("sys.data.type");
                String dataPath = PropertyUtil.getProperty("sys.data.path")+propertyMap.get("Ip")+"."+dataType;
                System.out.println(dataPath);
                File writeName = new File(dataPath);

                if(!writeName.exists()) {
                    // 创建新文件,有同名的文件的话直接覆盖
                    writeName.createNewFile();
                }
                FileWriter writer = new FileWriter(writeName);
                BufferedWriter out = new BufferedWriter(writer);

                //创建json对象
                Gson json = new Gson();
                // System信息，从jvm获取
                String sittingPath = PropertyUtil.getProperty("sitting.path");
                System.setProperty("java.library.path", sittingPath);

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

                /*二，启动客户端，连接服务端，将数据发送到服务端*/
                FileTransferClientUtil client = new FileTransferClientUtil(); // 启动客户端连接
                client.sendFile(); // 传输文件
                Thread.sleep(5000);
            }
        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }

}
