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
package aenu.reverse.ui.util;

import aenu.reverse.ui.Application;
import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipInputStream;
import java.util.concurrent.ExecutorService;

public class ZipArchive{   

    private static final String LOG_TAG = Application.getLogTag()+ZipArchive.class;

    private final List<Entry> allEntries;

    private final ZipFile zipFile;

    public ZipArchive(String path) throws IOException{
        this(new File(path));
    }
    
    public ZipArchive(File f) throws IOException{

        zipFile=new ZipFile(f);

        final EntryMap dirEntries=new EntryMap();
        final EntryMap fileEntries=new EntryMap();  

        FileInputStream in=new FileInputStream(f);

        ZipInputStream zis=new ZipInputStream(in);

        ZipEntry ze;

        while((ze=zis.getNextEntry())!=null){

            final String fullPath=ze.getName();

            if(ze.isDirectory()){              
                String path=fullPath.substring(0,fullPath.length()-1);//suffix is /
                
                DirEntry de= new DirEntry(this,path,ze.getTime());    
                
                if(dirEntries.containsKey(path))
                    dirEntries.set(path,de);
                else
                    dirEntries.put(path,de);
                continue;
            }

            fileEntries.put(fullPath,new FileEntry(this,ze));      

            addVirtualDirEntries(fullPath,dirEntries);

            zis.closeEntry();
        }

        zis.close();

        in.close();

        allEntries=mergeEntriesList(fileEntries,dirEntries);
    }
    
    public final List<Entry> listRootEntries(){
        final List<Entry> entries=new ArrayList<Entry>();
        int size=allEntries.size();
        int len8=size>>3;

        int index=0;
        Entry e;
        for(int i=0;i<len8;i++,index+=8){
            if((e=allEntries.get(index|0)).getFullPath().indexOf('/')==-1)
                entries.add(e);
            if((e=allEntries.get(index|1)).getFullPath().indexOf('/')==-1)
                entries.add(e);
            if((e=allEntries.get(index|2)).getFullPath().indexOf('/')==-1)
                entries.add(e);
            if((e=allEntries.get(index|3)).getFullPath().indexOf('/')==-1)
                entries.add(e);   
            if((e=allEntries.get(index|4)).getFullPath().indexOf('/')==-1)
                entries.add(e);
            if((e=allEntries.get(index|5)).getFullPath().indexOf('/')==-1)
                entries.add(e);
            if((e=allEntries.get(index|6)).getFullPath().indexOf('/')==-1)
                entries.add(e);
            if((e=allEntries.get(index|7)).getFullPath().indexOf('/')==-1)
                entries.add(e);
        }
        if((size&4)!=0){
            if((e=allEntries.get(index|0)).getFullPath().indexOf('/')==-1)
                entries.add(e);
            if((e=allEntries.get(index|1)).getFullPath().indexOf('/')==-1)
                entries.add(e);
            if((e=allEntries.get(index|2)).getFullPath().indexOf('/')==-1)
                entries.add(e);
            if((e=allEntries.get(index|3)).getFullPath().indexOf('/')==-1)
                entries.add(e);   
            index+=4;
        }
        if((size&2)!=0){
            if((e=allEntries.get(index|0)).getFullPath().indexOf('/')==-1)
                entries.add(e);
            if((e=allEntries.get(index|1)).getFullPath().indexOf('/')==-1)
                entries.add(e);
            index+=2;
        }
        if((size&1)!=0){
            if((e=allEntries.get(index|0)).getFullPath().indexOf('/')==-1)
                entries.add(e);
        }
        return entries;
    }
    
    public void close(){
        try{
            zipFile.close();
        }catch(IOException e){}
    }
    
    private InputStream getInputStream(ZipEntry zEntry) throws IOException{
        return zipFile.getInputStream(zEntry);
    }
    
