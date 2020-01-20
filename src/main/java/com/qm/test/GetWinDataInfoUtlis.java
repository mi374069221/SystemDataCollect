package com.qm.test;

import com.google.gson.Gson;
import com.qm.utils.PropertiesUtils;
import org.hyperic.sigar.*;

import java.io.*;
import java.net.InetAddress;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * 获取服务器信息
 */
public class GetWinDataInfoUtlis {
    Sigar sigar = new Sigar();

    public  Map<String, String> property() throws Exception {
        //获取JVM相关
        Runtime r = Runtime.getRuntime();
        String jvmTotalMemory = String.valueOf(r.totalMemory());
        String jvmFreeMonery = String.valueOf(r.freeMemory());
        String jvmAvailableProcessors = String.valueOf(r.availableProcessors());
        //获取操作系统相关
        Properties props = System.getProperties();
        //主机相关
        InetAddress addr;
        addr = InetAddress.getLocalHost();
        String ip = addr.getHostAddress();

        Map<String, String> map = System.getenv();
        String userName = map.get("USERNAME");// 获取用户名
        String computerName = map.get("COMPUTERNAME");// 获取计算机名
        String userDomain = map.get("USERDOMAIN");// 获取计算机域名

        Map<String,String> allDataMap = new LinkedHashMap<String, String>();
        allDataMap.put("UserName",userName);//用户名
        allDataMap.put("ComputerName",computerName);//计算机名
        allDataMap.put("UserDomain",userDomain);//计算机域名
        allDataMap.put("Ip",ip);//计算机IP
        allDataMap.put("HostName",addr.getHostName());//计算机主机名
        allDataMap.put("JvmTotalMemory",jvmTotalMemory);//JVM可以使用的总内存
        allDataMap.put("JvmFreeMemory",jvmFreeMonery);//JVM可以使用的剩余内存
        allDataMap.put("JvmAvailableProcessors",jvmAvailableProcessors);//JVM可以使用的处理器个数
        allDataMap.put("OsName",props.getProperty("os.name"));//操作系统的名称
        allDataMap.put("OsArch",props.getProperty("os.arch"));//操作系统的架构
        allDataMap.put("OsVersion",props.getProperty("os.version"));//操作系统的版本
        allDataMap.put("UserAccountName",props.getProperty("user.name"));//用户的账户名称
        allDataMap.put("UserAccountHome",props.getProperty("user.home"));//用户的主目录
        allDataMap.put("UserAccountDir", props.getProperty("user.dir"));//用户的当前工作目录
        return allDataMap;
       /* System.out.println("用户名:    " + userName);
        System.out.println("计算机名:    " + computerName);
        System.out.println("计算机域名:    " + userDomain);
        System.out.println("本地ip地址:    " + ip);
        System.out.println("本地主机名:    " + addr.getHostName());
        System.out.println("JVM可以使用的总内存:    " + r.totalMemory());
        System.out.println("JVM可以使用的剩余内存:    " + r.freeMemory());
        System.out.println("JVM可以使用的处理器个数:    " + r.availableProcessors());

        System.out.println("操作系统的名称：    " + props.getProperty("os.name"));
        System.out.println("操作系统的构架：    " + props.getProperty("os.arch"));
        System.out.println("操作系统的版本：    " + props.getProperty("os.version"));

        System.out.println("用户的账户名称：    " + props.getProperty("user.name"));
        System.out.println("用户的主目录：    " + props.getProperty("user.home"));
        System.out.println("用户的当前工作目录：    " + props.getProperty("user.dir"));*/
    }

