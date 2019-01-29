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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class JarInBrowseActivity extends LoadActivity{
   
    private final ZipArchive.Entry ROOT=null;
    private ZipArchive jarFile;
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
    protected void loading() throws Exception{
        ZipArchive zfile=new ZipArchive(new File(getIntent().getData().getPath()));
        jarFile=zfile;
    }

    @Override
    protected void onLoadDone(){             
        changeDir(ROOT);
    }

    @Override
    protected void onLoadFailed(){
        String text=String.format(getString(R.string.open_file_err),"jar");
        Toast.makeText(this,text,1).show();
        finish();
    }

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
        if(jarFile!=null)
            jarFile.close();
    }
   
    private void changeDir(ZipArchive.Entry dir){

        currentDir=dir;

        ((TextView)findViewById(R.id.list_path_show))
          .setText(dir==ROOT?":":':'+dir.getFullPath());
        
        if(dir==ROOT)
            listView.setAdapter(new ZAdapter(this,jarFile.listRootEntries()));  
        else 
            listView.setAdapter(new ZAdapter(this,dir));
    }

    private void onSelectEntry(ZipArchive.Entry entry){
        if(entry.getName().equals("resources.arsc")){
            openEditor(ArscEditorActivity.class,entry.getFullPath());
        }
        else if(entry.getName().endsWith(".xml")){
            openEditor(XmlEditorActivity.class,entry.getFullPath());
        }     
        else if(entry.getName().endsWith(".class")){
            openEditor(CTJ_EditorActivity.class,entry.getFullPath());
        }     
    }
    
    private void openEditor(Class<?> editor,Object... args){
        
        Intent intent=new Intent(this,editor);
        intent.setData(getIntent().getData());
        intent.putExtra(EditorIntent.EXTRA_OPEN_MODE,EditorIntent.OPEN_MODE_IN_ZIP);
        intent.putExtra(EditorIntent.EXTRA_OPEN_ENTRY,(String)(args[0]));

        startActivity(intent);
        
    }

    //@Override
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
    
    static class ZAdapter extends BaseAdapter{     

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

        private List<ZipArchive.Entry> z_entries;
        private Context context;

        ZAdapter(Context context,List<ZipArchive.Entry> entries){
            z_entries=entries;
            this.context=context;
            sort();
        }

        ZAdapter(Context context,ZipArchive.Entry entry){
            z_entries=entry.listEntries();
            this.context=context;
            sort();
        }

        private void sort(){
            Collections.sort(z_entries,comparator_);
        }

        @Override
        public int getCount(){
            return z_entries.size();
        }

        @Override
        public Object getItem(int p1){
            return z_entries.get(p1);
        }

        @Override
        public long getItemId(int p1){
            return 0;
        }

        private LayoutInflater getLayoutInflater(){
            return (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getView(int pos,View curView,ViewGroup p3){
            TextView text;
            if(curView==null){
                text=(TextView)getLayoutInflater().inflate(android.R.layout.simple_list_item_1,null);
                //cache default textcolor
                text.setTag(text.getTextColors().getDefaultColor());
            }
            else
                text=(TextView)curView;

            ZipArchive.Entry entry=z_entries.get(pos);

            text.setText(entry.getName());

            int color=text.getTag();
            if(entry.isDirectory())
                color=(color^(255<<0))|(255<<16);
            text.setTextColor(color);

            //text.setBackgroundColor(color^0xffffff);

            return text;
        }     
    }
}