    /*
    private void addVirtualDirEntries(String fullPath,Map<String,ZipArchive.Entry> dirEntries){

        for(int p=0;;p++){
            if((p=fullPath.indexOf('/',p))==-1)
                break;
            String dirPath=fullPath.substring(0,p);
            if(!dirEntries.containsKey(dirPath)){
                dirEntries.put(dirPath,new DirEntry(this,dirPath));
            }
        }
    }*/
    
    private void addVirtualDirEntries(String fullPath,Map<String,ZipArchive.Entry> dirEntries){

        String dirPath = fullPath;
        
        for(int p;;){
            
            if((p=dirPath.lastIndexOf('/'))==-1)return;
            
            dirPath = dirPath.substring(0,p);
            
            if(dirEntries.containsKey(dirPath)){
                return;
            }          
            dirEntries.put(dirPath,new DirEntry(this,dirPath));         
        }
    }
    
    private List<Entry> mergeEntriesList(EntryList fileEntries,EntryList dirEntries){

        List<Entry> entries=new ArrayList<Entry>();

        int len8,index;

        final Entry[] f_entries=fileEntries.getAllEntries();
        final int f_entries_count=fileEntries.getAllEntriesCount();
        len8=f_entries_count>>3;
        index=0;
        for(int i=0;i<len8;i++,index+=8){
            entries.add(f_entries[index|0]);
            entries.add(f_entries[index|1]);
            entries.add(f_entries[index|2]);
            entries.add(f_entries[index|3]);
            entries.add(f_entries[index|4]);
            entries.add(f_entries[index|5]);
            entries.add(f_entries[index|6]);
            entries.add(f_entries[index|7]);          
        }
        if((f_entries_count&4)!=0){
            entries.add(f_entries[index|0]);
            entries.add(f_entries[index|1]);
            entries.add(f_entries[index|2]);
            entries.add(f_entries[index|3]);
            index+=4;
        }
        if((f_entries_count&2)!=0){
            entries.add(f_entries[index|0]);
            entries.add(f_entries[index|1]);
            index+=2;
        }
        if((f_entries_count&1)!=0){
            entries.add(f_entries[index|0]);
        }

        final Entry[] d_entries=dirEntries.getAllEntries();
        final int d_entries_count=dirEntries.getAllEntriesCount();

        len8=d_entries_count>>3;
        index=0;
        for(int i=0;i<len8;i++,index+=8){
            entries.add(d_entries[index|0]);
            entries.add(d_entries[index|1]);
            entries.add(d_entries[index|2]);
            entries.add(d_entries[index|3]);
            entries.add(d_entries[index|4]);
            entries.add(d_entries[index|5]);
            entries.add(d_entries[index|6]);
            entries.add(d_entries[index|7]);          
        }
        if((d_entries_count&4)!=0){
            entries.add(d_entries[index|0]);
            entries.add(d_entries[index|1]);
            entries.add(d_entries[index|2]);
            entries.add(d_entries[index|3]);
            index+=4;
        }
        if((d_entries_count&2)!=0){
            entries.add(d_entries[index|0]);
            entries.add(d_entries[index|1]);
            index+=2;
        }
        if((d_entries_count&1)!=0){
            entries.add(d_entries[index|0]);
        }

        return entries;
    }

    static interface EntryList{
        ZipArchive.Entry[] getAllEntries();
        int getAllEntriesCount();
    };

    static class EntryMap implements Map<String,Entry>,EntryList{

        @Override
        public ZipArchive.Entry[] getAllEntries(){
            return values;
        }

        @Override
        public int getAllEntriesCount(){
            return count;
        }

        @Override
        public void clear(){
            reset();
        }

        @Override
        public boolean containsKey(Object key){
            return get(key)!=null;
        }

        @Override
        public boolean containsValue(Object p1){
            throw new UnsupportedOperationException();              
        }

        @Override
        public Set<Map.Entry<String,ZipArchive.Entry>> entrySet(){
            throw new UnsupportedOperationException();             
        }

