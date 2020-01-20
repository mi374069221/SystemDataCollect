package com.qm.run;

import com.qm.test.GetWinDataInfoUtlis;
import com.qm.utils.GetWinDataInfoUtil;
import com.qm.utils.TCPClientUtils;

import java.io.*;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 程序的入口
 */
public class RunProject {
    public static void main(String[] args) {
        //时间间隔
        final long PERIOD_DAY = 24 * 60 * 60 * 1000;
        try {
                /*启动客户端，连接服务端，将数据发送到服务端*/
                Timer timer1 = new Timer();
              final  TCPClientUtils tcpClient = new TCPClientUtils();
                timer1.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                            tcpClient.getServerConnect();
                            tcpClient.saveMapToDoc();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                },0,PERIOD_DAY);


                Timer timer2 = new Timer();
              final  GetWinDataInfoUtil getWinDataInfoUtil = new GetWinDataInfoUtil();
                getWinDataInfoUtil.property();
                timer2.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        try {
                           getWinDataInfoUtil.cpu();
                           getWinDataInfoUtil.file();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                },0,30000);



        } catch (Exception e1) {
            e1.printStackTrace();
        }
    }
}
