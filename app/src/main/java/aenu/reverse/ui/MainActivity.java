/*
Copyright 2018 by aenu

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package aenu.reverse.ui;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileFilter;
import java.text.Collator;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import android.os.Handler;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.IOException;

public class MainActivity extends ListActivity{

    
    
    private final static String LOG_TAG=Application.getLogTag()+ListActivity.class;
    
    private final static int MODE_FILE=0;
    private final static int MODE_APP=1;
   
    private final static int DIALOG_APK_OPTIONS=0;
    private final static int DIALOG_SIGNATURE=1;
    
    private File currentDir=Environment.getExternalStorageDirectory();
    private int showMode;
    private SharedPreferences preferences;
    private final File APP_DIR=new File("/sdcard/aa_reverse");
    private final File APP_PRIVATE_DIR=new File("/sdcard/.aa_reverse");
    
    private final View.OnClickListener l=new View.OnClickListener(){
        @Override
        public void onClick(View p1){
            File parentFile=currentDir.getParentFile();
            if(parentFile!=null)
                currentDir=parentFile;
            setAdapter(MODE_FILE);
        }
    };
    
    @Override
    public void onCreate(Bundle savedInstanceState){
        
        super.onCreate(savedInstanceState);
        
        preferences=PreferenceManager.getDefaultSharedPreferences(this);
              
        setContentView(R.layout.activity_list);
        
        showMode=preferences.getInt("showMode",MODE_FILE);
        
        currentDir=new File(preferences.getString("lastDir",currentDir.getAbsolutePath()));
            
        findViewById(R.id.goto_parent_dir).setOnClickListener(l);
        initialize();
        setAdapter(showMode);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        super.onPrepareOptionsMenu(menu);
        updateActionBar(menu.findItem(R.id.menu_show_mode));
        return true;
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        super.onOptionsItemSelected(item);
        switch(item.getItemId()){
            case R.id.menu_show_mode:
                setAdapter(MODE_FILE^MODE_APP^showMode);
                updateActionBar(item);
                break;        
            case R.id.menu_settings:
                startActivity(new Intent(this,SettingsActivity.class));
                break;       
        }
        return true;
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }

    @Override
    public void finish(){
        super.finish();
        preferences.edit()
            .putInt("showMode",showMode)
            .putString("lastDir",currentDir.getAbsolutePath())
            .commit();
    }

    @Override
    protected void onListItemClick(ListView l,View v,int position,long id){
        File f=((SelectAdapter)l.getAdapter()).getFile(position);
        if(f.isFile()){
            if(f.getName().endsWith(".apk"))
                startActivity(new Intent(MainActivity.this,ApkInBrowseActivity.class)
                              .setData(Uri.fromFile(f)));            
            else if(f.getName().endsWith(".jar"))
                startActivity(new Intent(this,JarInBrowseActivity.class)
                              .setData(Uri.fromFile(f)));
            else if(f.getName().endsWith(".dex"))
                startActivity(new Intent(this,DexEditorActivity.class)
                              .putExtra(EditorIntent.EXTRA_OPEN_MODE,EditorIntent.OPEN_MODE_FILE)
                              .setData(Uri.fromFile(f)));
            else if(f.getName().endsWith(".arsc"))
                startActivity(new Intent(this,ArscEditorActivity.class)
                              .putExtra(EditorIntent.EXTRA_OPEN_MODE,EditorIntent.OPEN_MODE_FILE)
                              .setData(Uri.fromFile(f)));
            else if(f.getName().endsWith(".xml"))
                startActivity(new Intent(this,XmlEditorActivity.class)
                              .putExtra(EditorIntent.EXTRA_OPEN_MODE,EditorIntent.OPEN_MODE_FILE)
                              .setData(Uri.fromFile(f)));
            else if(f.getName().endsWith(".class"))
                startActivity(new Intent(this,CTJ_EditorActivity.class)
                              .putExtra(EditorIntent.EXTRA_OPEN_MODE,EditorIntent.OPEN_MODE_FILE)
                              .setData(Uri.fromFile(f)));
            
        }        
        else
            changeDir(f);
    }
    
    private void initialize(){
        final ProgressDialog pd = ProgressDialog.show(this, null, "Initialize ...", true, false);
        new Thread(){
            public void run(){
                APP_DIR.mkdirs();
                APP_PRIVATE_DIR.mkdirs();
                final String framework_jar="framework-28.jar";
                File framework=new File(APP_PRIVATE_DIR,framework_jar);
                if(!framework.exists()){
                    try{
                        FileOutputStream out=new FileOutputStream(framework);
                        InputStream in=getAssets().open(framework_jar);
                        byte[] buf=new byte[4096];
                        int n;
                        while((n=in.read(buf))!=-1)
                            out.write(buf,0,n);
                            
                        in.close();
                        out.close();
                    }catch (IOException e) {}
                }
                
                runOnUiThread(new Runnable(){
                    @Override
                    public void run(){
                        pd.dismiss();
                    }
                });
            }
        }.start();
    }
    
    private void changeDir(File f){
        if(f.canRead())currentDir=f;
        setAdapter(MODE_FILE);
    }
    
    private final class setAdapterH extends Handler{
        
        private Dialog d;
        public SelectAdapter adapter;
        
        private void showLoadingDialog(){
            if(d!=null){
                d.dismiss();
                d=null;
            }
            
            d=LoadActivity.createLoadingDialog(
              MainActivity.this,
              MainActivity.this.getText(R.string.loading)
            );
            d.show();
        }
        
        private void hideLoadingDialog(){
            if(d!=null)
                d.dismiss();
            d=null;
        }
        
        public void handleMessage(android.os.Message msg){
            hideLoadingDialog();
            MainActivity.this.setListAdapter(adapter);
        }
    };
    
    private final class setAdapterT extends Thread{
        
        final int mode;
        setAdapterT(int mode){
            this.mode=mode;
        }
        
        public void run(){
            try{
                switch(mode){
                    case MODE_APP:
                        setAdapter.adapter=new AppAdapter(MainActivity.this);
                    break;
                    case MODE_FILE:
                        setAdapter.adapter=new FileAdapter(MainActivity.this,currentDir);
                    break;
                }        
            }
            finally{
                setAdapter.sendEmptyMessage(0);
            }
        }
    }
    
    private final setAdapterH setAdapter=new setAdapterH();
    
    private void setAdapter(int mode){
        
        showMode=mode;
        
        if(mode==MODE_APP)
            findViewById(R.id.list_navigation).setVisibility(View.GONE);
        else
            findViewById(R.id.list_navigation).setVisibility(View.VISIBLE);
        
        if(mode==MODE_FILE)
            ((TextView)findViewById(R.id.list_path_show)).setText(currentDir.getAbsolutePath());
        
        setAdapter.showLoadingDialog();
        new setAdapterT(mode).start();    
    }
    
    private void updateActionBar(MenuItem item){
        int icon=showMode==MODE_APP?R.drawable.ic_menu_app:R.drawable.ic_menu_archive;
        int text=showMode==MODE_APP?R.string.select_app:R.string.select_file;
        getActionBar().setTitle(text);
        getActionBar().setIcon(icon);
        
        item.setTitle(R.string.select_app^R.string.select_file^text);
        item.setIcon(R.drawable.ic_menu_app^R.drawable.ic_menu_archive^icon);
    }
    
    public static abstract class SelectAdapter extends BaseAdapter{
        public abstract File getFile(int pos);
        public abstract String getOutputDirName(int pos);
    }
    
    private static class AppAdapter extends SelectAdapter {

        static class Appinfo{
            int id;
            Drawable icon;
            String label;
            String version;
        }
        private PackageManager packageManager_;
        private Context __context;
        
        private List<PackageInfo> __packageInfoList;
        
        private final Comparator<PackageInfo> comparator_ = new Comparator<PackageInfo>(){
            final Collator collator =Collator.getInstance();
            @Override
            public int compare(PackageInfo p1,PackageInfo p2){
                return collator.compare(
                    p1.applicationInfo.loadLabel(packageManager_),
                    p2.applicationInfo.loadLabel(packageManager_));   
            }
        };

        private AppAdapter(Context context){   
            __context=context;     
            packageManager_=__context.getPackageManager();   
            
            List<PackageInfo> packInfoList= packageManager_.getInstalledPackages(PackageManager.GET_UNINSTALLED_PACKAGES);     
            Collections.sort(packInfoList,comparator_);
            __packageInfoList=packInfoList;
        }
        
        public File getFile(int pos){
            return new File(__packageInfoList.get(pos).applicationInfo.sourceDir);
        }
        
        @Override
        public String getOutputDirName(int pos) {
            return __packageInfoList.get(pos).packageName;
        }
        
        @Override
        public int getCount(){
            return __packageInfoList.size();
        }

        public String getSourcePath(int pos){
            return __packageInfoList.get(pos).applicationInfo.sourceDir;
        }

        public String getPacketName(int pos){
            return __packageInfoList.get(pos).packageName;
        }

        @Override
        public Object getItem(int p1){
            return __packageInfoList.get(p1);
        }

        @Override
        public long getItemId(int p1){
            return 0;
        }

        private View layoutInflate(Context c,int resid){
            LayoutInflater li=(LayoutInflater)c.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            return li.inflate(resid,null);
        }
        
        private Drawable getAppIcon(int pos){
            return __packageInfoList.get(pos).applicationInfo.loadIcon(packageManager_);
        }
        
        private String getAppLabel(int pos){
            return __packageInfoList.get(pos).applicationInfo.loadLabel(packageManager_).toString();           
        }
        
        private String getAppVersion(int pos){
            PackageInfo info=__packageInfoList.get(pos);
            return info.versionName +"("+info.versionCode+")";         
        }
        
        private Appinfo newAppinfo(int pos){
            Appinfo info=new Appinfo();
            info.id=pos;
            info.icon=getAppIcon(pos);
            info.label=getAppLabel(pos);
            info.version=getAppVersion(pos);
            return info;
        }
        
        @Override
        public View getView(int pos,View curView,ViewGroup vg){
            if(curView==null){
                curView=layoutInflate(__context,R.layout.list_item_1);            
            }
            
            final int KEY_OFF=0xAA000000;//与系统资源冲突会产生 IllegalArgumentException
            
            //if(pos>0x01ffffff)throw new ArrayStoreException();
            
            if(curView.getTag(KEY_OFF|pos)==null)
                curView.setTag(KEY_OFF|pos,newAppinfo(pos));   
                
            Appinfo info=(Appinfo)curView.getTag(KEY_OFF|pos);
            
            curView.findViewById(android.R.id.icon).setBackgroundDrawable(info.icon);          
            ((TextView)curView.findViewById(android.R.id.text1)).setText(info.label);          
            ((TextView)curView.findViewById(android.R.id.text2)).setText(info.version);  
            return curView;
        }
    }//!AppAdapter
    
    private static class FileAdapter extends SelectAdapter {

        private static final FileFilter filter_=new FileFilter(){
            private final String suffix[]={
                ".apk",
                ".jar",
                ".dex",
                ".xml",
                ".arsc",
                ".class"
            };
            @Override
            public boolean accept(File p1){
                if(p1.isDirectory())
                    return true;
                final String name=p1.getName();
                for(String s:suffix)
                    if(name.endsWith(s))
                        return true;
                return false;
            }         
        };

        private static final Comparator<File> comparator_ = new Comparator<File>(){
            final Collator collator =Collator.getInstance(); 
            @Override
            public int compare(File p1,File p2){
                if(p1.isDirectory()&&p2.isFile())
                    return -1;
                else if(p1.isFile()&&p2.isDirectory())
                    return 1;
                return collator.compare(p1.getName(),p2.getName());
            }
        };
        
        private File[] fileList_;
        private Context context_; 

        private FileAdapter(Context context,File file){
            context_=context;
                            
            if(file!=null&&file.isDirectory())
                fileList_=getFileList(file);
            else
                fileList_=getFileList(new File("/"));
        }

        private File[] getFileList(File f){
            File[] files=f.listFiles(filter_);               
            Arrays.sort(files,comparator_);
            return files;
        }
        
        @Override
        public String getOutputDirName(int pos) {
            String file_name=fileList_[pos].getName();
            int end=file_name.length()>4?file_name.length()-4:file_name.length();
            return file_name.substring(0,end);
        }
        
        public File getFile(int pos){
            return fileList_[pos];
        }
        
        @Override
        public int getCount(){
            return fileList_.length;
        }

        @Override
        public Object getItem(int p1){
            return fileList_[p1];
        }

        @Override
        public long getItemId(int p1){
            return p1;
        }
        
        private LayoutInflater getLayoutInflater(){
            return (LayoutInflater)context_.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }
        
        private String getTimeStr(long time){
            return DateFormat.format("yyyy-MM-dd hh:mm:ss",time).toString();
        }

        private String getSizeStr(long size){
            double s=(double)size;
            double u;
            String suffix;
            if(s>(u=1L<<40L)){
                suffix="tb";
            }
            else if(s>(u=1L<<30L)){
                suffix="gb";
            }
            else if(s>(u=1L<<20L)){
                suffix="mb";
            }
            else if(s>(u=1L<<10L)){
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
            
            File f=fileList_[pos];

            ImageView icon=(ImageView)curView.findViewById(android.R.id.icon);

            icon.setImageResource(
                f.isDirectory()
                ?R.drawable.ic_folder
                :R.drawable.ic_file);
       
            TextView name=(TextView)curView.findViewById(android.R.id.text1);
            name.setText(f.getName());
         
            TextView hint=(TextView)curView.findViewById(android.R.id.text2);
            String hihtStr=getTimeStr(f.lastModified());
            if(!f.isDirectory())
                hihtStr+=" "+getSizeStr(f.length());
            hint.setText(hihtStr);
            
            return curView;
        } 
    }//!FileAdapter
}