    public  Map<String, String> memory() throws SigarException {
        Mem mem = sigar.getMem();
        Swap swap = sigar.getSwap();
        //总内存
        String memTotal = mem.getTotal()/1024/1024+"MB";
        //当前内存使用量
        String memUsed = mem.getUsed()/1024/1024+"MB";
        //当前内存剩余量
        String memFree = mem.getFree()/1024/1024+"MB";
        //内存使用率
        String memUseRate =String.valueOf(mem.getUsed()/mem.getTotal()/1024/1024);
        //交换区内存总量
        String swapTotal =swap.getTotal()/1024/1024+"MB";
        //交换区当前内存使用量
        String swapUsed = swap.getUsed()/1024/1024+"MB";
        //交换区当前剩余内存
        String swapFree = swap.getFree()/1024/1024+"MB";
        //交换区内存使用率
        String swapUseRate =String.valueOf(swap.getUsed()/swap.getTotal()/1024/1024);

        Map<String,String> memoryMap = new LinkedHashMap<String, String>();

        memoryMap.put("MemTotal",memTotal);
        memoryMap.put("MemUsed",memUsed);
        memoryMap.put("MemFree",memFree);
        memoryMap.put("MemUseRate",memUseRate);
        memoryMap.put("SwapTotal",swapTotal);
        memoryMap.put("SwapUsed",swapUsed);
        memoryMap.put("SwapFree",swapFree);
        memoryMap.put("SwapUseRate",swapUseRate);

        return memoryMap;
      /*  // 内存总量
        System.out.println("内存总量:    " + mem.getTotal() / 1024L + "K av");
        // 当前内存使用量
        System.out.println("当前内存使用量:    " + mem.getUsed() / 1024L + "K used");
        // 当前内存剩余量
        System.out.println("当前内存剩余量:    " + mem.getFree() / 1024L + "K free");

        // 交换区总量
        System.out.println("交换区总量:    " + swap.getTotal() / 1024L + "K av");
        // 当前交换区使用量
        System.out.println("当前交换区使用量:    " + swap.getUsed() / 1024L + "K used");
        // 当前交换区剩余量
        System.out.println("当前交换区剩余量:    " + swap.getFree() / 1024L + "K free");*/
    }



    public Map<String, String> cpu() throws SigarException {
        CpuInfo infos[] = sigar.getCpuInfoList();
        CpuPerc cpuList[] = null;
        cpuList = sigar.getCpuPercList();
        Map<String, String> CpuMap = new LinkedHashMap<String, String>();
        for (int i = 0; i < infos.length; i++) {// 不管是单块CPU还是多CPU都适用
            CpuInfo info = infos[i];
            /*System.out.println("第" + (i + 1) + "块CPU信息");
            System.out.println("CPU的总量MHz:    " + info.getMhz());// CPU的总量MHz
            System.out.println("CPU生产商:    " + info.getVendor());// 获得CPU的卖主，如：Intel
            System.out.println("CPU类别:    " + info.getModel());// 获得CPU的类别，如：Celeron
            System.out.println("CPU缓存数量:    " + info.getCacheSize());// 缓冲存储器数量*/

            CpuMap.put("CpuTotal_"+(i+1),String.valueOf(info.getMhz()));
            CpuMap.put("CpuVendor_"+(i+1),info.getVendor());
            CpuMap.put("CpuModel_"+(i+1),info.getModel());
            CpuMap.put("CpuCacheSize_"+(i+1),String.valueOf(info.getCacheSize()));
            CpuMap.putAll(printCpuPerc(cpuList[i],i));

        }
        return CpuMap;
    }

