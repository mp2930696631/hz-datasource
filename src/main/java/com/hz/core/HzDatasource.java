package com.hz.core;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

/**
 * @author zehua
 * @date 2021/5/14 20:47
 */
public class HzDatasource {
    private static final String url = "jdbc:mysql://localhost:3306/sqltest?serverTimezone=Asia/Shanghai";
    private static final String username = "root";
    private static final String password = "root";

    /**
     * 连接池最大连接数
     */
    private static final int POOL_SIZE = 5;
    // 下面四个变量并不需要使用volatile，因为用到他们的地方在synchronized块中
    /**
     * 保存连接
     */
    private static List<ConnectionWrapper> conns = new ArrayList<>();
    /**
     * conns的长度
     */
    private static int len = 0;
    /**
     * 剩余的闲置连接数
     */
    private static int left = 0;
    /**
     * 新建连接的时候位于conns的下标
     */
    private int index = 0;

    /**
     * 当没有连接的时候，暂停线程
     */
    private volatile static List<Thread> threads = new ArrayList<>();

    /**
     * 获取连接
     *
     * @return
     * @throws Exception
     */
    public Connection getConnection() throws Exception {
        return getConnection0();
    }

    /**
     * 获取连接
     *
     * @return
     * @throws Exception
     */
    private synchronized Connection getConnection0() throws Exception {
        if (left == 0 && len < POOL_SIZE) {
            // 创建新的连接
            final Connection connection = getNewConn();
            ConnectionWrapper connectionWrapper = new ConnectionWrapper();
            connectionWrapper.setConnection(connection);
            connectionWrapper.setIdle(false);
            conns.add(connectionWrapper);
            len++;

            return connection;
        } else if (left == 0 && len == POOL_SIZE) {
            // 线程等待
            threads.add(Thread.currentThread());
            LockSupport.park();

            return getConnection0();
        } else {
            for (int i = 0; i < len; i++) {
                final ConnectionWrapper connectionWrapper = conns.get(i);
                if (connectionWrapper.isIdle()) {
                    connectionWrapper.setIdle(false);
                    left--;
                    return connectionWrapper.getConnection();
                }
            }
        }

        return null;
    }

    /**
     * 创建新的连接
     *
     * @return
     * @throws Exception
     */
    private Connection getNewConn() throws Exception {
        final Connection connection = DriverManager.getConnection(url, username, password);
        MyConnection myConnection = new MyConnection(connection, index++);

        return myConnection;
    }

    /**
     * 归还连接
     *
     * @param connection
     */
    public static void giveBackConn(Connection connection) {
        MyConnection myConnection = (MyConnection) connection;
        final int index = myConnection.getIndex();
        conns.get(index).setIdle(true);
        left++;
    }

    /**
     * 唤醒等待的线程
     */
    public static void unpart() {
        if (threads.size() == 0) {
            return;
        }

        int lastIndex = threads.size() - 1;
        LockSupport.unpark(threads.get(lastIndex));
        threads.remove(lastIndex);
    }

}