        @Override
        public ZipArchive.Entry get(Object key){

            if(!(key instanceof String))
                return null;

            final int l8=count>>3;
            int index=0;
            for(int i=0;i<l8;i++,index+=8){
                if(keys[index|0].equals(key))return values[index|0];
                if(keys[index|1].equals(key))return values[index|1];
                if(keys[index|2].equals(key))return values[index|2];
                if(keys[index|3].equals(key))return values[index|3];
                if(keys[index|4].equals(key))return values[index|4];
                if(keys[index|5].equals(key))return values[index|5];
                if(keys[index|6].equals(key))return values[index|6];
                if(keys[index|7].equals(key))return values[index|7];                  
            }
            if((count&4)==4){
                if(keys[index|0].equals(key))return values[index|0];
                if(keys[index|1].equals(key))return values[index|1];
                if(keys[index|2].equals(key))return values[index|2];
                if(keys[index|3].equals(key))return values[index|3];
                index+=4;
            }
            if((count&2)==2){
                if(keys[index|0].equals(key))return values[index|0];
                if(keys[index|1].equals(key))return values[index|1];
                index+=2;
            }
            if((count&1)==1){
                if(keys[index].equals(key))return values[index];
            }
            return null;
        }

        @Override
        public boolean isEmpty(){
            return count==0;
        }

        @Override
        public Set<String> keySet(){
            throw new UnsupportedOperationException();               
        }

        @Override
        public ZipArchive.Entry put(String key,ZipArchive.Entry value){
            keys[count]=key;
            values[count]=value;

            if(++count==max_count)
                updateKeysAndValues();

            return value;
        }

        @Override
        public void putAll(Map<? extends String, ? extends ZipArchive.Entry> p1){
            throw new UnsupportedOperationException();
        }

        @Override
        public ZipArchive.Entry remove(Object key){
            throw new UnsupportedOperationException();
        }

        @Override
        public int size(){
            return count;
        }

        @Override
        public Collection<ZipArchive.Entry> values(){
            throw new UnsupportedOperationException();
        }

        private void reset(){
            keys=new String[DEFAULT_SIZE];
            values=new ZipArchive.Entry[DEFAULT_SIZE];
            count=0;
            max_count=DEFAULT_SIZE;
        }

        public void set(String key,ZipArchive.Entry value){
            if(!(key instanceof String))
                return;

            final int l8=count>>3;
            int index=0;
            for(int i=0;i<l8;i++,index+=8){
                if(keys[index|0].equals(key)){values[index|0]=value;return;}
                if(keys[index|1].equals(key)){values[index|1]=value;return;}
                if(keys[index|2].equals(key)){values[index|2]=value;return;}
                if(keys[index|3].equals(key)){values[index|3]=value;return;}
                if(keys[index|4].equals(key)){values[index|4]=value;return;}
                if(keys[index|5].equals(key)){values[index|5]=value;return;}
                if(keys[index|6].equals(key)){values[index|6]=value;return;}
                if(keys[index|7].equals(key)){values[index|7]=value;return;}                  
            }
            if((count&4)==4){
                if(keys[index|0].equals(key)){values[index|0]=value;return;}
                if(keys[index|1].equals(key)){values[index|1]=value;return;}
                if(keys[index|2].equals(key)){values[index|2]=value;return;}
                if(keys[index|3].equals(key)){values[index|3]=value;return;}
                index+=4;
            }
            if((count&2)==2){
                if(keys[index|0].equals(key)){values[index|0]=value;return;}
                if(keys[index|1].equals(key)){values[index|1]=value;return;}
                index+=2;
            }
            if((count&1)==1){
                if(keys[index].equals(key)){values[index]=value;return;}
            }
        }
        
        private void updateKeysAndValues(){

            int maxcount=this.max_count<<1;
            String[] keys=new String[maxcount];
            ZipArchive.Entry[] values=new ZipArchive.Entry[maxcount];    

            System.arraycopy(this.keys,0,keys,0,this.max_count);
            System.arraycopy(this.values,0,values,0,this.max_count);

            this.keys=keys;
            this.values=values;
            this.max_count=maxcount;
        }

