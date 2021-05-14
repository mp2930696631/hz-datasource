package com.hz.core;

import java.sql.Connection;

/**
 * connection的包装类
 * idle是当前连接是否闲置的标识
 *
 * @author zehua
 * @date 2021/5/14 20:49
 */
public class ConnectionWrapper {
    private boolean isIdle;
    private Connection connection;

    public boolean isIdle() {
        return isIdle;
    }

    public void setIdle(boolean idle) {
        isIdle = idle;
    }

    public Connection getConnection() {
        return connection;
    }

    public void setConnection(Connection connection) {
        this.connection = connection;
    }
}
