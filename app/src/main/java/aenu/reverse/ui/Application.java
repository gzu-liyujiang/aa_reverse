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
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.PrintStream;

public class Application extends android.app.Application{

    private static final String LOG_TAG = Application.getLogTag()+Application.class;

    public final static String getLogTag(){
        return "AA逆向助手:";
    }
    
    @Override
    public void onCreate(){
        super.onCreate();
        registerActivityLifecycleCallbacks(callback);
        if(_ExceptionHandler==null)
            _ExceptionHandler=new ApplicationExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(_ExceptionHandler);
    }
    
    public final static ActivityLifecycleCallbacks callback=new ActivityLifecycleCallbacks(){
        
        
        private int theme;
        private SharedPreferences pref;     
        
        @Override
        public void onActivityCreated(Activity p1,Bundle p2){
            pref=PreferenceManager.getDefaultSharedPreferences(p1);     
            theme=Preferences.ui_GetTheme(pref);
            p1.setTheme(theme);
        }

        @Override
        public void onActivityStarted(Activity p1){
            if(theme!=Preferences.ui_GetTheme(pref)) {
                p1.recreate();
            }
        }

        @Override
        public void onActivityResumed(Activity p1){       
        }

        @Override
        public void onActivityPaused(Activity p1){
        }

        @Override
        public void onActivityStopped(Activity p1){
        }

        @Override
        public void onActivitySaveInstanceState(Activity p1,Bundle p2){
        }

        @Override
        public void onActivityDestroyed(Activity p1){
        }        
    };
    
    private static ApplicationExceptionHandler _ExceptionHandler;

    private static class ApplicationExceptionHandler implements Thread.UncaughtExceptionHandler {
        @Override
        public void uncaughtException(Thread thread, Throwable th) {
            ByteArrayOutputStream log_stream=new ByteArrayOutputStream();
            PrintStream print=new PrintStream(log_stream);
            th.printStackTrace(print);
            Log.e(LOG_TAG,log_stream.toString());
        }
    }
}