    public  Map<String,String> printCpuPerc(CpuPerc cpu,int i) {
        Map cpuMap = new LinkedHashMap();
        cpuMap.put("CpuUserRate_"+(i+1),CpuPerc.format(cpu.getUser()));// 用户使用率
        cpuMap.put("CpuSysRate_"+(i+1),CpuPerc.format(cpu.getSys()));// 系统使用率
        cpuMap.put("CpuWaitRate_"+(i+1),CpuPerc.format(cpu.getWait()));// 当前等待率
        cpuMap.put("CpuErrRate_"+(i+1),CpuPerc.format(cpu.getNice()));//当前错误率
        cpuMap.put("CpufreeRate_"+(i+1),CpuPerc.format(cpu.getIdle()));// 当前空闲率
        cpuMap.put("CpuTotalRate_"+(i+1),CpuPerc.format(cpu.getCombined()));// 总的使用率
        return cpuMap;
        /*System.out.println("CPU用户使用率:    " + CpuPerc.format(cpu.getUser()));// 用户使用率
        System.out.println("CPU系统使用率:    " + CpuPerc.format(cpu.getSys()));// 系统使用率
        System.out.println("CPU当前等待率:    " + CpuPerc.format(cpu.getWait()));// 当前等待率
        System.out.println("CPU当前错误率:    " + CpuPerc.format(cpu.getNice()));//当前错误率
        System.out.println("CPU当前空闲率:    " + CpuPerc.format(cpu.getIdle()));// 当前空闲率
        System.out.println("CPU总的使用率:    " + CpuPerc.format(cpu.getCombined()));// 总的使用率*/
    }

    public  Map<String, String> os() {
        OperatingSystem OS = OperatingSystem.getInstance();
        Map<String,String> osMaP = new LinkedHashMap<String, String>();
        //操作系统内核类型
        osMaP.put("OsSystem",OS.getArch());//操作系统
        osMaP.put("CpuEndian",OS.getCpuEndian());
        osMaP.put("DataModel",OS.getDataModel());
        // 操作系统类型
        osMaP.put("OsName",OS.getName());
        osMaP.put("OsPatchLevel",OS.getPatchLevel());//补丁级别
        // 操作系统的供应商编号
        osMaP.put("OsVendorCodeName",OS.getVendorCodeName());
        // 操作系统的版本号
        osMaP.put("OsVersion",OS.getVersion());
        return osMaP;

        // 操作系统内核类型如： 386、486、586等x86
       /* System.out.println("操作系统:    " + OS.getArch());
        System.out.println("操作系统CpuEndian():    " + OS.getCpuEndian());//
        System.out.println("操作系统DataModel():    " + OS.getDataModel());//
        // 系统描述
        System.out.println("操作系统的描述:    " + OS.getDescription());
        // 操作系统类型
        System.out.println("OS.getName():    " + OS.getName());
        System.out.println("OS.getPatchLevel():    " + OS.getPatchLevel());//

        // 操作系统名称
        System.out.println("操作系统名称:    " + OS.getVendorName());

        // 操作系统的版本号
        System.out.println("操作系统的版本号:    " + OS.getVersion());*/
    }

    public  Map<String, String> who() throws SigarException {
        Who who[] = sigar.getWhoList();
        Map<String,String> whoMap = new LinkedHashMap<String, String>();
        if (who != null && who.length > 0) {
            for (int i = 0; i < who.length; i++) {
                Who _who = who[i];
                whoMap.put("ExecUserName_"+(i+1),_who.getUser());//当前系统进程表中的用户名
                whoMap.put("UserDevice_"+(i+1),_who.getDevice());//用户终端
                whoMap.put("UserHost_"+(i+1),_who.getHost());//用户host

                // 当前系统进程表中的用户名
               /* System.out.println("当前系统进程表中的用户名:    " + _who.getUser());
                System.out.println("用户终端:    " + _who.getDevice());
                System.out.println("用户host:    " + _who.getHost());
                System.out.println("getTime():    " + _who.getTime());*/

            }
        }
        return whoMap;
    }

