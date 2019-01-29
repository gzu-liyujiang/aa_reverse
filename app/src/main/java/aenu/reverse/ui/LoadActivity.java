
/**
 *  Copyright 2018 by aenu
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
 
package aenu.reverse.ui;

import android.app.Activity;
import android.os.Handler;
import android.os.Message;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.view.KeyEvent;
import android.util.Log;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.PrintStream;
import android.content.Context;

public abstract class LoadActivity extends Activity{
   
    private static final String LOG_TAG = Application.getLogTag()+LoadActivity.class;
    
    private static final int LOAD_FAILED=0xAA000000;
    private static final int LOAD_DONE=0xAA000001;
    
    private final Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg){
            load_dialog.hide();
            load_dialog.dismiss();
            load_dialog=null;
            
            load_thread=null;
            
            if(msg.what==LOAD_FAILED)
                onLoadFailed();
            else if(msg.what==LOAD_DONE)
                onLoadDone();
            else
                Log.w(LOG_TAG,"unknown message -- "+msg.what);
        }       
    };
    
    private Dialog load_dialog;
    
    private Thread load_thread=new Thread(){
        @Override
        public void run(){
            try{
                loading();
                handler.sendEmptyMessage(LOAD_DONE);
            }catch(Exception e){
                ByteArrayOutputStream log_stream=new ByteArrayOutputStream();
                PrintStream print=new PrintStream(log_stream);
                e.printStackTrace(print);
                Log.e(LOG_TAG,log_stream.toString());
                handler.sendEmptyMessage(LOAD_FAILED);                        
            }
        }     
    };
    
    protected abstract void loading() throws Exception;
    protected abstract void onLoadDone();
    protected abstract void onLoadFailed();
    
    protected void startLoad(){
        if(load_thread==null)
            throw new RuntimeException("multiple load");
        
        load_dialog=createLoadingDialog(this,getText(R.string.loading));
        load_dialog.show();
        load_thread.start();
    }
    
    public static final Dialog createLoadingDialog(Context context,CharSequence message){
        ProgressDialog d=new ProgressDialog(context);
        d.setMessage(message);
        d.setCanceledOnTouchOutside(false);
        d.setOnKeyListener(new DialogInterface.OnKeyListener(){
            @Override
            public boolean onKey(DialogInterface p1,int p2,KeyEvent p3){
                return true;
            }         
        });
        return d;
    }   
}
