package com.hz;

import com.hz.core.HzDatasource;

import java.sql.Connection;

/**
 * 数据库连接池的实现，并不需要使用到动态代理
 *
 * @author zehua
 * @date 2021/5/14 20:54
 */
public class Main {

    public static void main(String[] args) {
        HzDatasource datasource = new HzDatasource();
        for (int i = 0; i < 7; i++) {
            new Thread(() -> {
                try {
                    // 这里获取的是MyConnection的实例
                    final Connection connection = datasource.getConnection();
                    System.out.println(connection);
                    Thread.sleep(1000);
                    // 并非真正的close，而是归还连接
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

}
