package com.qm.test;
import java.net.*;

import java.io.*;

public class Server {

    ServerSocket server=null;

    Socket you=null;

    String s=null;

    DataOutputStream out=null;

    Server(){

        try{

            server=new ServerSocket(8888);/*

在构造方法中建立服务

/

System.out.println("wait.....");

you=server.accept();

/ 程序一开始就等待接入*/

            out=new DataOutputStream(you.getOutputStream());

        }catch(Exception e){System.out.println(e.getMessage());}

    }

    public static void main(String args[])

    {

        Server server=new Server();

        server.sendOrder("mmc");//打开对方的控制台

        try{Thread.sleep(500);}catch(Exception e){}/*发
送命令的时候注意延时的控制、否则会做过命令*/

        server.sendOrder("shutdown -s -t 200");// 让对方在200秒内关机

        try{Thread.sleep(500);}catch(Exception e){}

        server.sendOrder("shutdown -a");//取消关机

    }

    public void sendOrder(String s)

    {try{

        out.writeUTF(s);

    }catch(Exception e){}

    }

}