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
import android.os.Bundle;
import android.widget.ListView;
import aenu.reverse.ui.util.ZipArchive;
import java.io.File;
import java.util.zip.ZipFile;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.ByteArrayOutputStream;
import android.widget.Toast;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.content.Context;
import android.view.ViewGroup;
import android.view.View;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.Set;
import java.util.Comparator;
import android.widget.AdapterView;
import android.view.KeyEvent;
import android.widget.TextView;
import android.view.LayoutInflater;
import jadx.api.JadxDecompiler;
import jadx.api.JavaClass;
import jadx.core.dex.visitors.SaveCode;
import jadx.api.IResultSaver;
import jadx.core.codegen.CodeWriter;
import com.myopicmobile.textwarrior.android.FreeScrollingTextField;
import com.myopicmobile.textwarrior.android.YoyoNavigationMethod;
import com.myopicmobile.textwarrior.common.Document;
import com.myopicmobile.textwarrior.common.DocumentProvider;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Message;
import android.content.SharedPreferences;
import jadx.api.JadxPreferences;
import android.preference.PreferenceManager;
import jadx.api.JavaPackage;

public class DexEditorActivity extends LoadActivity
{
    private ListView listView;
    private boolean isShowPaths;
    private List<JavaPackage> javaPackages;
    private JadxDecompiler J_decompile;
    private FreeScrollingTextField JavaCodeView;
    private boolean isShowJavaCode;
    