        static final int DEFAULT_SIZE=1024;
        String[] keys;
        ZipArchive.Entry[] values;
        int count;
        int max_count;
        public EntryMap(){
            reset();
        }

    }

    public static abstract class Entry{

        public abstract InputStream getInputStream();
        public abstract boolean isDirectory();
        public abstract List<Entry> listEntries();
        public abstract Entry getParentEntry();
        public abstract long getSize();
        public abstract long getTime();
        public abstract String getName();
        public abstract String getFullPath();        

        @Override
        public boolean equals(Object o){
            try{
                Entry e=(Entry)o;
                if(getFullPath().equals(e.getFullPath()))
                    return isDirectory()==e.isDirectory();
            }catch(Exception e){
                return false;
            }
            return false;
        }       
    }//!Entry

    public static class FileEntry extends Entry{

        private ZipArchive zipArchive;
        private ZipEntry zEntry;

        FileEntry(ZipArchive za,ZipEntry ze){
            zipArchive=za;
            zEntry=ze;
        }

        public boolean isDirectory(){
            return false;
        }

        public InputStream getInputStream(){
            try{
                return zipArchive.getInputStream(zEntry);
            }catch(IOException e){
                Log.i(LOG_TAG,e.toString());
                return null;
            }
        }

        public List<Entry> listEntries(){
            throw new UnsupportedOperationException();             
        }

        public Entry getParentEntry(){

            String fullPath=getFullPath();

            int p=fullPath.lastIndexOf('/');
            if(p==-1) return null;

            String parentPath=fullPath.substring(0,p);

            return new DirEntry(zipArchive,parentPath);
        } 

        public long getSize(){
            return zEntry.getSize();
        }

        public long getTime(){
            return zEntry.getTime();
        }

        public String getName(){
            String fullPath=getFullPath();
            int p=fullPath.lastIndexOf('/');

            if(p==-1) return fullPath;

            return fullPath
                .substring(p+1,fullPath.length());
        }
        @Override
        public String getFullPath(){
            return zEntry.getName();
        }


    }//!FileEntry

    public static class DirEntry extends Entry{

        @Override
        public InputStream getInputStream(){
            throw new UnsupportedOperationException();              
        }

        @Override
        public boolean isDirectory(){
            return true;
        }

        @Override
        public List<ZipArchive.Entry> listEntries(){
            List<Entry> entries=new ArrayList<>();
            String h_filter=entryFullPath+"/";
            for(Entry ze:zipArchive.allEntries){
                if(ze.getFullPath().startsWith(h_filter)){
                    if(ze.getFullPath().indexOf('/',h_filter.length())==-1)
                        entries.add(ze);
                }                 
            }
            return entries;
        }

        @Override
        public ZipArchive.Entry getParentEntry(){
            int p=entryFullPath.lastIndexOf('/');
            if(p==-1)return null;         
            String parentPath=entryFullPath.substring(0,p);
            return new DirEntry(zipArchive,parentPath);
        }

        @Override
        public long getSize(){
            return 0;
        }

        @Override
        public long getTime(){
            return entryDate;
        }

        @Override
        public String getName(){
            int p=entryFullPath.lastIndexOf('/');
            if(p==-1)return entryFullPath;

            return entryFullPath
                .substring(p+1,entryFullPath.length());
        }

        @Override
        public String getFullPath(){
            return entryFullPath;
        }


        private String entryFullPath;
        private long entryDate;
        private ZipArchive zipArchive;
        
        DirEntry(ZipArchive za,String entry_fullpath){
            this(za,entry_fullpath,0);
        }
        
        DirEntry(ZipArchive za,String entry_fullpath,long entry_date){
            zipArchive=za;
            entryFullPath=entry_fullpath;
            entryDate=entry_date;
        }
    }//!DirEntry   
}//!ZipArchive
