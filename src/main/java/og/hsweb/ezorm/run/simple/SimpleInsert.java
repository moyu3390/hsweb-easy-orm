package og.hsweb.ezorm.run.simple;

import og.hsweb.ezorm.executor.SQL;
import og.hsweb.ezorm.executor.SqlExecutor;
import og.hsweb.ezorm.meta.expand.Trigger;
import og.hsweb.ezorm.param.InsertParam;
import og.hsweb.ezorm.render.SqlRender;
import og.hsweb.ezorm.run.Insert;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;

/**
 * Created by zhouhao on 16-6-5.
 */
class SimpleInsert<T> implements Insert<T> {
    private InsertParam insertParam;
    private SimpleTable<T> table;
    private SqlExecutor sqlExecutor;

    public SimpleInsert(SimpleTable<T> table, SqlExecutor sqlExecutor) {
        this.table = table;
        this.sqlExecutor = sqlExecutor;
        insertParam = new InsertParam();
    }

    @Override
    public Insert<T> value(T data) {
        this.insertParam.setData(data);
        return this;
    }

    @Override
    public Insert<T> values(Collection<T> data) {
        this.insertParam.setData(data);
        return this;
    }

    @Override
    public int exec() throws SQLException {
        boolean supportBefore = table.getMeta().triggerIsSupport(Trigger.insert_before);
        boolean supportDone = table.getMeta().triggerIsSupport(Trigger.insert_done);
        Map<String, Object> context = table.getDatabase().getTriggerContextRoot();
        if (supportBefore || supportDone) {
            context = table.getDatabase().getTriggerContextRoot();
            context.put("table", table);
            context.put("database", table.getDatabase());
            context.put("param", insertParam);
        }
        if (supportBefore) {
            table.getMeta().on(Trigger.insert_before, context);
        }
        SqlRender<InsertParam> render = table.getMeta().getDatabaseMetaData().getRenderer(SqlRender.TYPE.INSERT);
        SQL sql = render.render(table.getMeta(), insertParam);
        int total = sqlExecutor.insert(sql);
        if (supportDone) {
            context.put("total", total);
            table.getMeta().on(Trigger.insert_done, context);
        }
        return total;
    }
}