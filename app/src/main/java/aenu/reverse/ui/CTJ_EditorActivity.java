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
import android.widget.Toast;

import com.myopicmobile.textwarrior.android.FreeScrollingTextField;
import com.myopicmobile.textwarrior.android.YoyoNavigationMethod;
import com.myopicmobile.textwarrior.common.Document;
import com.myopicmobile.textwarrior.common.DocumentProvider;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.jetbrains.java.decompiler.main.Fernflower;
import org.jetbrains.java.decompiler.main.decompiler.PrintStreamLogger;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import org.jetbrains.java.decompiler.main.extern.IResultSaver;


//Class To Java Editor
public class CTJ_EditorActivity extends LoadActivity{

    private String javaCode;
    private String outputLog;
    private FreeScrollingTextField free_text_view;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_code);    

        free_text_view=(FreeScrollingTextField)findViewById(android.R.id.message);
        free_text_view.setShowLineNumbers(true);
        free_text_view.setHighlightCurrentRow(true);
        free_text_view.setNavigationMethod(new YoyoNavigationMethod(free_text_view));

        startLoad();
    }
    
    @Override
    protected void loading() throws Exception{
        
        String mode;
        String path=getIntent().getData().getPath();
        ZipFile zfile=null;
        List<Map<String,byte[]>> class_list;
        
        mode=getIntent().getStringExtra(EditorIntent.EXTRA_OPEN_MODE);

        if(mode!=null&&mode.equals(EditorIntent.OPEN_MODE_IN_ZIP)){
            String entry=getIntent().getStringExtra(EditorIntent.EXTRA_OPEN_ENTRY);      
            zfile=new ZipFile(path);                
            class_list=loadFullClassWithZip(zfile,entry);
        }else{
            class_list=loadFullClassWithFile(new File(path));
        }

        Map<String,String> result=decompileClass(class_list);
        javaCode=result.keySet().iterator().next();
        outputLog=result.get(javaCode);
        
        if(zfile!=null)
            zfile.close();
        
    }

    private List<Map<String,byte[]>> loadFullClassWithZip(ZipFile zfile,String classEntry) throws IOException{
       
        final List<Map<String,byte[]>> list=new ArrayList<>();
        
        List<ZipEntry> entries=new ArrayList<ZipEntry>();
        int p=classEntry.indexOf('$');
        if(p==-1)p=classEntry.indexOf('.');
        classEntry=classEntry.substring(0,p);

        Enumeration<? extends ZipEntry> zentries=zfile.entries();
        for(ZipEntry e=zentries.nextElement();zentries.hasMoreElements();e=zentries.nextElement()){
            String name=e.getName();
            if(name.startsWith(classEntry)
               &&name.endsWith(".class")
               &&(name.charAt(p)=='$'||name.charAt(p)=='.'))
                entries.add(e);
        }
        
        ByteArrayOutputStream cache=new ByteArrayOutputStream();               
        byte[] buf=new byte[4096];
        InputStream in;
        int n;   
        
        for(ZipEntry e:entries){
           
            in=zfile.getInputStream(e);
            
            cache.reset();
            
            while((n=in.read(buf))!=-1)
                cache.write(buf,0,n);
                
            in.close();
            
            Map<String,byte[]> map=new HashMap<>();
            map.put(e.getName(),cache.toByteArray());
            
            list.add(map);
        }

        return list;
    }

    private List<Map<String,byte[]>> loadFullClassWithFile(File classFile) throws IOException{
        
        final List<Map<String,byte[]>> list=new ArrayList<>();
        
        File dir=classFile.getParentFile();

        String className=classFile.getName();
        int p=className.indexOf('$');
        if(p==-1)p=className.indexOf('.');
        className=className.substring(0,p);

        List<File> entries=new ArrayList<File>();

        File[] files=dir.listFiles();

        for(File f:files){
            String name=f.getName();
            if(f.isFile()
               &&name.startsWith(className)
               &&name.endsWith(".class")
               &&(name.charAt(p)=='$'||name.charAt(p)=='.'))
                entries.add(f);
        }
     
        ByteArrayOutputStream cache=new ByteArrayOutputStream();         
        
        InputStream in;
        byte[] buf=new byte[4096];
        int n;
        
        for(File f:entries){
            
            in=new FileInputStream(f);
            
            cache.reset();
            
            while((n=in.read(buf))!=-1)
                cache.write(buf,0,n);
            
            in.close();
            
            Map<String,byte[]> map=new HashMap<>();
            map.put(f.getAbsolutePath(),cache.toByteArray());
            
            list.add(map);
        }

        return list;
    }
    
    @Override
    protected void onLoadDone(){
        Document doc=new Document(free_text_view);
        
        String text=javaCode+"\n\n/*----\n\n"+outputLog+"\n\n----*/\n\n";
        
        doc.setText(text);
        doc.setWordWrap(false);//设置为true会无法滑动！

        free_text_view.setDocumentProvider(new DocumentProvider(doc));
        free_text_view.setLanguage(JavaLanguage.instance);
        free_text_view.respan();
        free_text_view.invalidate();
    }

    @Override
    protected void onLoadFailed(){
        String text=String.format(getString(R.string.open_file_err),"class");
        Toast.makeText(this,text,1).show();
        finish();
    }
    
    public final static Map<String/*source code*/,String/*log output*/>  decompileClass(final List<Map<String,byte[]>> classList) throws IOException{
           
        final ByteArrayOutputStream log_out=new ByteArrayOutputStream();

        final PrintStreamLogger logger=new PrintStreamLogger(new PrintStream(log_out));
    
        final IBytecodeProvider provider=new IBytecodeProvider(){
            @Override
            public byte[] getBytecode(String tag) throws IOException{
                for(Map<String,byte[]> t:classList){
                    String cls=t.keySet().iterator().next();
                    if(cls.equals(tag))
                        return t.get(cls);
                }
                return null;
            }
        };

        final ByteArrayOutputStream result=new ByteArrayOutputStream();
        
        final IResultSaver saver=new IResultSaver(){
            @Override
            public void saveResult(String tag,String content){
                try{
                    result.write(content.getBytes());
                }catch(IOException e){
                }          
            }
        };

        final Fernflower fernflower=new Fernflower(provider,saver,null, logger);

        for(Map<String,byte[]> t:classList){
            String cls=t.keySet().iterator().next();
            fernflower.getStructContext().addSpace(cls,true);
        }
        
        try {
            fernflower.decompileContext();
        }
        finally {
            fernflower.clearContext();
            
            Map<String,String> ret=new HashMap<>();
            ret.put(result.toString(),log_out.toString());
            
            return ret;
        }
    }
}
