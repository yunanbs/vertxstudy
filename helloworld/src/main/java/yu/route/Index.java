package yu.route;

import io.vertx.core.http.HttpMethod;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import io.vertx.rx.java.RxHelper;
import rx.Observable;
import yu.db.MysqlPool;
import yu.utils.ActionResult;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

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

        mrouter.get("/testquery").handler(this::testquery);
    }



    private void testquery(RoutingContext ctx){
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

}
