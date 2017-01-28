package yu.log;

import java.util.logging.Logger;

/**
 * Created by yunan on 2017/1/28.
 */
public class Log
{
    public static Logger getlog(String name){
         return  Logger.getLogger(name);
    }
}
