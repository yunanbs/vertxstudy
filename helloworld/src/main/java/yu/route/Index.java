package yu.route;

import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpClientOptions;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.Session;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CookieHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.ext.web.handler.SessionHandler;
import io.vertx.ext.web.sstore.ClusteredSessionStore;
import io.vertx.ext.web.sstore.SessionStore;
import io.vertx.rx.java.ObservableFuture;
import io.vertx.rxjava.core.RxHelper;
import io.vertx.rxjava.core.buffer.Buffer;
import io.vertx.rxjava.core.http.HttpClient;
import io.vertx.rxjava.core.http.HttpClientRequest;
import io.vertx.rxjava.core.http.HttpClientResponse;
import rx.Observable;
import yu.app;
import yu.db.MysqlPool;
import yu.db.MysqlPoolCF;
import yu.utils.ActionResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.*;

/**
 * Created by yunan on 2017/1/21.
 */
public class Index
{
    private static Router mrouter;

    public static  Router getrouter(){
        return  mrouter;
    }

    public static void IniRouter(Vertx vertx){
        SessionStore cstore = ClusteredSessionStore.create(vertx);

        mrouter = Router.router(vertx);
        mrouter.route().handler(BodyHandler.create());
        mrouter.route().handler(CorsHandler.create("*").allowedMethods( new HashSet<HttpMethod>(){{
            add(HttpMethod.GET);
            add(HttpMethod.POST);
            add(HttpMethod.OPTIONS);
        }}));
        mrouter.route().handler(CookieHandler.create());
        mrouter.route().handler(SessionHandler.create(cstore));

        mrouter.get("/testquery").handler(Index::testquery);
        mrouter.get("/readsession").handler(Index::getsession);
        mrouter.get("/setsession").handler(Index::setsession);

        mrouter.get("/getaddress").handler(Index::address);

        mrouter.get("/cftest").handler(Index::cftest);
    }

    public static void testquery(RoutingContext ctx){
        MysqlPool.mysqlpool.getConnection(ar->{
            if(ar.failed())
                ctx.response().end(Json.encodePrettily(ActionResult.getresult(false,ar.cause().getMessage(),"")));

            //List<Integer> r = new ArrayList<Integer>();
            final ActionResult[] actionResult = {null};

            // 链式操作 可以通过自定义 list 方式获取各个环节的操作结果
            //MysqlPool.query(ar.result(),"select * from country")
            //        .concatMap(res -> {
            //            r.add(res.getRows().size());
            //            return MysqlPool.query(ar.result(),"select * from city");
            //        }).subscribe(
            //            res->{
            //                r.add(res.getRows().size());
            //                actionResult[0] =ActionResult.getresult(true,Json.encodePrettily(r),"");
            //            },
            //            e->{
            //                actionResult[0] =ActionResult.getresult(false,e.getMessage(),"");
            //            },
            //            ()->{
            //                ar.result().close();
            //                ctx.response().end(Json.encodePrettily(actionResult[0]));
            //            }
            //        );

            // combineLatest 可以写入多个Observable 并在一个最终的方法中返回这些Observable的操作结果
            Observable.combineLatest(
                    MysqlPool.query(ar.result(),"select count(*) as c1 from country"),
                    MysqlPool.query(ar.result(),"select count(*) as c2 from city"),
                    (rs1,rs2)->{
                        List<JsonObject> sr = new ArrayList(rs1.getRows());
                        sr.addAll(rs2.getRows());
                        return  sr;
                    })
                    .subscribe(
                    res->{
                        actionResult[0] =ActionResult.getresult(true,Json.encodePrettily(res),"");
                    },
                    e->{
                        actionResult[0] =ActionResult.getresult(false,e.getMessage(),"");
                    },
                    ()->{
                        ar.result().close();
                        ctx.response().end(Json.encodePrettily(actionResult[0]));
                    }
            );

        });
    }

    public static void setsession(RoutingContext ctx){
        Session s = ctx.session();
        s.put("name","yunan");
        ctx.response().end("session ok");
    }

    public static void getsession(RoutingContext ctx){
        Session s = ctx.session();
        ctx.response().end(s.id()+": name "+s.get("name"));
    }

