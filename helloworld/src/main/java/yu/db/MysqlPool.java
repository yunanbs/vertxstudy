package yu.db;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import rx.Observable;

/**
 * Created by yunan on 2017/1/22.
 */
public class MysqlPool
{
    private static JDBCClient mysqlpool;
//    private static AsyncSQLClient mysqlpool;
    public static void IniDB(Vertx vertx){

        JsonObject config = new JsonObject();
        config.put("user", "root");
        config.put("password", "root");
        config.put("max_pool_size", 20);

        config.put("url", "jdbc:mysql://127.0.0.1:3306/world?characterEncoding=UTF-8");
        config.put("driver_class", "com.mysql.jdbc.Driver");


//        config.put("host", "127.0.0.1");
//        config.put("port", 3306);

        mysqlpool = JDBCClient.createNonShared(vertx,config);
//        mysqlpool = MySQLClient.createNonShared(vertx,config);
    }

    public static Observable GetClient(String sql){
        Observable h = GetCon().concatMap(conn->query((SQLConnection) conn,sql));
        return  h;
    }

    public static Observable GetCon(){
        ObservableFuture result = RxHelper.observableFuture();
        mysqlpool.getConnection(result.toHandler());
        return  result;
    }

    public static Observable query(SQLConnection conn,String sql){
        ObservableFuture result = RxHelper.observableFuture();
        conn.query(sql,result.toHandler()).close();
        return  result;
    }

}
