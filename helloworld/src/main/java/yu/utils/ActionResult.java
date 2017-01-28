package yu.utils;

/**
 * Created by yunan on 2017/1/28.
 */
public class ActionResult
{
    private boolean resultflag;
    private String datastr;
    private String errstr;
    private String tag;

    public ActionResult(boolean resultflag, String datastr, String errstr, String tag)
    {
        this.resultflag = resultflag;
        this.datastr = datastr;
        this.errstr = errstr;
        this.tag = tag;
    }

    public static ActionResult getresult(boolean flag,String msg,String tag){
        if(flag){
            return  new ActionResult(flag,msg,"",tag);
        }else{
            return  new ActionResult(flag,"",msg,tag);
        }
    }

    public boolean isResultflag()
    {
        return resultflag;
    }

    public void setResultflag(boolean resultflag)
    {
        this.resultflag = resultflag;
    }

    public String getDatastr()
    {
        return datastr;
    }

    public void setDatastr(String datastr)
    {
        this.datastr = datastr;
    }

    public String getErrstr()
    {
        return errstr;
    }

    public void setErrstr(String errstr)
    {
        this.errstr = errstr;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }


}
