package com.qm.run;


import com.qm.utils.MD5Utils;
import com.qm.utils.PropertyClient;
import com.qm.utils.PropertyServer;

import java.io.*;
import java.net.Socket;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class TCPClient extends Socket {
    private static final String SERVER_IP = PropertyServer.getPropertyServer("server.ip"); // 服务端IP
    private static final int SERVER_PORT = Integer.parseInt(PropertyServer.getPropertyServer("server.port")); // 服务端端口
    private Socket socketClient;
    private OutputStream os;
    private FileInputStream fis;

    static int size = 10 * 1024;
    static byte[] buf = new byte[size];
    static int len = -1;
    Map<String, String> map = new HashMap<>();
    String[] arrPath = new String[]{"app.log.path", "tomcat.log.path"};

    /**
     * 构造函数
     * 与服务器建立连接
     *
     * @throws Exception
     */
    public TCPClient() throws IOException {
        super(SERVER_IP, SERVER_PORT);
        this.socketClient = this;
        System.out.println("客户端：[port:" + socketClient.getLocalPort() + "] 成功连接到服务端");
        os = new DataOutputStream(socketClient.getOutputStream());
    }


    public void getServerConnect() throws IOException {


        try {
            String path;
            for (int x = 0; x < arrPath.length; x++) {
                path = arrPath[x];

                long start = System.currentTimeMillis();//开始时间
                System.out.println("指定文件监控中...");

                String fileName = PropertyClient.getPropertyClient(path);//"I:"+File.separator;获取文件或者文件夹路径
                //String fileName="G:"+ File.separator+"myData";

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
                    os.flush();
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
        //System.out.println(fname);
        String name = fname.replace(sendFileName, "");//将父目录替换为空，得到文件名或文件夹名称

        //判断是文件夹还是文件
        if (file.isDirectory()) {
            System.out.println("文件夹名称：" + name + "   文件夹名称长度：" + name.length());
            File[] fs = file.listFiles();//获取该文件夹下的所有文件夹和文件的路径

            os.write(new byte[]{(byte) 2}, 0, 1);//2:文件夹名
            int fileLength = name.getBytes().length;
            os.write(intToBytes(fileLength), 0, 4);//文件名的长度
            os.write(name.getBytes(), 0, name.getBytes().length);//文件名
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
                    if (!map.keySet().contains(MD5Utils.getFileMD5(fs[x]))) {
                        System.out.println(fs[x] + ":" + MD5Utils.getFileMD5(fs[x]));
                        //map.put(MD5Utils.getFileMD5(fs[x]), setDateFormat(System.currentTimeMillis()));
                        sendData(fs[x], os, sendFileName);
                    } else {
                        System.out.println(fs[x] + ":" + MD5Utils.getFileMD5(fs[x]));
                        System.out.println("文件" + fs[x] + "已经存在，不给予备份~请注意！");
                    }

                }
            }

        } else if (file.isFile()) {

            if (!map.keySet().contains(MD5Utils.getFileMD5(file))) {
                map.put(MD5Utils.getFileMD5(file), setDateFormat(System.currentTimeMillis()));
                os.write(new byte[]{(byte) 1}, 0, 1);//1：文件名
                int fileLength = name.getBytes().length;
                os.write(intToBytes(fileLength), 0, 4);//文件夹名的长度
                os.write(name.getBytes(), 0, name.getBytes().length);//文件夹名
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
        String appName = PropertyClient.getPropertyClient("app.name");
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
        TCPClient tcpClient = new TCPClient();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    tcpClient.getServerConnect();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, 0, 5000);
        /*while (true){
            tcpClient.getServerConnect();
            Thread.sleep(5000);
        }*/
        // timer.cancel();

    }


}