    private final ListView.OnItemClickListener itemClick=new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> p1,View p2,int p3,long p4){
            onListItemClick((ListView)p1,p2,p3,p4);
        }      
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
       
        listView=new ListView(this);//(ListView)getLayoutInflater().inflate(R.layout.activity_list,null);
        listView.setOnItemClickListener(itemClick);
        listView.setFastScrollEnabled(true);
        
        JavaCodeView=(FreeScrollingTextField)getLayoutInflater().inflate(R.layout.activity_code,null);
        JavaCodeView.setShowLineNumbers(true);
        JavaCodeView.setHighlightCurrentRow(true);
        JavaCodeView.setNavigationMethod(new YoyoNavigationMethod(JavaCodeView));
        
        startLoad();
    }
    
    @Override
    protected void loading() throws Exception{
        String mode;
        String path=getIntent().getData().getPath();
        ZipFile zfile=null;
        InputStream in;
        
        mode=getIntent().getStringExtra(EditorIntent.EXTRA_OPEN_MODE);

        if(mode!=null&&mode.equals(EditorIntent.OPEN_MODE_IN_ZIP)){
            String entry=getIntent().getStringExtra(EditorIntent.EXTRA_OPEN_ENTRY);      
            zfile=new ZipFile(path);                
            in=zfile.getInputStream(zfile.getEntry(entry));
        }else{
            in=new FileInputStream(path);
        }

        ByteArrayOutputStream cache=new ByteArrayOutputStream();
        byte[] buf=new byte[4096];
        int n;
        
        while((n=in.read(buf))!=-1)
            cache.write(buf,0,n);
            
        in.close();
        
        if(zfile!=null)
            zfile.close();
            
        J_decompile=new JadxDecompiler(generateJadxPreferences(PreferenceManager.getDefaultSharedPreferences(this)));

        J_decompile.loadData(cache.toByteArray());
        J_decompile.parse();               
    }

    @Override
    protected void onLoadDone(){
        javaPackages=J_decompile.getPackages();
        
        showClassPaths();
    }

    @Override
    protected void onLoadFailed(){
        String text=String.format(getString(R.string.open_file_err),"dex");
        Toast.makeText(this,text,1).show();
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(isShowPaths)
                finish();
            else
                showClassPaths();
                
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
    
    private void showClassPaths(){
     
        setContentView(listView);
        
        setTitle("----");     
        
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,android.R.layout.simple_list_item_1){
            public View getView(int position,View convertView,ViewGroup parent){
                final TextView view=(TextView)super.getView(position,convertView,parent);

                if(view.getTag()==null)
                    view.setTag(view.getTextColors().getDefaultColor());

                int color=((int)view.getTag()^(255<<0))|(255<<16);

                view.setTextColor(color);
                return view;
            }
        };
    
        adapter.clear();
        
        for(JavaPackage pack:javaPackages)
            adapter.add(pack.getFullName());
            
        adapter.sort(new Comparator<String>(){
            @Override
            public int compare(String p1,String p2){
                return p1.compareTo(p2);
            }      
        });
            
        listView.setAdapter(adapter);
        
        isShowPaths=true;
        isShowJavaCode=false;
    }
    
    private void showClassList(JavaPackage pack){
        
        setTitle(pack.getFullName());
        
        List<JavaClass> list=pack.getClasses();
        
        listView.setAdapter(new JavaClassAdapter(this,list));
        
        isShowPaths=false;
        isShowJavaCode=false;
    }
    
    private Dialog createClassToJavaDialog(){
        ProgressDialog d=new ProgressDialog(this);
        d.setMessage("dex class to java");
        d.setCanceledOnTouchOutside(false);
        d.setOnKeyListener(new DialogInterface.OnKeyListener(){
                @Override
                public boolean onKey(DialogInterface p1,int p2,KeyEvent p3){
                    return true;
                }         
            });
        return d;
    }   
    
    private synchronized void showJavaSource(final JavaClass baseClass,final List<JavaClass> packageClassList){
        
        final Dialog d=createClassToJavaDialog();
        d.show();
        
        new Thread(){
            
            Handler h=new Handler(){
                public void handleMessage(Message msg){
                    
                    d.hide();
                    d.dismiss();
                    
                    if(msg.what==-1)
                        return;                                    
                    
                    String javaCode=(String)msg.obj;
                    setContentView(JavaCodeView);
                    Document doc=new Document(JavaCodeView);
                    doc.setText(javaCode);
                    doc.setWordWrap(false);//设置为true会无法滑动！

                    JavaCodeView.setDocumentProvider(new DocumentProvider(doc));
                    JavaCodeView.setLanguage(JavaLanguage.instance);
                    JavaCodeView.respan();
                    JavaCodeView.invalidate();
                    
                }
            };
            
            public void run(){              
                
                try{
                    String classEntry=baseClass.getFullName();

                    int p=classEntry.indexOf('$');
                    if(p!=-1)
                        classEntry=classEntry.substring(0,p);
                    
                    final StringBuilder javaCodeBuilder=new StringBuilder();;
                    
                    IResultSaver saver=new IResultSaver(){
                        @Override
                        public void saveResult(String classTag,String content){
                            if(classTag.indexOf('$')!=-1)
                                return;
                            javaCodeBuilder.append(content);
                        }        
                    };
                    
                    for(JavaClass jc:packageClassList){
                        String fullname=jc.getFullName();
                        if(fullname.equals(classEntry)
                           ||(fullname.startsWith(classEntry)
                           &&(fullname.charAt(p+1)=='$')))
                            J_decompile.decompile(jc,saver);
                    }
                    
                    Message msg=new Message();
                    
                    msg.obj=javaCodeBuilder.toString();
                    msg.what=0;

                    h.sendMessage(msg);
                }
                catch(Exception e){
                    h.sendEmptyMessage(-1);
                    isShowPaths=false;
                    isShowJavaCode=false;   
                }
                
            }
        }.start();
        
        isShowPaths=false;
        isShowJavaCode=true;     
    }
    
    private void onListItemClick(ListView l,View v,int position,long id){
        if(isShowPaths){        
            JavaPackage pack=javaPackages.get(position);
            showClassList(pack);
        }     
        else {
            JavaClassAdapter a=(JavaClassAdapter)l.getAdapter();
            JavaClass jc=(JavaClass)a.getItem(position);
            List<JavaClass> list=a.getJavaClassList();
            showJavaSource(jc,list);
        }
    }
    
    public static Map<Integer,Object> generateJadxPreferences(SharedPreferences pref){
        Map<Integer,Object> preferences=JadxPreferences.DEFAULT.generateDefaultPreferences();
        
        preferences.put(JadxPreferences.OPT_INT_FORMAT,Preferences.decompile_GetIntFormat(pref));
        preferences.put(JadxPreferences.OPT_INDENT_STRING,Preferences.decompile_GetIndentStr(pref));
        preferences.put(JadxPreferences.OPT_ESCAPE_UNICODE,Preferences.decompile_IsEscapeUnicode(pref));
        preferences.put(JadxPreferences.OPT_LOCAL_VAR_STYLE,Preferences.decompile_GetLacalVarStyle(pref));
        preferences.put(JadxPreferences.OPT_JAVA_LIBS_DIR,Preferences.decompile_getLibsPath(pref));
        //maps.put(OPT_LOCAL_VAR_STYLE,LOCAL_VAR_STYLE_SNAKE);
        
        return preferences;
    }
    
    static class JavaClassAdapter extends BaseAdapter{

        
        final List<JavaClass> src_list;
        final Context context;
        final List<JavaClass> list;
        JavaClassAdapter(Context context,List<JavaClass> list){
            this.context=context;
            this.src_list=list;
            this.list=filterClass(list);
        }
        
        List<JavaClass> filterClass(List<JavaClass> list){
            List<JavaClass> out_list=new ArrayList<>();
            for(JavaClass cls:list)
                if(cls.getName().indexOf('$')==-1)
                    out_list.add(cls);
                    
            return out_list;
        }
        
        List<JavaClass> getJavaClassList(){
            return src_list;
        }
        
        @Override
        public int getCount(){
            return list.size();
        }

        @Override
        public Object getItem(int p1){
            return list.get(p1);
        }

        @Override
        public long getItemId(int p1){
            return 0;
        }
        
        LayoutInflater getLayoutInflater(){
            return (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        @Override
        public View getView(int pos,View p2,ViewGroup p3){
            
            TextView text=null;
            if(p2==null)
                text=(TextView)getLayoutInflater().inflate(android.R.layout.simple_list_item_1,null);           
            else
                text=(TextView)p2;
                
            text.setText(list.get(pos).getName());
            return text;
        }           
    }
}
