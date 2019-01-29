package org.slf4j;

public interface Logger{

    public void info(String p0,Object ...p1);

    public void warn(String p0,Object ...p1);

    public void error(String p0,Object ...p1);

    public void debug(String p0,Object ...p1);
}
