package com.qm.utils;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
/**
 * agent客户端工具类
 * @author: codermi
 * @date: 2020/1/14 10:44
 */


public class TCPClientUtils extends Socket {
    //获取服务端ip
    private static final String SERVER_IP = PropertiesUtils.getPropertiesFromUserDir("server.properties").getProperty("server.ip"); // 服务端IP
    //获取服务端端口
    private static final int SERVER_PORT = Integer.parseInt(PropertiesUtils.getPropertiesFromUserDir("server.properties").getProperty("server.port")); // 服务端端口
    private Socket socketClient;
    private DataOutputStream os;
    private OutputStream outputStream;
    private FileInputStream fis;
    private InputStream is;
    static int size = 10 * 1024;
    //定义字节数组，用于读取数据流
    static byte[] buf = new byte[size];
    static int len = -1;
    //定义map集合用于增量筛选
    Map<String, String> map = new HashMap<String, String>();
    //获取增量过滤文件路径
    String fileFinalTime = System.getProperty("user.dir")+"/doc/fileFinalTime.txt";
    //创建json对象用于将map转换为json对象
    Gson json = new Gson();
    //创建ObiectMap对象，用于读取本地增量过滤json文件，并转化为map集合
    ObjectMapper mapper = new ObjectMapper();
    /**
     * 构造函数
     * 与服务器建立连接
     *
     * @throws Exception
     */
    public TCPClientUtils() throws IOException {
        super(SERVER_IP, SERVER_PORT);//初始化服务端ip和端口
        this.socketClient = this;//初始化客户端对象
        System.out.println("客户端：[port:" + socketClient.getLocalPort() + "] 成功连接到服务端");
        os = new DataOutputStream(socketClient.getOutputStream());//获取输出流
        //初始化map集合，读取本地增量过滤json文件，并将数据转化为map集合的初始值
        map = mapper.readValue(new File(fileFinalTime),new TypeReference<Map<String, String>>(){});
//        System.out.println("G:\\大米\\QMJK.txt:"+map.get("G:\\大米\\QMJK.txt"));
    }


