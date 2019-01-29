package jadx.api;

public interface ILogger{
    void info(String msg);
    void wran(String msg);
    void error(String msg);
    void error(Throwable e);
    void error(String msg,Throwable e);
}
