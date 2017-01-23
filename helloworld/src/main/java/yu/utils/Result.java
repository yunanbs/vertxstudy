package yu.utils;

/**
 * Created by yunan on 2017/1/21.
 */
public class Result
{
    private String flag;
    private String data;
    private String error;
    private String tag;

    private Result()
    {
    }

    public Result(String flag, String data, String error, String tag)
    {
        this.flag = flag;
        this.data = data;
        this.error = error;
        this.tag = tag;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data)
    {
        this.data = data;
    }

    public String getError()
    {
        return error;
    }

    public void setError(String error)
    {
        this.error = error;
    }

    public String getTag()
    {
        return tag;
    }

    public void setTag(String tag)
    {
        this.tag = tag;
    }

    public String getFlag()
    {
        return flag;
    }

    public void setFlag(String flag)
    {
        this.flag = flag;
    }
}