    public  Map<String, String> file() throws Exception {
        FileSystem fslist[] = sigar.getFileSystemList();
        Map<String,String> fileMap = new LinkedHashMap<String, String>();
        for (int i = 0; i < fslist.length; i++) {
            FileSystem fs = fslist[i];
            fileMap.put("DevName_"+fs.getDirName().substring(0,1),fs.getDevName());//盘符名称
            fileMap.put("DirName_"+fs.getDirName().substring(0,1),fs.getDirName());//盘符路径
            fileMap.put("FileFlags_"+fs.getDirName().substring(0,1),String.valueOf(fs.getFlags()));//盘符标志
            fileMap.put("SysTypeName_"+fs.getDirName().substring(0,1),fs.getSysTypeName());//盘符类型
            fileMap.put("FileTypeName_"+fs.getDirName().substring(0,1),fs.getTypeName());//盘符类型名
            fileMap.put("FileType_"+fs.getDirName().substring(0,1),String.valueOf(fs.getType()));// 文件系统类型


            /*System.out.println("分区的盘符名称" + i);
            // 分区的盘符名称
            System.out.println("盘符名称:    " + fs.getDevName());
            System.out.println("盘符路径:    " + fs.getDirName());
            System.out.println("盘符标志:    " + fs.getFlags());//
            // 文件系统类型，比如 FAT32、NTFS
            System.out.println("盘符类型:    " + fs.getSysTypeName());
            // 文件系统类型名，比如本地硬盘、光驱、网络文件系统等
            System.out.println("盘符类型名:    " + fs.getTypeName());
            // 文件系统类型
            System.out.println("盘符文件系统类型:    " + fs.getType());*/

            switch (fs.getType()) {
                case 0: // TYPE_UNKNOWN ：未知
                    break;
                case 1: // TYPE_NONE
                    break;
                case 2: // TYPE_LOCAL_DISK : 本地硬盘
                    FileSystemUsage usage = sigar.getFileSystemUsage(fs.getDirName());
                    double usePercent = usage.getUsePercent() * 100D;
                    DecimalFormat df = new DecimalFormat("0.00");//格式化小数
                    fileMap.put(fs.getDirName()+"TotalDisk",df.format(usage.getTotal()/1024/1024) + "GB");//总大小
                    fileMap.put(fs.getDirName()+"FreeDisk",df.format(usage.getFree()/1024/1024) + "GB");//剩余大小
                    fileMap.put(fs.getDirName()+"AvailDisk",df.format(usage.getAvail()/1024/1024) + "GB");//可用大小
                    fileMap.put(fs.getDirName()+"UsedDisk",df.format(usage.getUsed()/1024/1024) + "GB");//已经使用量
                    fileMap.put(fs.getDirName()+"PercentDisk",usePercent + "%");//资源的利用率
                    fileMap.put(fs.getDirName()+"ReadDisk",String.valueOf(usage.getDiskReads()));//读出个数
                    fileMap.put(fs.getDirName()+"WriteDisk",String.valueOf(usage.getDiskWrites()));//写入个数

                    /*// 文件系统总大小
                    System.out.println(fs.getDevName() + "总大小:    " + usage.getTotal() + "KB");
                    // 文件系统剩余大小
                    System.out.println(fs.getDevName() + "剩余大小:    " + usage.getFree() + "KB");
                    // 文件系统可用大小
                    System.out.println(fs.getDevName() + "可用大小:    " + usage.getAvail() + "KB");
                    // 文件系统已经使用量
                    System.out.println(fs.getDevName() + "已经使用量:    " + usage.getUsed() + "KB");

                    // 文件系统资源的利用率
                    System.out.println(fs.getDevName() + "资源的利用率:    " + usePercent + "%");

                    System.out.println(fs.getDevName() + "读出：    " + usage.getDiskReads());
                    System.out.println(fs.getDevName() + "写入：    " + usage.getDiskWrites());*/
                    break;
                case 3:// TYPE_NETWORK ：网络
                    break;
                case 4:// TYPE_RAM_DISK ：闪存
                    break;
                case 5:// TYPE_CDROM ：光驱
                    break;
                case 6:// TYPE_SWAP ：页面交换
                    break;
            }

        }
        return fileMap;
    }

