package com.qm.utils;

import org.hyperic.sigar.*;

import java.net.InetAddress;
import java.sql.Connection;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

/**
 * 获取服务器信息
 */
public class GetWinDataInfoUtil {
    Sigar sigar = new Sigar();
    Connection conn = JdbcUtils.getConnection();
    String appId = PropertiesUtils.getPropertiesFromUserDir("config.properties").getProperty("app.id");
    String appName = PropertiesUtils.getPropertiesFromUserDir("config.properties").getProperty("app.name");
    public  void property() throws Exception {
        //获取JVM相关
        Runtime r = Runtime.getRuntime();
        String jvmTotalMemory = String.valueOf(r.totalMemory());
        String jvmFreeMonery = String.valueOf(r.freeMemory());
        String jvmAvailableProcessors = String.valueOf(r.availableProcessors());
        //获取操作系统相关
        Properties props = System.getProperties();
        OperatingSystem OS = OperatingSystem.getInstance();
        //主机IP
        String ip = String.join(",", LocalHostUtil.getLocalIPs());
        InetAddress addr = InetAddress.getLocalHost();


        Map<String, String> map = System.getenv();
        String userName = map.get("USERNAME");// 获取用户名
        String computerName = map.get("COMPUTERNAME");// 获取计算机名
        String userDomain = map.get("USERDOMAIN");// 获取计算机域名

        //执行sql================
        String sqlDelete = "delete from sys_data where AppId='"+appId+"'";
        JdbcUtils.delete(conn,sqlDelete);
        String sqlInsert = "insert into sys_data(AppId,AppName,Ip,UserName,ComputerName,UserDomain,HostName,JvmTotalMemory," +
                "JvmFreeMemory,JvmAvailableProcessors,OsName,OsArch,OsVersion,UserAccountName,UserAccountHome,UserAccountDir," +
                "OsSystem,CpuEndian,DataModel,OsType,OsPatchLevel,OsVendorCodeName)" +
                "values('"+appId+"','"+appName+"','"+ip+"','"+userName+"','"+computerName+"','"+userDomain+"','"+addr.getHostName()+"'," +
                "'"+jvmTotalMemory+"','"+jvmFreeMonery+"','"+jvmAvailableProcessors+"','"+props.getProperty("os.name")+"'," +
                "'"+props.getProperty("os.arch")+"','"+props.getProperty("os.version")+"','"+props.getProperty("user.name")+"'," +
                "'"+props.getProperty("user.home")+"','"+props.getProperty("user.dir")+"','"+OS.getArch()+"','"+OS.getCpuEndian()+"'," +
                "'"+OS.getDataModel()+"','"+OS.getName()+"','"+OS.getPatchLevel()+"','"+OS.getVendorCodeName()+"')";
        JdbcUtils.insert(conn,sqlInsert);
        //执行sql结束==================
    }


    public void cpu() throws SigarException {
        CpuInfo infos[] = sigar.getCpuInfoList();
        CpuPerc[] cpuList = sigar.getCpuPercList();
        //获取总的cpu使用情况
        CpuPerc perc = sigar.getCpuPerc();
        DecimalFormat df = new DecimalFormat("0.00");//格式化小数
        String cpuIdleRate = String.valueOf(df.format(perc.getIdle()));
        String cpuUserRate = String.valueOf(df.format(perc.getUser()));
        String cpuSysRate = String.valueOf(df.format(perc.getSys()));
        String cpuErrRate = String.valueOf(df.format(perc.getNice()));
        String cpuWaitRate = String.valueOf(df.format(perc.getWait()));

        Map<String, String> cpuMap = new LinkedHashMap<String, String>();
        for (int i = 0; i < infos.length; i++) {// 不管是单块CPU还是多CPU都适用
            CpuInfo info = infos[i];
           /* System.out.println("第" + (i + 1) + "块CPU信息");
            System.out.println("CPU的总量MHz:    " + info.getMhz());// CPU的总量MHz
            System.out.println("CPU生产商:    " + info.getVendor());// 获得CPU的卖主，如：Intel
            System.out.println("CPU类别:    " + info.getModel());// 获得CPU的类别，如：Celeron
            System.out.println("CPU缓存数量:    " + info.getCacheSize());// 缓冲存储器数量*/
            cpuMap.put("CpuMHz_"+(i+1),String.valueOf(info.getMhz()));
            cpuMap.put("CpuModel_"+(i+1),info.getModel());
            cpuMap.put("CpuWaitRate_"+(i+1),CpuPerc.format(cpuList[i].getWait()));// 当前等待率
            cpuMap.put("CpuNiceRate_"+(i+1),CpuPerc.format(cpuList[i].getNice()));//当前错误率
            cpuMap.put("CpuIdleRate_"+(i+1),CpuPerc.format(cpuList[i].getCombined()));// 总的使用率
        }
        //sql开始
        String sqlDelete = "delete from cpu where AppId = '"+appId+"'";
        JdbcUtils.delete(conn,sqlDelete);
        String sqlInsert = "insert into cpu (AppId,CpuIdleRate,CpuUserRate,CpuSysRate,CpuNiceRate,CpuWaitRate)" +
                "values('"+appId+"','"+cpuIdleRate+"','"+cpuUserRate+"','"+cpuSysRate+"','"+cpuErrRate+"','"+cpuWaitRate+"')";
        JdbcUtils.insert(conn,sqlInsert);


        //执行更新
        for (Map.Entry<String, String> entry : cpuMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            String sqlUpdate = "update cpu set "+key+"='"+value+"' where AppId= '"+appId+"'";
            JdbcUtils.update(conn,sqlUpdate);
        }
        //sql结束
    }