    public void getServerConnect() throws IOException {
        try {
            //从服务配置文件读取客户配置文件的属性，获取到客户所填写的文件路径
            String logsKey = PropertiesUtils.getPropertiesFromUserDir("server.properties").getProperty("app.logs.key");
            String[] arrPath = logsKey.split(",");

            for (int x = 0; x < arrPath.length; x++) {
                String path = arrPath[x];
                long start = System.currentTimeMillis();//开始时间
                System.out.println("指定文件监控中...");

                String fileName = PropertiesUtils.getPropertiesFromUserDir("config.properties").getProperty(path);//"I:"+File.separator;获取文件或者文件夹路径
                System.out.println(fileName);

                if (!fileName.endsWith(File.separator)) {
                    fileName += File.separator; //fileName.endsWith(File.separator)判断是否以指定字符串结尾
                }
                File file = new File(fileName);
                if (file.exists()) {
                    String parent = file.getParent();//获取父级目录
                    if (parent == null) {
                        File[] fs = file.listFiles();//获取该路径下所有文件夹或者文件的路径
                        for (int i = 0; i < fs.length; i++) {
                            //判断是否隐藏
                            if (fs[i].isHidden()) {
                                System.out.println("隐藏文件" + fs[i].getAbsolutePath() + ",不会被发送");
                                continue;
                            }
                            sendData(fs[i], os, fs[i].getParent());
                        }
                    } else {
                        sendData(file, os, parent);
                    }
                    // Runtime.getRuntime().addShutdownHook(new ShudownHookThread());   //注册收尾线程
                    System.out.println("文件发送成功,共耗时：" + (System.currentTimeMillis() - start) + "ms");
                    os.flush();//监控数据发送到服务端完毕

                    //创建输出流
                    outputStream  = new FileOutputStream(fileFinalTime,false);
                    //map储存的数据写出到本地文件
                    outputStream.write(json.toJson(map).getBytes());
                    //输出流换行
                    String newLine = System.getProperty("line.separator");
                    outputStream.write(newLine.getBytes());
                    outputStream.flush();
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }/*finally {
            if(os!=null) os.close();
            socketClient.close();
        }*/
    }

    /**
     * 向服务端传输文件
     *
     * @throws Exception
     */
    private void sendData(File file, OutputStream os, String sendFileName) throws Exception {
        String fname = file.getAbsolutePath();//返回的是定义时的路径对应的相对路径，但不会处理“.”和“..”的情况
        String name = fname.replace(sendFileName, "");//将父目录替换为空，得到文件名或文件夹名称

        //判断是文件夹还是文件
        if (file.isDirectory()) {
            System.out.println("文件夹名称：" + name + "   文件夹名称长度：" + name.length());
            File[] fs = file.listFiles();//获取该文件夹下的所有文件夹和文件的路径
            os.write(new byte[]{(byte) 2}, 0, 1);//2:文件夹名
            int fileLength = name.getBytes().length;
            os.write(intToBytes(fileLength), 0, 4);//文件名的长度
            os.write(name.getBytes("utf-8"), 0, name.getBytes().length);//文件名
            os.flush();

            System.out.println("0");
            for (int x = 0; x < fs.length; x++) {
                if (fs[x].isDirectory()) {
                    System.out.println("1");
                    sendData(fs[x], os, sendFileName);
                } else if (fs[x].isHidden()) {
                    System.out.println("隐藏文件" + fs[x].getAbsolutePath() + ",不会被发送");
                    continue;
                } else {
                    System.out.println("2");
                    //sendData(fs[x], os, sendFileName);
                    //如果map集合key中没有包含此文件的值或者包含此文件的值（也就是key）
                    // 并且该key对应的值（value）和当前文件最后一次修改时间不一样。
                    if (!map.keySet().contains(fs[x].toString())||
                         (map.keySet().contains(fs[x].toString())&&
                         !map.get(fs[x].toString()).equals(ReadFileTimeUtils.getModifiedTime_1(fs[x].toString())))) {
                        System.out.println(fs[x]);//打印文件路径
                        System.out.println(map.get(fs[x].toString()));//打印map已存在的该文件对应的值
                        System.out.println(ReadFileTimeUtils.getModifiedTime_1(fs[x].toString()));//打印当前文件最后一次修改时间
                        //map.put(MD5Utils.getFileMD5(fs[x]), setDateFormat(System.currentTimeMillis()));
                        sendData(fs[x], os, sendFileName);
                    } else {
                        System.out.println("文件" + fs[x] + "已经存在，不给予备份~请注意！");
                    }

                }
            }

        } else if (file.isFile()) {
            //如果map集合key中没有包含此文件的值或者包含此文件的值（也就是key）
            // 并且该key对应的值（value）和当前文件最后一次修改时间不一样。
            if (!map.keySet().contains(file.toString())) {
                //将文件，和新的修改时间，保存到map集合
                map.put(file.toString(), ReadFileTimeUtils.getModifiedTime_1(file.toString()));
                os.write(new byte[]{(byte) 1}, 0, 1);//1：文件名
                int fileLength = name.getBytes().length;
                os.write(intToBytes(fileLength), 0, 4);//文件名的长度
                os.write(name.getBytes("utf-8"), 0, name.getBytes().length);//文件名
                System.out.println("文件：" + name + "   " + name.length() + "  " + file.length());
                os.flush();
                fis = new FileInputStream(file);
                os.write(new byte[]{(byte) 0}, 0, 1);//0表示文件数据
                os.write(longToBytes(file.length()), 0, 8);//文件的长度
                os.flush();
                while ((len = fis.read(buf, 0, size)) != -1) {
                    os.write(buf, 0, len);
                    os.flush();
                }
            }else if (map.keySet().contains(file.toString())&&
                    !map.get(file.toString()).equals(ReadFileTimeUtils.getModifiedTime_1(file.toString()))){

                //将文件，和新的修改时间，保存到map集合
                map.put(file.toString(), ReadFileTimeUtils.getModifiedTime_1(file.toString()));
                os.write(new byte[]{(byte) 1}, 0, 1);//1：文件名
                int fileLength = name.getBytes().length;
                os.write(intToBytes(fileLength), 0, 4);//文件夹名的长度
                os.write(name.getBytes("utf-8"), 0, name.getBytes().length);//文件夹名
                System.out.println("文件：" + name + "   " + name.length() + "  " + file.length());
                os.flush();
                fis = new FileInputStream(file);
                os.write(new byte[]{(byte) 0}, 0, 1);//0表示文件数据
                os.write(longToBytes(file.length()), 0, 8);//文件的长度
                os.flush();
                while ((len = fis.read(buf, 0, size)) != -1) {
                    os.write(buf, 0, len);
                    os.flush();
                }

            } else {
                System.out.println("文件" + name + "已经存在，不给予备份--请注意！");
            }


            // fis.close();

        }
    }

    private static byte[] intToBytes(int i) {
        byte[] b = new byte[4];
        b[0] = (byte) ((i >>> 24) & 255);
        b[1] = (byte) ((i >>> 16) & 255);
        b[2] = (byte) ((i >>> 8) & 255);
        b[3] = (byte) (i & 255);
        return b;
    }

    private static byte[] longToBytes(long i) {
        byte[] b = new byte[8];
        b[0] = (byte) ((i >>> 56) & 255);
        b[1] = (byte) ((i >>> 48) & 255);
        b[2] = (byte) ((i >>> 40) & 255);
        b[3] = (byte) ((i >>> 32) & 255);
        b[4] = (byte) ((i >>> 24) & 255);
        b[5] = (byte) ((i >>> 16) & 255);
        b[6] = (byte) ((i >>> 8) & 255);
        b[7] = (byte) (i & 255);
        return b;
    }

    //获取项目名称
    private  void getAppName(){
        String appName = PropertiesUtils.getPropertiesFromUserDir("config.properties").getProperty("app.name");
        PrintWriter pw =new PrintWriter(os);
        pw.print(appName);
    }



    //转化时间格式
    public static String setDateFormat(long time) {
        Date dt = new Date(time);
        DateFormat df = new SimpleDateFormat("YYYY年MM月DD日 hh时MM分ss秒");
        return df.format(dt);
    }

    //优雅的关闭程序

    public class ShudownHookThread extends Thread {

        @Override
        public void run() {
            System.out.println("线程收尾工作开始执行了!!!");
            System.gc();
            //此处可以进行关闭连接
            //判断队列中的数据是否消费完毕
            //直到消费完毕再退出
        }
    }


    public static void main(String[] args) throws InterruptedException, IOException {

        Timer timer = new Timer();
       final TCPClientUtils  tcpClient = new TCPClientUtils();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    tcpClient.getServerConnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 10000);
        /*while (true){
            tcpClient.getServerConnect();
            Thread.sleep(5000);
        }*/
        // timer.cancel();

    }


}