    public  Map<String, String> net() throws Exception {
        String ifNames[] = sigar.getNetInterfaceList();
        Map<String,String> netMap = new LinkedHashMap<String, String>();
        for (int i = 0; i < ifNames.length; i++) {
            String name = ifNames[i];
            NetInterfaceConfig ifconfig = sigar.getNetInterfaceConfig(name);
            if (NetFlags.LOOPBACK_ADDRESS.equals(ifconfig.getAddress()) || (ifconfig.getFlags() & NetFlags.IFF_LOOPBACK) != 0
                    || NetFlags.NULL_HWADDR.equals(ifconfig.getHwaddr())) {
                continue;
            }
            netMap.put("NetName_"+name,name);// 网络设备名
            netMap.put("IpAddr_"+name,ifconfig.getAddress());// IP地址
            netMap.put("NetMask_"+name,ifconfig.getNetmask());// 子网掩码
            netMap.put("NetBroadcast_"+name,ifconfig.getBroadcast());// 网关广播地址
            netMap.put("NetMacAddr_"+name,ifconfig.getHwaddr());// 网卡MAC地址
            netMap.put("NetDescription_"+name,ifconfig.getDescription());// 网卡描述信息
            netMap.put("NetType_"+name,ifconfig.getType());//网卡类型
            /*System.out.println("网络设备名:    " + name);// 网络设备名
            System.out.println("IP地址:    " + ifconfig.getAddress());// IP地址
            System.out.println("子网掩码:    " + ifconfig.getNetmask());// 子网掩码
            System.out.println(ifconfig.getName() + "网关广播地址:" + ifconfig.getBroadcast());// 网关广播地址
            System.out.println(ifconfig.getName() + "网卡MAC地址:" + ifconfig.getHwaddr());// 网卡MAC地址
            System.out.println(ifconfig.getName() + "网卡描述信息:" + ifconfig.getDescription());// 网卡描述信息
            System.out.println(ifconfig.getName() + "网卡类型" + ifconfig.getType());//网卡类型*/
            if ((ifconfig.getFlags() & 1L) <= 0L) {
               // System.out.println(name+"网络数据不存在");
                continue;
            }
            NetInterfaceStat ifstat = sigar.getNetInterfaceStat(name);
            netMap.put("RxPackets_"+name,String.valueOf(ifstat.getRxPackets()));// 接收的总包裹数
            netMap.put("TxPackets_"+name,String.valueOf(ifstat.getTxPackets()));// 发送的总包裹数
            netMap.put("RxBytes_"+name,String.valueOf(ifstat.getRxBytes()));// 接收到的总字节数
            netMap.put("TxByte_"+name,String.valueOf(ifstat.getTxBytes()));// 发送的总字节数
            netMap.put("RxErrors_"+name,String.valueOf(ifstat.getRxErrors()));// 接收到的错误包数
            netMap.put("TxErrors_"+name,String.valueOf(ifstat.getTxErrors()));// 发送数据包时的错误数
            netMap.put("RxDropped_"+name,String.valueOf(ifstat.getRxDropped()));// 接收时丢弃的包数
            netMap.put("TxDropped_"+name,String.valueOf(ifstat.getTxDropped()));// 发送时丢弃的包数
            /*System.out.println(name + "接收的总包裹数:" + ifstat.getRxPackets());// 接收的总包裹数
            System.out.println(name + "发送的总包裹数:" + ifstat.getTxPackets());// 发送的总包裹数
            System.out.println(name + "接收到的总字节数:" + ifstat.getRxBytes());// 接收到的总字节数
            System.out.println(name + "发送的总字节数:" + ifstat.getTxBytes());// 发送的总字节数
            System.out.println(name + "接收到的错误包数:" + ifstat.getRxErrors());// 接收到的错误包数
            System.out.println(name + "发送数据包时的错误数:" + ifstat.getTxErrors());// 发送数据包时的错误数
            System.out.println(name + "接收时丢弃的包数:" + ifstat.getRxDropped());// 接收时丢弃的包数
            System.out.println(name + "发送时丢弃的包数:" + ifstat.getTxDropped());// 发送时丢弃的包数*/
        }
        return netMap;
    }


