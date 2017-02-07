package yu;

import io.vertx.core.*;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.rx.java.RxHelper;
import io.vertx.spi.cluster.hazelcast.HazelcastClusterManager;
import rx.Observable;
import yu.db.MysqlPool;
import yu.db.MysqlPoolCF;
import yu.route.Index;

import java.security.PublicKey;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yunan on 2017/1/21.
 */
public class app extends AbstractVerticle
{
    //private static  Vertx vertx;
    //private static HttpServer server;
    //private static Router router;
    public static Vertx vertx=null;

    private static Logger log  = LoggerFactory.getLogger(app.class);
    //public static void main(String[] args)
    //{
    //    long st = System.currentTimeMillis();
    //    log.info("start app......");
    //    //System.out.println("start app......");
    //    vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(1000));
    //    vertx.deployVerticle(app.class.getName(), new DeploymentOptions().setInstances(2 * Runtime.getRuntime().availableProcessors()));
    //    server = vertx.createHttpServer(new HttpServerOptions().setPort(8000));
    //    router = Router.router(vertx);
    //    Index index = new Index(router);
    //    MysqlPool.IniDB(vertx);
    //    server.requestHandler(router::accept).listen();
    //    long ed = System.currentTimeMillis();
    //    //System.out.println(String.format("app start ok %d ms",ed-st));
    //    log.info(String.format("app start ok %d ms", ed - st));
    //
    //}

    public static void main(String[] args)
    {
        System.out.println(123);
        long st = System.currentTimeMillis();
        log.info("start app......");
        HazelcastClusterManager mgr = new HazelcastClusterManager();
        Vertx.clusteredVertx(new VertxOptions().setClusterManager(mgr).setWorkerPoolSize(1000).setClustered(true),res->
        {
            vertx = res.result();
            vertx.deployVerticle(app.class.getName(), new DeploymentOptions().setInstances(2 * Runtime.getRuntime().availableProcessors()));
            HttpServer server = vertx.createHttpServer(new HttpServerOptions().setPort(8000));
            Index.IniRouter(vertx);
            MysqlPoolCF.IniDB(vertx);
            server.requestHandler(Index.getrouter()::accept).listen();
            long ed = System.currentTimeMillis();
            log.info(String.format("app start ok %d ms", ed - st));

            List<String> lr = new ArrayList();
            Observable.just("hello world my name ha1 ha2 ha3").concatMap(s ->
            {
                List<Observable<Character>> tmp = new ArrayList();
                for (char c : s.toCharArray())
                {
                    tmp.add(Observable.just(c));
                }
                Observable[] a = new Observable[tmp.size()];

                //return Observable.zip(tmp.toArray(a),ss->ss);
                return  Observable.combineLatest(tmp,objects -> {
                   String result = "";
                   for(Object c: objects){
                        result = result+(char)c;
                   }
                   return  result;
                });
            }).subscribe(o -> {
                System.out.println(o);
                //System.out.println(o.length);
                //Object[] a = o.clone();
                //for(Object aa:a){
                //    System.out.println(aa);
                //}
            });
        });

    }

    public static <T> String gettest(T o){
        System.out.println(o);
        return  o.toString();
    }

}
