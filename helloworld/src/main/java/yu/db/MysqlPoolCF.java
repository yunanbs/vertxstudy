package yu.db;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.jdbc.JDBCClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rx.java.RxHelper;
import io.vertx.rxjava.core.http.HttpClient;
import io.vertx.rxjava.core.http.HttpClientRequest;
import rx.Observable;
import rx.functions.Func1;
import yu.app;
import yu.utils.ActionResult;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Created by yunan on 2017/2/7.
 */
public class MysqlPoolCF
{
    public static JDBCClient mysqlpool;

    public static void IniDB(Vertx vertx){

        JsonObject config = new JsonObject();
        config.put("user", "root");
        config.put("password", "root");
        config.put("max_pool_size", 20);

        config.put("url", "jdbc:mysql://127.0.0.1:3306/world");
        config.put("driver_class", "com.mysql.jdbc.Driver");

        mysqlpool = JDBCClient.createNonShared(vertx,config);

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

    public static CompletableFuture<SQLConnection> GetConCF(){
        CompletableFuture<SQLConnection> future = new CompletableFuture();
        mysqlpool.getConnection(res->{
            if(res.succeeded())
                future.complete(res.result());
            else
                future.completeExceptionally(res.cause());
        });
        return future;
    }

    public static CompletableFuture<String> QueryCF(SQLConnection conn,String sql){
        CompletableFuture<String> future = new CompletableFuture();
        conn.query(sql,res->{
            if(res.succeeded())
                future.complete(Json.encodePrettily(res.result().getRows()));
            else
                future.completeExceptionally(res.cause());
        });
        return future;
    }

    public static  CompletableFuture<String> Queryaddress(String address){
        CompletableFuture<String> result =new CompletableFuture<>();
        String url = "http://restapi.amap.com/v3/place/text?key=e58f8693ecc99194ffb7ec1d6427fb55&keywords=%s";
        String tmp  = String.format(url,address);
        HttpClient arclient = new HttpClient(app.vertx.createHttpClient( new HttpClientOptions().setDefaultPort(80).setDefaultHost("restapi.amap.com")));
        HttpClientRequest resp= arclient.request(HttpMethod.GET,80,"restapi.amap.com",tmp);
        resp.toObservable().subscribe(res->res.bodyHandler(buffer -> result.complete(buffer.toString())));
        resp.end();
        return result;
    }
}