    public static void address(RoutingContext ctx){
        //getaddressex(ctx.request().getParam("add")).subscribe(
        //        res->
        //        {
        //            Buffer ar = (Buffer) res;
        //            ctx.response().end(ar.toString());
        //        }
        //);

        ExecutorService es = Executors.newCachedThreadPool();






        //List<String> s = new ArrayList<>();
        //Observable.from(new String[]{"上海市宝山区友谊路1016号", "上海市杨高北路38885弄"})
        //        .concatMap(res->getbuffer(res))
        //        .subscribe(
        //                res->{System.out.println(((Buffer)res).toString());}
        //);
    }

    private static Observable getaddress(String souraddress){
        return Observable.create((res)->{
            String url = "http://restapi.amap.com/v3/place/text?key=e58f8693ecc99194ffb7ec1d6427fb55&keywords=%s";
            String tmp  = String.format(url,souraddress);
            HttpClient arclient = new HttpClient(app.vertx.createHttpClient( new HttpClientOptions().setDefaultPort(80).setDefaultHost("restapi.amap.com")));
            HttpClientRequest resp= arclient.request(HttpMethod.GET,80,"restapi.amap.com",tmp);
            Observable<HttpClientResponse> result = resp.toObservable();
            result.subscribe(res);
            resp.end();
        });
    }

    private static void getaddressex(String souraddress, Handler<Buffer> h){
        String url = "http://restapi.amap.com/v3/place/text?key=e58f8693ecc99194ffb7ec1d6427fb55&keywords=%s";
        String tmp  = String.format(url,souraddress);
        HttpClient arclient = new HttpClient(app.vertx.createHttpClient( new HttpClientOptions().setDefaultPort(80).setDefaultHost("restapi.amap.com")));
        HttpClientRequest resp= arclient.request(HttpMethod.GET,80,"restapi.amap.com",tmp,res->res.bodyHandler(buffer -> h.handle(buffer)));
        resp.end();
    }

    private static Observable getaddressexx(String souraddress){
        ObservableFuture t = new ObservableFuture();

        String url = "http://restapi.amap.com/v3/place/text?key=e58f8693ecc99194ffb7ec1d6427fb55&keywords=%s";
        String tmp  = String.format(url,souraddress);
        HttpClient arclient = new HttpClient(app.vertx.createHttpClient( new HttpClientOptions().setDefaultPort(80).setDefaultHost("restapi.amap.com")));
        HttpClientRequest resp= arclient.request(HttpMethod.GET,80,"restapi.amap.com",tmp,res->res.bodyHandler(buffer -> t.toHandler().handle(buffer)));
        resp.end();
        return t.asObservable();
    }

    private  static Observable getbuffer(String sourceaddress){
        ObservableFuture future = io.vertx.rx.java.RxHelper.observableFuture();
       getaddressex(sourceaddress,future.toHandler());
        return  future;
    }

    private static void cftest(RoutingContext ctx){
        CompletableFuture<SQLConnection> connfuture = MysqlPoolCF.GetConCF();
        CompletableFuture<String> queryfuture1 = connfuture.thenComposeAsync(res->MysqlPoolCF.QueryCF(res,"select count(*) from country"));
        CompletableFuture<String> queryfuture2 = connfuture.thenComposeAsync(res->MysqlPoolCF.QueryCF(res,"select count(*) from city"));
        CompletableFuture<String> queryfuture3 = queryfuture2.thenComposeAsync(res->MysqlPoolCF.Queryaddress("上海市友谊路1016号"));

        //queryfuture.whenComplete((res,e)->
        //{
        //    connfuture.thenAccept(
        //            ar->{
        //                ar.close();
        //                System.out.println("close db");
        //            });
        //    ctx.response().end(res);
        //});



        CompletableFuture.allOf(queryfuture1,queryfuture2,queryfuture2).whenComplete((s,e)->{
            List<String> results=new ArrayList<>();
            try
            {
                results.add(queryfuture1.get());
                results.add(queryfuture2.get());
                results.add(queryfuture3.get());
            } catch (Exception ex)
            {
                ex.printStackTrace();
            }finally
            {
                ctx.response().end(Json.encodePrettily(results));
                connfuture.thenAccept(res->res.close());
            }
        });
    }

}
