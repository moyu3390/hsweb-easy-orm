package og.hsweb.ezorm.render.support.simple;

import og.hsweb.ezorm.executor.SQL;
import og.hsweb.ezorm.meta.Correlation;
import og.hsweb.ezorm.meta.FieldMetaData;
import og.hsweb.ezorm.meta.TableMetaData;
import og.hsweb.ezorm.param.QueryParam;
import og.hsweb.ezorm.param.Sort;
import og.hsweb.ezorm.render.Dialect;
import og.hsweb.ezorm.render.SqlAppender;

import java.util.*;

/**
 * Created by zhouhao on 16-5-17.
 */
public class SimpleSelectSqlRender extends CommonSqlRender<QueryParam> {

    private Dialect dialect;

    public SimpleSelectSqlRender(Dialect dialect) {
        this.dialect = dialect;
    }

    public Dialect getDialect() {
        return dialect;
    }

    public void setDialect(Dialect dialect) {
        this.dialect = dialect;
    }

    class SimpleSelectSqlRenderProcess extends SimpleWhereSqlBuilder {
        private TableMetaData metaData;
        private QueryParam param;
        private List<OperationField> selectField;
        private SqlAppender whereSql = new SqlAppender();
        private Set<String> needSelectTable = new LinkedHashSet<>();
        private List<Sort> sorts = new ArrayList<>();

        public SimpleSelectSqlRenderProcess(TableMetaData metaData, QueryParam param) {
            this.metaData = metaData;
            this.param = param;
            //解析要查询的字段
            this.selectField = parseOperationField(metaData, param);
            //解析查询条件
            buildWhere(metaData, "", param.getTerms(), whereSql, needSelectTable);
            if (!whereSql.isEmpty()) whereSql.removeFirst();
            //加入要查询的表
            this.selectField.stream().forEach(field -> {
                needSelectTable.add(field.getTableName());
            });
            param.getSorts().forEach(sort -> {
                FieldMetaData fieldMetaData = metaData.findFieldByName(sort.getField());
                if (fieldMetaData.getName() == null) return;
                String tableName = getTableAlias(metaData, sort.getField());
                needSelectTable.add(tableName);
                sort.setField(tableName + "." + fieldMetaData.getName());
                sorts.add(sort);
            });
        }

        public SQL process() {
            SqlAppender appender = new SqlAppender();
            appender.add("SELECT ");
            if (selectField.isEmpty()) appender.add(" * ");
            selectField.forEach(operationField -> {
                FieldMetaData fieldMetaData = operationField.getFieldMetaData();
                String tableName = fieldMetaData.getTableMetaData().getName();
                Correlation correlation = metaData.getCorrelation(tableName);
                if (correlation == null) {
                    appender.add(operationField.getTableName(), ".", fieldMetaData.getName(), " AS "
                            , dialect.getQuoteStart()
                            , fieldMetaData.getAlias()
                            , dialect.getQuoteEnd());
                } else {
                    //关联的另外一张表
                    if (correlation.isOne2one()) {
                        appender.add(operationField.getTableName(), ".", fieldMetaData.getName(), " AS "
                                , dialect.getQuoteStart()
                                , operationField.getTableName(), ".", fieldMetaData.getAlias()
                                , dialect.getQuoteEnd());
                    }
                }
                appender.add(",");
            });
            appender.removeLast();
            appender.add(" FROM ", metaData.getName(), " ", metaData.getAlias());
            //生成join
            needSelectTable.forEach(table -> {
                if (table.equals(metaData.getName())) return;
                Correlation correlation = metaData.getCorrelation(table);
                if (correlation != null) {
                    appender.add(" ", correlation.getJoin(), " "
                            , correlation.getTargetTable(), " ", correlation.getAlias()
                            , " ON ");
                    SqlAppender joinOn = new SqlAppender();
                    buildWhere(metaData.getDatabaseMetaData().getTable(correlation.getTargetTable()),
                            "", correlation.getTerms(), joinOn, new HashSet());
                    if (!joinOn.isEmpty()) joinOn.removeFirst();
                    appender.addAll(joinOn);
                }
            });
            if (!whereSql.isEmpty())
                appender.add(" WHERE ", "").addAll(whereSql);
            if (!sorts.isEmpty()) {
                appender.add(" ORDER BY ");
                sorts.forEach(sort -> appender.add(sort.getField(), " ", sort.getDir(), ","));
                appender.removeLast();
            }
            String sql = appender.toString();
            if (param.isPaging()) {
                sql = dialect.doPaging(sql, param.getPageIndex(), param.getPageSize());
            }
            SimpleSQL simpleSQL = new SimpleSQL(metaData, sql, param);
            return simpleSQL;
        }

        @Override
        public Dialect getDialect() {
            return dialect;
        }
    }

    @Override
    public SQL render(TableMetaData metaData, QueryParam param) {
        return new SimpleSelectSqlRenderProcess(metaData, param).process();
    }
}
