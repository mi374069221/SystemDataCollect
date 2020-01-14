package com.qm.run;

import com.qm.utils.GetWinDataInfoUtlis;
import com.qm.utils.TCPClientUtils;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 程序的入口
 */
public class RunProject {
    public static void main(String[] args) {
        try {
                /*启动客户端，连接服务端，将数据发送到服务端*/
                Timer timer = new Timer();
              final  TCPClientUtils tcpClient = new TCPClientUtils();
              final  GetWinDataInfoUtlis getWinDataInfoUtlis = new GetWinDataInfoUtlis();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            getWinDataInfoUtlis.saveDataToServer();
                            tcpClient.getServerConnect();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },0,30000);

        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
