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

import aenu.reverse.ui.util.ZipArchive;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipFile;

public class ApkInBrowseActivity extends LoadActivity{
    
    private static final String LOG_TAG = Application.getLogTag()+ApkInBrowseActivity.class;
    
    private final ZipArchive.Entry ROOT=null;
    private ZipArchive apkFile;
    private ZipArchive.Entry currentDir;
    
    private ListView listView;
    
    private final ListView.OnItemClickListener itemClick=new ListView.OnItemClickListener(){
        @Override
        public void onItemClick(AdapterView<?> p1,View p2,int p3,long p4){
            onListItemClick((ListView)p1,p2,p3,p4);
        }      
    };
    private final View.OnClickListener gotoParentClick=new View.OnClickListener(){
        @Override
        public void onClick(View p1)
        {
            if(currentDir!=ROOT)
                changeDir(currentDir.getParentEntry());                       
        }
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);    
        listView=(ListView)findViewById(android.R.id.list);
        listView.setOnItemClickListener(itemClick);
        
        findViewById(R.id.goto_parent_dir).setOnClickListener(gotoParentClick);
        
        startLoad();
    }

    @Override
    protected void onDestroy(){
        super.onDestroy();
        if(apkFile!=null)
            apkFile.close();
    }
    
    @Override
    protected void loading() throws IOException{
        ZipArchive zfile=new ZipArchive(new File(getIntent().getData().getPath()));
        apkFile=zfile;
    }

    @Override
    protected void onLoadDone(){
        changeDir(ROOT);
    }

    @Override
    protected void onLoadFailed(){
        String text=String.format(getString(R.string.open_file_err),"apk");
        Toast.makeText(this,text,1).show();
        finish();
    }
    
    private Dialog createArscDecoderSelectDialog(){
        AlertDialog.Builder builder=new AlertDialog.Builder(this);
       
        final String[] decoder_select=getResources().getStringArray(R.array.arsc_decoder_select);
        
        DialogInterface.OnClickListener l=new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface p1,int p2){
                p1.dismiss();
                
                if(decoder_select[p2].equals("arsc_preview"))
                    openEditor(ArscEditorActivity.class,"resources.arsc");
                else if(decoder_select[p2].equals("arsc_to_xml"))
                    openEditor(ArscToXmlEditorActivity.class,"resources.arsc");            
            }            
        };
        
        return builder
          .setItems(R.array.arsc_decoder_select_name,l)
          .create();
    }
    
    private void changeDir(ZipArchive.Entry dir){
        
        currentDir=dir;
        
        ((TextView)findViewById(R.id.list_path_show))
            .setText(dir==ROOT?":":':'+dir.getFullPath());
        
        if(dir==ROOT)
            listView.setAdapter(new ApkInAdapter(this,apkFile.listRootEntries()));  
        else 
            listView.setAdapter(new ApkInAdapter(this,dir));
    }
    
    private void onSelectEntry(ZipArchive.Entry entry){
        if(entry.getName().equals("resources.arsc")){
            createArscDecoderSelectDialog().show();
        }
        else if(entry.getName().endsWith(".xml")){
            openEditor(XmlEditorActivity.class,entry.getFullPath());
        }     
        else if(entry.getName().endsWith(".dex")){
            openEditor(DexEditorActivity.class,entry.getFullPath());
            
            //execDex2Jar(getIntent().getData().getPath(),entry.getFullPath());
        }     
    }
    
    private void openEditor(Class<?> editor,Object... args){
        if(editor.equals(ArscEditorActivity.class)
         ||editor.equals(XmlEditorActivity.class)
           ||editor.equals(ArscToXmlEditorActivity.class)
           ||editor.equals(DexEditorActivity.class)
         ){
         
           Intent intent=new Intent(this,editor);
           intent.setData(getIntent().getData());
           intent.putExtra(EditorIntent.EXTRA_OPEN_MODE,EditorIntent.OPEN_MODE_IN_ZIP);
           intent.putExtra(EditorIntent.EXTRA_OPEN_ENTRY,(String)(args[0]));
           startActivity(intent);
       }
        else if(editor.equals(JarInBrowseActivity.class)){
           Intent intent=new Intent(this,editor);
           intent.setData(Uri.fromFile(new File((String)args[0])));
           startActivity(intent);
       }
    }
    
    private void onListItemClick(ListView l,View v,int position,long id){
        ZipArchive.Entry entry=(ZipArchive.Entry)l.getAdapter().getItem(position);
        if(entry.isDirectory())
            changeDir(entry);
        else
            onSelectEntry(entry);
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
    
    static class ApkInAdapter extends BaseAdapter{     
        
        private static final Comparator<ZipArchive.Entry> comparator_ = new Comparator<ZipArchive.Entry>(){
            final Collator collator =Collator.getInstance(); 
            @Override
            public int compare(ZipArchive.Entry p1,ZipArchive.Entry p2){
                if(p1.isDirectory()&&!p2.isDirectory())
                    return -1;
                else if(!p1.isDirectory()&&p2.isDirectory())
                    return 1;
                return collator.compare(p1.getName(),p2.getName());
            }
        };
        
        private List<ZipArchive.Entry> af_entries;
        private Context context;
        
        ApkInAdapter(Context context,List<ZipArchive.Entry> entries){
            af_entries=entries;
            this.context=context;
            sort();
        }
        
        ApkInAdapter(Context context,ZipArchive.Entry entry){
            af_entries=entry.listEntries();
            this.context=context;
            sort();
        }
        
        private void sort(){
            Collections.sort(af_entries,comparator_);
        }
        
        @Override
        public int getCount(){
            return af_entries.size();
        }

        @Override
        public Object getItem(int p1){
            return af_entries.get(p1);
        }

        @Override
        public long getItemId(int p1){
            return 0;
        }

        private LayoutInflater getLayoutInflater(){
            return (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        private String getTimeStr(long time){
            return DateFormat.format("yyyy-MM-dd hh:mm:ss",time).toString();
        }
        
        private String getSizeStr(long size){
            double s=(double)size;
            double u;
            String suffix;
            if(s>(u=1<<30)){
                suffix="gb";
            }
            else if(s>(u=1<<20)){
                suffix="mb";
            }
            else if(s>(u=1<<10)){
                suffix="kb";
            }
            else{
                u=1;
                suffix="b";
            }
            
            return String.format("%.2f ",s/u)+suffix;
        }
        
        @Override
        public View getView(int pos,View curView,ViewGroup p3){
                
            if(curView==null){
                curView=getLayoutInflater().inflate(R.layout.list_item_1,null);
            } 
            
            ZipArchive.Entry entry=af_entries.get(pos);
            
            ImageView icon=(ImageView)curView.findViewById(android.R.id.icon);
            
            icon.setImageResource(
              entry.isDirectory()
              ?R.drawable.ic_folder
              :R.drawable.ic_file);
             
            TextView name=(TextView)curView.findViewById(android.R.id.text1);
            name.setText(entry.getName());
   
            TextView hint=(TextView)curView.findViewById(android.R.id.text2);
            String hihtStr=getTimeStr(entry.getTime());
            if(!entry.isDirectory())
                hihtStr+=" "+getSizeStr(entry.getSize());
            hint.setText(hihtStr);
           
            return curView;
        }     
    }
}
