package og.hsweb.ezorm.run;

import og.hsweb.ezorm.meta.DatabaseMetaData;
import og.hsweb.ezorm.meta.TableMetaData;
import og.hsweb.ezorm.meta.parser.TableMetaParser;

import java.sql.SQLException;

/**
 * 数据库操作接口
 * Created by zhouhao on 16-6-4.
 */
public interface Database {

    /**
     * 获取数据库定义对象
     *
     * @return 数据库定义对象
     */
    DatabaseMetaData getMeta();

    /**
     * 获取一个表操作接口,如果数据库定义对象里未找到表结构定义,则尝试使用{@link TableMetaParser#parse(String)}进行解析
     *
     * @param name 表名
     * @param <T>  表数据泛型
     * @return 表操作接口
     */
    <T> Table<T> getTable(String name);

    /**
     * 创建表,在数据库中创建表,如果表已存在,将不进行任何操作
     *
     * @param tableMetaData 表结构定义
     * @param <T>           表数据泛型
     * @return 表操作接口
     * @throws SQLException 创建异常信息
     */
    <T> Table<T> createTable(TableMetaData tableMetaData) throws SQLException;

    /**
     * 重新载入结构定义,此操作不会对数据库表结构进行任何操作
     *
     * @param tableMetaData 表结构定义
     * @param <T>           表数据泛型
     * @return 表操作接口
     */
    <T> Table<T> reloadTable(TableMetaData tableMetaData);

    <T> Table<T> alterTable(TableMetaData tableMetaData);

    /**
     * 删除表,此操作只会删除结构定义,不会删除物理数据库中的表
     *
     * @param name 表名
     * @return
     */
    boolean removeTable(String name);

}
