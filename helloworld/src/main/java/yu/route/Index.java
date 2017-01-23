package yu.route;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import rx.Observable;
import yu.db.MysqlPool;
import yu.utils.Result;

import java.util.HashSet;

/**
 * Created by yunan on 2017/1/21.
 */
public class Index
{
    private static Router mrouter;

    private Index(){}

    public Index(Router router){
        mrouter = router;

        mrouter.route("/*").handler(BodyHandler.create());
        mrouter.route("/").handler(CorsHandler.create("*").allowedMethods( new HashSet<HttpMethod>(){{
            add(HttpMethod.GET);
            add(HttpMethod.POST);
            add(HttpMethod.POST);
        }}));

        mrouter.post("/hello").handler(this::hello);
        mrouter.get("/add").handler(this::add);
        mrouter.get("/getdb").handler(this::getdb);
    }

    private void hello(RoutingContext ctx){
        JsonObject reqparams = ctx.getBodyAsJson();
        Result result= new Result();
        String name = "default";
        try{
            name = reqparams.getString("username");
            result = new Result("true",String.format("hello %s",name),"","");
        }catch (Exception ex){
            result = new Result("false",String.format("hello %s",name),ex.getMessage(),"");
        }finally
        {
            createres(ctx, Json.encodePrettily(result));
        }


    }

    private void add(RoutingContext ctx){
        Result result = new Result();
        try{
            int a = Integer.parseInt(ctx.request().getParam("x"));
            int b = Integer.parseInt(ctx.request().getParam("y"));

            result = new Result("true",String.valueOf(a+b),"","");
        }catch (Exception ex){
            result = new Result("false","",ex.getMessage(),"");
        }finally
        {
            createres(ctx, Json.encodePrettily(result));
        }
    }

    private void getdb(RoutingContext ctx){
        Observable rs = MysqlPool.GetClient("select count(*) from city")
                .concatMap(ar->MysqlPool.GetClient("select * from country"));
        rs.subscribe(
                ar->ctx.response().end(Json.encodePrettily(((ResultSet)ar).getRows()))
        );


    }

    private static  void createres(RoutingContext ctx,String result){
        ctx.response().setChunked(true);
        ctx.response().write(result);
        ctx.response().end();
    }

}
