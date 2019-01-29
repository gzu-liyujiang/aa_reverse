
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
import android.content.SharedPreferences;
import android.content.res.Resources;
import jadx.api.JadxPreferences;

public class Preferences{
    
    //界面
    
    public static int ui_GetTheme(SharedPreferences pref){
        
        final int theme_4x[]={
            R.style.Theme_Holo,
            R.style.Theme_Holo_Light
        };

        final int theme_5x[]={
            R.style.Theme_Material,
            R.style.Theme_Material_Light
        };

        final String theme_values[]={
            "dark",
            "light"
        };
    
        final String theme=pref.getString("ui_theme","dark");
        for(int i=0;i<theme_values.length;i++){

            if(!theme_values[i].equals(theme))
                continue;

            if(android.os.Build.VERSION.SDK_INT>=21)
                return theme_5x[i];
            else
                return theme_4x[i];
        }
        return theme_4x[0];
    }
    
    //!界面
    
    //反编译
    
    public static String decompile_GetIntFormat(SharedPreferences pref){
        return pref.getString("decompile_int_format","%d");
    }
    
    public static String decompile_GetIndentStr(SharedPreferences pref){
        int indent_count=pref.getInt("decompile_indent_count",4);
        String indent="";
        while(--indent_count>-1)
            indent+=' ';
        return indent;
    }
    
    public static boolean decompile_IsEscapeUnicode(SharedPreferences pref){
        boolean escape_unicode=pref.getBoolean("decompile_escape_unicode",false);
        return escape_unicode;
    }
    
    public static int decompile_GetLacalVarStyle(SharedPreferences pref){
        String styleStr=pref.getString("decompile_local_var_style","snake");
        int style=JadxPreferences.LOCAL_VAR_STYLE_SNAKE;
        if(styleStr.equals("snake"))
            style=JadxPreferences.LOCAL_VAR_STYLE_SNAKE;
        else if(styleStr.equals("hump"))
            style=JadxPreferences.LOCAL_VAR_STYLE_HUMP;
        else if(styleStr.equals("hump_large"))
            style=JadxPreferences.LOCAL_VAR_STYLE_HUMP_LARGE;
        else if(styleStr.equals("abbreviate"))
            style=JadxPreferences.LOCAL_VAR_STYLE_ABBREVIATE;
        else if(styleStr.equals("abbreviate_large"))
            style=JadxPreferences.LOCAL_VAR_STYLE_ABBREVIATE_LARGE;
        else if(styleStr.equals("first_cut"))
            style=JadxPreferences.LOCAL_VAR_STYLE_FIRST_CUT;
        else if(styleStr.equals("last_cut"))
            style=JadxPreferences.LOCAL_VAR_STYLE_LAST_CUT;
        else if(styleStr.equals("hungary"))
            style=JadxPreferences.LOCAL_VAR_STYLE_HUNGARY;
        else if(styleStr.equals("hungary_snake"))
            style=JadxPreferences.LOCAL_VAR_STYLE_HUNGARY_SNAKE;
        return style;
    }
    
    public static String decompile_getFrameworkPath(SharedPreferences pref){
        return pref.getString("decompile_framework_path","/system/framework/framework-res.apk");  
    }
    
    public static String decompile_getLibsPath(SharedPreferences pref){
        return pref.getString("decompile_libs_path","/sdcard/aa_reverse/libs");        
    }
    
    //!反编译
}
