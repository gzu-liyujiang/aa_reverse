package org.slf4j;
import android.util.Log;

class Logger_Android implements Logger{

    private static final String TAG="my-slf4j";
    @Override
    public void info(String p0,Object[] p1){
        String str=msg_+p0;
        
        for(Object o:p1)
            if(o instanceof String)
                str+=o+"\n";
            else if(o instanceof CharSequence)
                str+=o+"\n";
            else
                str+=o.getClass().getName()+"\n";
                  
        Log.i(TAG,str);
    }

    @Override
    public void warn(String p0,Object[] p1){
        String str=msg_+p0;
       
        for(Object o:p1)
            if(o instanceof String)
                str+=o+"\n";
            else if(o instanceof CharSequence)
                str+=o+"\n";
            else
                str+=o.getClass().getName()+"\n";
        
        Log.w(TAG,str);
    }

    @Override
    public void error(String p0,Object[] p1){
        String str=msg_+p0;
        
        for(Object o:p1)
            if(o instanceof String)
                str+=o+"\n";
            else if(o instanceof CharSequence)
                str+=o+"\n";
            else
                str+=o.getClass().getName()+"\n";
        
        Log.e(TAG,str);
    }

    @Override
    public void debug(String p0,Object[] p1){
        String str=msg_+p0;
        
        for(Object o:p1)
            if(o instanceof String)
                str+=o+"\n";
            else if(o instanceof CharSequence)
                str+=o+"\n";
            else
                str+=o.getClass().getName()+"\n";
        
        Log.d(TAG,str);
    }

    Logger_Android(String msg){
        msg_= msg+"_ ";
    }

    private String msg_;
}
