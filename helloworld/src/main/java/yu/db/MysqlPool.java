package yu.db;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import rx.Observable;
import rx.functions.Func1;
import yu.utils.ActionResult;

import java.util.List;

/**
 * Created by yunan on 2017/1/22.
 */
public class MysqlPool
{
    public static JDBCClient mysqlpool;
//    private static AsyncSQLClient mysqlpool;
    public static void IniDB(Vertx vertx){

        JsonObject config = new JsonObject();
        config.put("user", "root");
        config.put("password", "root");
        config.put("max_pool_size", 20);

        config.put("url", "jdbc:mysql://127.0.0.1:3306/world");
        config.put("driver_class", "com.mysql.jdbc.Driver");


//        config.put("host", "127.0.0.1");
//        config.put("port", 3306);

        mysqlpool = JDBCClient.createNonShared(vertx,config);
//        mysqlpool = MySQLClient.createNonShared(vertx,config);
    }

    public static Observable<SQLConnection> GetCon(){
        ObservableFuture result = RxHelper.observableFuture();
        mysqlpool.getConnection(result.toHandler());
        return  result;
    }

    public static Observable<ResultSet> query(SQLConnection conn, String sql){
        ObservableFuture<ResultSet> result = RxHelper.observableFuture();
        conn.query(sql,result.toHandler());
        return  result;
    }

    public static Observable<List<Integer>> excutesqls(SQLConnection conn, List<String> sqls){
        ObservableFuture<List<Integer>> result = RxHelper.observableFuture();
        conn.batch(sqls,result.toHandler()).close();
        return  result;
    }

    public static Observable dosqljob(Func1<SQLConnection,Object> sqljob, Handler<ActionResult> h){
        Observable result = RxHelper.observableFuture();
        mysqlpool.getConnection(ar->{
            if(ar.failed()){
                h.handle(ActionResult.getresult(false,ar.cause().getMessage(),""));
                return;
            }else{
                sqljob.call(ar.result());
            }
        });
        return result;
    }

}
