package com.qm.utils;

import com.sun.scenario.effect.impl.sw.sse.SSEBlend_SRC_OUTPeer;

import java.net.UnknownHostException;
import java.sql.*;

public class JdbcUtils {

    public JdbcUtils() throws UnknownHostException {
    }

    //数据库连接
    public static Connection getConnection() {

        Connection conn = null;//声明连接对象
        String user = "root";
        String password = "root";
        String driver = "com.mysql.jdbc.Driver";// 驱动程序类名
        String url = "jdbc:mysql://192.168.1.77:3306/agent?" // 数据库URL
                + "useUnicode=true&characterEncoding=UTF8";// 防止乱码
        try {
            Class.forName(driver);// 注册(加载)驱动程序
            conn = DriverManager.getConnection(url, user, password);// 获取数据库连接
        } catch (Exception e) {
            e.printStackTrace();
        }
        return conn;
    }
    // 释放数据库连接
    public static void releaseConnection(Connection conn) {
        try {
            if (conn != null)
                conn.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    interface IRowMapper{
        void rowMapper(ResultSet resultSet);
    }
    //查询数据，定义的query方法
    public static void query(Connection conn, String Sql,IRowMapper rowMapper) throws SQLException {
        Statement stmt = conn.createStatement(); //也可以使用PreparedStatement来做
           ResultSet rs = stmt.executeQuery(Sql);//执行sql语句并返还结束
            rowMapper.rowMapper(rs);
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

    }

    //插入数据
    public static void insert(Connection conn,String sqlInsert) {
        try {
            /*String sql = "insert into  sys_data(AppName,Ip,UserName,ComputerName,UserDomain)"
                    + " values ('齐明ruanjian','100010', 'xiaogou', '7000','004')"; // 插入数据的sql语句*/
            Statement stmt1 =conn.createStatement();  // 创建用于执行静态sql语句的Statement对象
            int count = stmt1.executeUpdate(sqlInsert); // 执行插入操作的sql语句，并返回插入数据的个数
            System.out.println( count + " 条数据插入成功"); //输出插入操作的处理结果
           // conn.close();  //关闭数据库连接
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //删除数据
    public static void delete(Connection conn,String sqlDelete) {
        try {
            Statement stmt = conn.createStatement();// 或者用PreparedStatement方法
            stmt.executeUpdate(sqlDelete);//执行sql语句
            if (stmt != null) {

                stmt.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //更新数据
    public static void update(Connection conn,String sqlUpdate){
        try {
            Statement stmt1 = conn.createStatement();//或者用PreparedStatement方法
            stmt1.executeUpdate(sqlUpdate);
            if (stmt1 != null) {

                stmt1.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){

        Connection conn= getConnection(); //获取数据库连接

        class rowMapper implements IRowMapper{

            @Override
            public void rowMapper(ResultSet resultSet) {
                try {
                    while (resultSet.next()){
                        System.out.println(resultSet.getString("name"));
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

        }
        String sql ="select * from test where ip =100010";
        try {
            query(conn,sql,new rowMapper());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        releaseConnection(conn); // 释放数据库连接
    }





}
