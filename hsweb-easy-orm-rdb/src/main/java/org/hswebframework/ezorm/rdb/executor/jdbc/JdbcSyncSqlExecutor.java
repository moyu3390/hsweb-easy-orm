package org.hswebframework.ezorm.rdb.executor.jdbc;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.hswebframework.ezorm.rdb.executor.SimpleSqlRequest;
import org.hswebframework.ezorm.rdb.executor.SqlRequest;
import org.hswebframework.ezorm.rdb.executor.SyncSqlExecutor;
import org.hswebframework.ezorm.rdb.executor.wrapper.ResultWrapper;

import java.io.ByteArrayInputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.hswebframework.ezorm.rdb.executor.jdbc.JdbcSqlExecutorHelper.*;

@Slf4j
public abstract class JdbcSyncSqlExecutor extends JdbcSqlExecutor implements SyncSqlExecutor {

    public abstract Connection getConnection(SqlRequest sqlRequest);

    public abstract void releaseConnection(Connection connection, SqlRequest sqlRequest);

    @SneakyThrows
    public void releaseStatement(Statement statement) {
        statement.close();
    }

    @SneakyThrows
    public void releaseResultSet(ResultSet resultSet) {
        resultSet.close();
    }

    @Override
    @SneakyThrows
    public int update(SqlRequest request) {
        printSql(log, request);
        Connection connection = getConnection(request);
        try {
            return doUpdate(connection, request);
        } finally {
            releaseConnection(connection, request);
        }
    }

    @Override
    @SneakyThrows
    public void execute(SqlRequest request) {
        printSql(log, request);

        Connection connection = getConnection(request);
        try {
              doExecute(connection, request);
        } finally {
            releaseConnection(connection, request);
        }
    }

    @Override
    @SneakyThrows
    public <T, R> R select(SqlRequest request, ResultWrapper<T, R> wrapper) {
        printSql(log, request);

        Connection connection = getConnection(request);
        try {
            return doSelect(connection, request, wrapper);
        } finally {
            releaseConnection(connection, request);
        }
    }

}