    public  void file() throws Exception {
        Mem mem = sigar.getMem();
        // 创建一个数值格式化对象
        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);

        //总内存
        String memTotal =numberFormat.format((float)mem.getTotal()/1024/1024/1024)+"GB";
        //当前内存使用量
        String memUsed = numberFormat.format((float)mem.getUsed()/1024/1024/1024)+"GB";
        //当前内存剩余量
        String memFree =numberFormat.format((float) mem.getFree()/1024/1024/1024)+"GB";
        //内存使用率
        long total = mem.getTotal();
        long used = mem.getUsed();
        String  memUseRate = numberFormat.format((float)used/(float)total*100)+"%";

        //==============================================================
        FileSystem fslist[] = sigar.getFileSystemList();
        Map<String,String> fileMap = new LinkedHashMap<String, String>();
        for (int i = 0; i < fslist.length; i++) {
            FileSystem fs = fslist[i];
            String fsDirName = fs.getDirName();
            fileMap.put("DirName_"+(i+1), fsDirName.replaceAll("\\\\",""));//盘符路径


            switch (fs.getType()) {
                case 0: // TYPE_UNKNOWN ：未知
                    break;
                case 1: // TYPE_NONE
                    break;
                case 2: // TYPE_LOCAL_DISK : 本地硬盘
                    FileSystemUsage usage = sigar.getFileSystemUsage(fsDirName);
                    DecimalFormat df = new DecimalFormat("0.00");//格式化小数
                    String totalDisk = df.format(usage.getTotal() / 1024 / 1024) + "GB";
                    String freeDisk = df.format(usage.getFree() / 1024 / 1024) + "GB";
                    String availDisk = df.format(usage.getAvail() / 1024 / 1024) + "GB";
                    String userDisk = df.format(usage.getUsed() / 1024 / 1024) + "GB";

                    fileMap.put("TotalDisk_"+(i+1), totalDisk);//总大小
                    fileMap.put("FreeDisk_"+(i+1), freeDisk);//剩余大小
                    fileMap.put("AvailDisk_"+(i+1), availDisk);//可用大小
                    fileMap.put("UsedDisk_"+(i+1), userDisk);//已经使用量

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
        //执行sql开始
        String sqlDelete = "delete from mem_dir where AppId='"+appId+"'";
        JdbcUtils.delete(conn,sqlDelete);
        String sqlInsert = "insert into mem_dir(AppId,MemTotal,MemUsed,MemFree,MemUseRate)" +
                " values('"+appId+"','"+ memTotal +"','"+memUsed+"','"+memFree+"','"+memUseRate+"')";
        JdbcUtils.insert(conn,sqlInsert);

        for (Map.Entry<String, String> entry : fileMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            //执行更新操作
            String sqlUpdate = "update mem_dir set "+key+"='"+value+"' where AppId= '"+appId+"'";
            JdbcUtils.update(conn,sqlUpdate);

        }

    }

    public  void net() throws Exception {
        String ifNames[] = sigar.getNetInterfaceList();
        Map<String,String> netMap = new LinkedHashMap<String, String>();
        for (int i = 0; i < ifNames.length; i++) {
            String name = ifNames[i];
            NetInterfaceConfig ipconfigs = sigar.getNetInterfaceConfig(name);
            if (NetFlags.LOOPBACK_ADDRESS.equals(ipconfigs.getAddress()) || (ipconfigs.getFlags() & NetFlags.IFF_LOOPBACK) != 0
                    || NetFlags.NULL_HWADDR.equals(ipconfigs.getHwaddr())||ipconfigs.getAddress().equals("0.0.0.0")) {
                continue;
            }
            netMap.put("NetName_"+name,name);// 网络设备名
            netMap.put("IpAddr_"+name,ipconfigs.getAddress());// IP地址
            netMap.put("NetMask_"+name,ipconfigs.getNetmask());// 子网掩码
            netMap.put("NetBroadcast_"+name,ipconfigs.getBroadcast());// 网关广播地址
            netMap.put("NetMacAddr_"+name,ipconfigs.getHwaddr());// 网卡MAC地址
            netMap.put("NetDescription_"+name,ipconfigs.getDescription());// 网卡描述信息
            netMap.put("NetType_"+name,ipconfigs.getType());//网卡类型

            System.out.println("网络设备名:    " + name);// 网络设备名
            System.out.println("IP地址:    " + ipconfigs.getAddress());// IP地址
            System.out.println("子网掩码:    " + ipconfigs.getNetmask());// 子网掩码
            System.out.println(ipconfigs.getName() + "网关广播地址:" + ipconfigs.getBroadcast());// 网关广播地址
            System.out.println(ipconfigs.getName() + "网卡MAC地址:" + ipconfigs.getHwaddr());// 网卡MAC地址
            System.out.println(ipconfigs.getName() + "网卡描述信息:" + ipconfigs.getDescription());// 网卡描述信息
            System.out.println(ipconfigs.getName() + "网卡类型" + ipconfigs.getType());//网卡类型
            if ((ipconfigs.getFlags() & 1L) <= 0L) {
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

        }

    }


    public void saveDataToServer()  {

        try {
            property();
            file();
            cpu();
            net();
            JdbcUtils.releaseConnection(conn);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {
        GetWinDataInfoUtil g = new GetWinDataInfoUtil();
        g.saveDataToServer();
    }
}
