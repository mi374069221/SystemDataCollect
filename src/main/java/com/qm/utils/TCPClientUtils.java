package com.qm.utils;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;

import javax.swing.tree.RowMapper;
import java.io.*;
import java.net.Socket;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
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
    //获取项目id和项目名称
    String appId = PropertiesUtils.getPropertiesFromUserDir("config.properties").getProperty("app.id");
    String appName = PropertiesUtils.getPropertiesFromUserDir("config.properties").getProperty("app.name");
    //定义流
    private Socket socketClient;
    private DataOutputStream os;
    private OutputStream outputStream;
    private FileInputStream fis;
    private InputStreamReader isr;
    private BufferedReader br;
    static int size = 10 * 1024;
    //定义字节数组，用于读取数据流
    static byte[] buf = new byte[size];
    static int len = -1;
    //定义map集合用于增量筛选
    Map<String, String> timeMap = new HashMap<String, String>();
    Map<String, Long> byteMap = new HashMap<String, Long>();
    Map<String, Integer> lineMap = new HashMap<String, Integer>();
    //定义List集合存储读取文件的行数
    List<Integer> list = new ArrayList<>();

    //获取数据库连接，存储文件夹读取总行数
    Connection conn = JdbcUtils.getConnection();
    //获取增量过滤文件路径
    String fileFinalTime = System.getProperty("user.dir")+"/doc/fileFinalTime.txt";
    String fileFinalByte = System.getProperty("user.dir")+"/doc/fileFinalByte.txt";
    String fileFinalLine = System.getProperty("user.dir")+"/doc/fileFinalLine.txt";

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
        fis = new FileInputStream(fileFinalTime);
        if (fis.read()!=-1){
            timeMap = mapper.readValue(new File(fileFinalTime),new TypeReference<Map<String, String>>(){});
        }
        //初始化map集合，读取本地增量过滤json文件，并将数据转化为map集合的初始值
        fis = new FileInputStream(fileFinalByte);
        if (fis.read()!=-1){
            byteMap = mapper.readValue(new File(fileFinalByte),new TypeReference<Map<String, Long>>(){});
        }
        //初始化map集合，读取本地增量过滤json文件，并将数据转化为map集合的初始值
        fis = new FileInputStream(fileFinalLine);
        if (fis.read()!=-1){
            lineMap = mapper.readValue(new File(fileFinalLine),new TypeReference<Map<String, Integer>>(){});
        }



        //初始化插入项目id和项目名称到数据库
        String sqlInsert = "insert into file_line_num(AppId,AppName)values('"+appId+"','"+appName+"')";
        JdbcUtils.insert(conn,sqlInsert);
    }


    public void getServerConnect() throws IOException {
        try {
            //获取当天日期
            String day = getDay();
            String sqlUpdateDay = "update file_line_num set DateTime='"+day+"'where AppId='"+appId+"'";
            JdbcUtils.update(conn,sqlUpdateDay);
            //从服务配置文件读取客户配置文件的属性，获取到客户所填写的文件路径
            String logsKey = PropertiesUtils.getPropertiesFromUserDir("server.properties").getProperty("app.logs.key");
            String[] arrPath = logsKey.split(",");
            //从服务配置文件读取模块对应数据库字段
            String fileModelTotal = PropertiesUtils.getPropertiesFromUserDir("server.properties").getProperty("file.model.total");
            String[] fileModelsTotal = fileModelTotal.split(",");
            String fileModelAdd = PropertiesUtils.getPropertiesFromUserDir("server.properties").getProperty("file.model.add");
            String[] fileModelsAdd = fileModelAdd.split(",");

            for (int x = 0; x < arrPath.length; x++) {
                String path = arrPath[x];
                String fileModelT = fileModelsTotal[x];
                String fileModelA = fileModelsAdd[x];
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

                            //获取新增条数
                            Integer sumAdd = 0;
                            for (Integer integer : list) {
                                sumAdd +=integer;
                            }
                            //将模块对应的行数保存到数据库
                            String sqlUpdateA = "update file_line_num set "+fileModelA+"='"+sumAdd+"'where AppId='"+appId+"'";
                            JdbcUtils.update(conn,sqlUpdateA);


                            //将模块对应的行数保存到数据库
                            saveLineNumToDb(fileModelT,sumAdd);

                            list.clear();
                            
                        }
                    } else {
                        sendData(file, os, parent);

                        //获取新增条数
                        Integer sumAdd = 0;
                        for (Integer integer : list) {
                            sumAdd +=integer;
                        }
                        //将模块对应的行数保存到数据库
                        String sqlUpdateA = "update file_line_num set "+fileModelA+"='"+sumAdd+"'where AppId='"+appId+"'";
                        JdbcUtils.update(conn,sqlUpdateA);


                        //将模块对应的行数保存到数据库
                        saveLineNumToDb(fileModelT,sumAdd);

                        list.clear();
                        
                        
                    }
                    // Runtime.getRuntime().addShutdownHook(new ShudownHookThread());   //注册收尾线程
                    System.out.println("文件发送成功,共耗时：" + (System.currentTimeMillis() - start) + "ms");
                    os.flush();//监控数据发送到服务端完毕

                }else{
                    System.out.println(file+"目录或者文件不存在");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            /*if(os!=null) os.close();
            socketClient.close();*/
        }
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
            if (!timeMap.keySet().contains(file.toString())){
                timeMap.put(file.toString(),ReadFileTimeUtils.getModifiedTime_1(file.toString()));
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
                        if (!timeMap.keySet().contains(fs[x].toString())||
                                (timeMap.keySet().contains(fs[x].toString())&&
                                        !timeMap.get(fs[x].toString()).equals(ReadFileTimeUtils.getModifiedTime_1(fs[x].toString())))) {
                            System.out.println(fs[x]);//打印文件路径
                            System.out.println(timeMap.get(fs[x].toString()));//打印map已存在的该文件对应的值
                            System.out.println(ReadFileTimeUtils.getModifiedTime_1(fs[x].toString()));//打印当前文件最后一次修改时间
                            //timeMap.put(MD5Utils.getFileMD5(fs[x]), setDateFormat(System.currentTimeMillis()));
                            sendData(fs[x], os, sendFileName);
                        } else {
                            System.out.println("文件" + fs[x] + "已经存在，不给予备份~请注意！");
                        }

                    }
                }
            }else if (timeMap.keySet().contains(file.toString())){
                System.out.println("文件夹名称：" + name + "   文件夹名称长度：" + name.length());
                File[] fs = file.listFiles();//获取该文件夹下的所有文件夹和文件的路径
                for (int x = 0; x < fs.length; x++) {
                    if (fs[x].isDirectory()) {
                        sendData(fs[x], os, sendFileName);
                    } else if (fs[x].isHidden()) {
                        System.out.println("隐藏文件" + fs[x].getAbsolutePath() + ",不会被发送");
                        continue;
                    } else {
                        //sendData(fs[x], os, sendFileName);
                        //如果map集合key中没有包含此文件的值或者包含此文件的值（也就是key）
                        // 并且该key对应的值（value）和当前文件最后一次修改时间不一样。
                        if (!timeMap.keySet().contains(fs[x].toString())||
                                (timeMap.keySet().contains(fs[x].toString())&&
                                        !timeMap.get(fs[x].toString()).equals(ReadFileTimeUtils.getModifiedTime_1(fs[x].toString())))) {
                            System.out.println(fs[x]);//打印文件路径
                            System.out.println(timeMap.get(fs[x].toString()));//打印map已存在的该文件对应的值
                            System.out.println(ReadFileTimeUtils.getModifiedTime_1(fs[x].toString()));//打印当前文件最后一次修改时间
                            //timeMap.put(MD5Utils.getFileMD5(fs[x]), setDateFormat(System.currentTimeMillis()));
                            sendData(fs[x], os, sendFileName);
                        } else {
                            System.out.println("文件" + fs[x] + "已经存在，不给予备份--请注意！");
                        }

                    }
                }
            }

        } else if (file.isFile()) {
            //如果map集合key中没有包含此文件的值或者包含此文件的值（也就是key）
            // 并且该key对应的值（value）和当前文件最后一次修改时间不一样。
            if (!timeMap.keySet().contains(file.toString())) {
                //将文件，和新的修改时间，保存到map集合
                timeMap.put(file.toString(), ReadFileTimeUtils.getModifiedTime_1(file.toString()));
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
                long bytes = 0L;
                while ((len = fis.read(buf, 0, size)) != -1) {
                    bytes += len;
                    os.write(buf, 0, len);
                    os.flush();
                }
                System.out.println("文件字节数："+bytes);
                byteMap.put(file.toString(),bytes);

                //获取文件行数
                FileInputStream fisLine = new FileInputStream(file);
                isr = new InputStreamReader(fisLine);
                br = new BufferedReader(isr);
                int line = 0;
                String str=null;
                while ((str=br.readLine())!=null){
                    line++;
                }
                System.out.println("文件行数："+line);
                lineMap.put(file.toString(),line);
                list.add(line);
                


            }else if (timeMap.keySet().contains(file.toString())&&
                    !timeMap.get(file.toString()).equals(ReadFileTimeUtils.getModifiedTime_1(file.toString()))){

                //将文件，和新的修改时间，保存到map集合
                timeMap.put(file.toString(), ReadFileTimeUtils.getModifiedTime_1(file.toString()));
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
                //System.out.println(byteMap.get(file.toString()));
                //fis.skip(byteMap.get(file.toString()));
                Long n = byteMap.get(file.toString());//获取保存的已写出的字节数
                Long i = 0L;
                skipBytesFromStream(fis,n);
                while ((len = fis.read(buf, 0, size)) != -1) {
                    os.write(buf, 0, len);
                    os.flush();
                    i+=len;
                }
                //将文件和已写出的总字节保存到byteMap中
                byteMap.put(file.toString(),i+n);

                //获取文件行数
               /* FileReader fileReader = new FileReader(file);
                LineNumberReader lineNumberReader = new LineNumberReader(fileReader);
                Integer lineSkip = lineMap.get(file.toString());
                lineNumberReader.skip(lineSkip);
                int lineAdd = 1;
                String s =null;
                while ((s=lineNumberReader.readLine()) != null) {
                    lineAdd++;
                }
                System.out.println("文件行数=========================："+lineAdd);
                lineMap.put(file.toString(),lineAdd+lineSkip);
                list.add(lineAdd);*/
                FileInputStream fisLine = new FileInputStream(file);
                isr = new InputStreamReader(fisLine);
                br = new BufferedReader(isr);
                int line = 0;
                String s=null;
                Integer lineOld = lineMap.get(file.toString());
                while ((s=br.readLine())!=null){
                    line++;
                }
                System.out.println("文件行数："+line);
                lineMap.put(file.toString(),line);
                list.add(line-lineOld);

            } else {
                System.out.println("文件" + name + "已经存在，不给予备份--请注意！");
            }


            // fis.close();

        }
    }

    /*将行号保存到数据库*/
    public void saveLineNumToDb(String fileModelT,Integer sumAdd){
        String[] fileNumTitle = new String[1];
        class RowMapper implements JdbcUtils.IRowMapper{
            @Override
            public void rowMapper(ResultSet resultSet) {
                try {
                    while (resultSet.next()){
                        fileNumTitle[0] = resultSet.getString(fileModelT);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
        String sqlQueryT = "select "+fileModelT+" from file_line_num where AppId='"+appId+"'";
        try {
            JdbcUtils.query(conn, sqlQueryT,new RowMapper());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        if (fileNumTitle[0]!=null){
            Integer sum = Integer.parseInt(fileNumTitle[0])+ sumAdd;
            String sqlUpdateT = "update file_line_num set "+fileModelT+"='"+sum+"'where AppId='"+appId+"'";
            JdbcUtils.update(conn,sqlUpdateT);
        }else {
            String sqlUpdateT = "update file_line_num set "+fileModelT+"='"+sumAdd+"'where AppId='"+appId+"'";
            JdbcUtils.update(conn,sqlUpdateT);
        }
    }

    /*将map集合内的数据保存到doc文件夹*/
    public void saveMapToDoc(){
        //创建输出流
        try {
            outputStream  = new FileOutputStream(fileFinalTime,false);
            //map储存的数据写出到本地文件
            outputStream.write(json.toJson(timeMap).getBytes());
            //输出流换行,追加保存時使用
                    /*String newLine = System.getProperty("line.separator");
                    outputStream.write(newLine.getBytes());*/
            outputStream.flush();


            //创建输出流
            outputStream = new FileOutputStream(fileFinalByte,false);
            //map储存的数据写出到本地文件
            outputStream.write(json.toJson(byteMap).getBytes());
            outputStream.flush();

            //创建输出流
            outputStream = new FileOutputStream(fileFinalLine,false);
            //map储存的数据写出到本地文件
            outputStream.write(json.toJson(lineMap).getBytes());
            outputStream.flush();


        } catch (Exception e) {
            System.out.println("保存Map数据到Doc异常");
        }

    }


    /*重写了Inpustream 中的skip(long n) 方法，将数据流中起始的n 个字节跳过*/
    private long skipBytesFromStream(InputStream inputStream, long n) {
        long remaining = n;
        // SKIP_BUFFER_SIZE is used to determine the size of skipBuffer
        int SKIP_BUFFER_SIZE = 2048;
        // skipBuffer is initialized in skip(long), if needed.
        byte[] skipBuffer = null;
        int nr = 0;
        if (skipBuffer == null) {
            skipBuffer = new byte[SKIP_BUFFER_SIZE];
        }
        byte[] localSkipBuffer = skipBuffer;
        if (n <= 0) {
            return 0;
        }
        while (remaining > 0) {
            try {
                nr = inputStream.read(localSkipBuffer, 0,
                        (int) Math.min(SKIP_BUFFER_SIZE, remaining));
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (nr < 0) {
                break;
            }
            remaining -= nr;
        }
        return n - remaining;
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

    //获取当前时间
    public String getDay(){
        //获取本次方法执行时间
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = new Date();
        String nowDate = simpleDateFormat.format(date);
        return nowDate;
    }
    


    public static void main(String[] args) throws InterruptedException, IOException {

        Timer timer = new Timer();
       final TCPClientUtils  tcpClient = new TCPClientUtils();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    tcpClient.getServerConnect();
                    tcpClient.saveMapToDoc();
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