    public void saveDataToServer()  {
        try {
         /*一：获取服务器数据*/
        //建立一个output.txt文件
        //File writeName = new File("G:\\data.txt");
        GetWinDataInfoUtlis getWinDataInfo = new GetWinDataInfoUtlis();
        Map<String, String> propertyMap = getWinDataInfo.property();
        // System.out.println(propertyMap.get("Ip"));
        String dataType = PropertiesUtils.getPropertiesFromUserDir("server.properties").getProperty("sys.data.type");
        String dataPath = PropertiesUtils.getPropertiesFromUserDir("config.properties").getProperty("sys.data.path")+
                          File.separator+
                          PropertiesUtils.getPropertiesFromUserDir("config.properties").getProperty("app.name")+"_"+
                          propertyMap.get("Ip")+"."+dataType;
//        String value = new String(dataPath.getBytes("iso-8859-1"),"utf-8"); //解决中文文件名乱码问题
        //System.out.println(value);
        File writeName = new File(dataPath);

            if(!writeName.exists()) {
            // 创建新文件,有同名的文件的话直接覆盖
            writeName.createNewFile();
        }
        /*FileWriter writer = new FileWriter(writeName);
        BufferedWriter out = new BufferedWriter(writer);*/
        //解决文件内部中文乱码问题
      //BufferedWriter out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(writeName, true),"UTF-8"));
            OutputStream out = new FileOutputStream(writeName,true);

            //创建json对象
        Gson json = new Gson();
        // System信息，从jvm获取
        String sittingPath =  PropertiesUtils.getPropertiesFromUserDir("server.properties").getProperty("sitting.path");
        System.setProperty("java.library.path", sittingPath);

        String name= PropertiesUtils.getPropertiesFromUserDir("config.properties").getProperty("app.name");
        String appName = new String(name.getBytes("iso-8859-1"),"utf-8");
        Map<String,String> appMap = new HashMap<String, String>();
        appMap.put("AppName",appName);
        out.write(json.toJson(appMap).getBytes());
         out.write(System.getProperty("line.separator").getBytes());//换行
        //将基本数据转换为json格式并写入文件
        out.write(json.toJson(propertyMap).getBytes());
            out.write(System.getProperty("line.separator").getBytes());//换行
            /*for (String s : property.keySet()) {
                System.out.println(s+":"+property.get(s));
                //out.write(s+":"+property.get(s));
            }*/

        Map<String, String> memoryMap = getWinDataInfo.memory();
        //将内存数据转换为json格式并写入文件
        out.write(json.toJson(memoryMap).getBytes());
        out.write(System.getProperty("line.separator").getBytes());//换行

        Map<String, String> cpuMap = getWinDataInfo.cpu();
        //将cpu数据转换为json格式并写入文件
        out.write(json.toJson(cpuMap).getBytes());
            out.write(System.getProperty("line.separator").getBytes());//换行

        Map<String, String> osMap = getWinDataInfo.os();
        //将操作系统数据转换为json格式并写入文件
        out.write(json.toJson(osMap).getBytes());
            out.write(System.getProperty("line.separator").getBytes());//换行

        Map<String, String> whoMap = getWinDataInfo.who();
        //将用户数据转换为json并写入文件
        out.write(json.toJson(whoMap).getBytes());
            out.write(System.getProperty("line.separator").getBytes());//换行

        Map<String, String> fileMap = getWinDataInfo.file();
        //将磁盘信息转换为json并写入文件
        out.write(json.toJson(fileMap).getBytes());
            out.write(System.getProperty("line.separator").getBytes());//换行

        Map<String, String> netMap = getWinDataInfo.net();
        //将网络数据转换为json并写入文件
        out.write(json.toJson(netMap).getBytes());
            out.write(System.getProperty("line.separator").getBytes());//换行

        out.flush(); // 把缓存区内容压入文件
        out.close();
        System.out.println("数据写入完成。");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        GetWinDataInfoUtlis g = new GetWinDataInfoUtlis();

    }
}
