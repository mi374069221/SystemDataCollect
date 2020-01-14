package com.qm.utils;

import com.qm.test.PropertyClient;
import com.qm.test.PropertyServer;

import java.io.*;
import java.net.Socket;
import java.util.Map;

/**
 * 文件传输Client端
 * 功能说明：
 *
 * @author codermi
 * @Date 2019年12月31日
 * @version 1.0
 */
public class FileTransferClientUtil extends Socket {

    private static final String SERVER_IP = PropertyServer.getPropertyServer("server.ip"); // 服务端IP
    private static final int SERVER_PORT =Integer.parseInt(PropertyServer.getPropertyServer("server.port")) ; // 服务端端口

    private Socket client;

    private FileInputStream fis;

    private DataOutputStream dos;


    /**
     * 构造函数
     * 与服务器建立连接
     * @throws Exception
     */
    public FileTransferClientUtil() throws Exception {
        super(SERVER_IP, SERVER_PORT);
        this.client = this;
        System.out.println("Cliect[port:" + client.getLocalPort() + "] 成功连接服务端");
    }

    /**
     * 向服务端传输文件
     * @throws Exception
     */
    public void sendFile() throws Exception {
        try {
            /*系统信息传输到服务端*/
            GetWinDataInfoUtlis getWinDataInfo = new GetWinDataInfoUtlis();
            Map<String, String> propertyMap = getWinDataInfo.property();
            String dataType = PropertyServer.getPropertyServer("sys.data.type");
            String dataPath = PropertyServer.getPropertyServer("app.log.path")+propertyMap.get("Ip")+"."+dataType;

            File sysFile = new File(dataPath);
            if(sysFile.exists()) {
                fis = new FileInputStream(sysFile);
                dos = new DataOutputStream(client.getOutputStream());

                // 文件名和长度
                dos.writeUTF(sysFile.getName());
                dos.flush();
                dos.writeLong(sysFile.length());
                dos.flush();

                // 开始传输文件
                System.out.println("======== 开始传输文件 ========");
                byte[] bytes = new byte[1024];
                int length = 0;
                long progress = 0;
                while((length = fis.read(bytes, 0, bytes.length)) != -1) {
                    dos.write(bytes, 0, length);
                    dos.flush();
                    progress += length;
                    System.out.print("| " + (100*progress/sysFile.length()) + "% |");
                }
                System.out.println();
                System.out.println("======== 文件传输成功 ========");
            }

            /*应用日志传输到服务端*/
            //创建一个读取流对象和文件相关联。
            String logPath = PropertyClient.getPropertyClient("app.log.path");
            FileReader fr=new FileReader(logPath);

            //为了提高效率，加入缓冲技术，将字符读取流对象作为参数传递给缓冲对象的构造函数
            BufferedReader bufr=new BufferedReader(fr);

            String line=null;
            int i = 0;
            while((line=bufr.readLine())!=null)//一行一行读取，在得到返回值是null后停止读取
            {
                System.out.println(line);
                i++;
            }
            System.out.println("已读取到"+i+"行");
            bufr.close();//close()中已经包含了flush



        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(fis != null)
                fis.close();
            if(dos != null)
                dos.close();
            client.close();
        }
    }

    // 读取文件指定行。
    static void readAppLogLineNumber(File sourceFile, int lineNumber) throws IOException {
        FileReader in = new FileReader(sourceFile);
        LineNumberReader reader = new LineNumberReader(in);
        String s = null;
        int line = 1;
        if (lineNumber < 0 || lineNumber > getTotalLines(sourceFile)) {
            System.out.println("不在文件的行数范围之内。");
        } else {

            System.out.println("当前行号为:" + reader.getLineNumber());

            reader.setLineNumber(23);
            System.out.println("更改后行号为:" + reader.getLineNumber());
            long i = reader.getLineNumber();
            while (reader.readLine() != null) {
                line++;
                if (i == line) {
                    s = reader.readLine();
                    System.out.println(s);
                    break;
                }

            }

        }

        reader.close();
        in.close();

    }

    // 文件内容的总行数。
    static int getTotalLines(File file) throws IOException {
        FileReader in = new FileReader(file);
        LineNumberReader reader = new LineNumberReader(in);
        String s = reader.readLine();
        int lines = 0;
        while (s != null) {
            lines++;
            s = reader.readLine();
        }
        reader.close();
        in.close();
        return lines;
    }

    /**
     * 入口
     * @param args
     */
    public static void main(String[] args) {
        try {
            while (true){
            FileTransferClientUtil client = new FileTransferClientUtil(); // 启动客户端连接
                client.sendFile(); // 传输文件
                Thread.sleep(5000);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}

