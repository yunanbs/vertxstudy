package yu.service;

import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import rx.Observable;
import yu.db.MysqlPool;

/**
 * Created by yunan on 2017/1/28.
 */
public class baseservice
{
    public static Observable<ResultSet> testmutilquery(SQLConnection conn, String sql){
        return MysqlPool.query(conn,sql);
    }
}
