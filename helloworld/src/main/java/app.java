import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.http.HttpServer;
import io.vertx.core.http.HttpServerOptions;
import io.vertx.ext.web.Router;
import yu.db.MysqlPool;
import yu.log.Log;
import yu.route.Index;

import java.util.logging.Logger;

/**
 * Created by yunan on 2017/1/21.
 */
public class app extends AbstractVerticle
{
    private static  Vertx vertx;
    private static HttpServer server;
    private static Router router;
    private static Logger log  = Log.getlog("app");
    public static void main(String[] args) {
        long st = System.currentTimeMillis();
        log.info("start app......");
        //System.out.println("start app......");
        vertx = Vertx.vertx(new VertxOptions().setWorkerPoolSize(1000));
        vertx.deployVerticle(app.class.getName(),new DeploymentOptions().setInstances(2 * Runtime.getRuntime().availableProcessors()));
        server = vertx.createHttpServer(new HttpServerOptions().setPort(8000));
        router = Router.router(vertx);
        Index index = new Index(router);
        MysqlPool.IniDB(vertx);
        server.requestHandler(router::accept).listen();
        long ed = System.currentTimeMillis();
        //System.out.println(String.format("app start ok %d ms",ed-st));
        log.info(String.format("app start ok %d ms",ed-st));

}
}
