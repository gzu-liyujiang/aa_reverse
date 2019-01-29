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
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import brut.androlib.AndrolibException;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResResSpec;
import brut.androlib.res.data.ResResource;
import brut.androlib.res.data.ResType;
import brut.androlib.res.data.ResTypeSpec;
import brut.androlib.res.data.value.ResArrayValue;
import brut.androlib.res.data.value.ResBoolValue;
import brut.androlib.res.data.value.ResColorValue;
import brut.androlib.res.data.value.ResIntValue;
import brut.androlib.res.data.value.ResScalarValue;
import brut.androlib.res.data.value.ResStringValue;
import brut.androlib.res.data.value.ResValue;
import brut.androlib.res.decoder.ARSCDecoder;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import android.widget.TextView;

public class ArscEditorActivity extends LoadActivity{

    private static final String LOG_TAG = Application.getLogTag()+ArscEditorActivity.class;

    private static final int DEEP_PACKAGES=0;
    private static final int DEEP_TYPE_SPECS=1;
    private static final int DEEP_TYPES=2;
    private static final int DEEP_RES_SPECS=3;

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
            switch(deep){
                case DEEP_PACKAGES:
                    break;
                case DEEP_TYPE_SPECS:
                    setPackagesAdapter();
                    break;
                case DEEP_TYPES:
                    setTypeSpecsAdapter();
                    break;
                case DEEP_RES_SPECS:
                    setTypesAdapter();
                    break;
            }
        }
        
        
    };

    private int deep;

    private ResPackage currentResPackage;
    private ResTypeSpec currentResTypeSpec;
    private String currentResType;

    private Map<ResPackage,Map<ResTypeSpec,Map<String/*ResType*/,List<Map<ResType,ResResSpec>>>>> packagesMap=new HashMap<>();

    private ListView listView;
    private ArrayAdapter<CharSequence> listAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list); 

        listView=(ListView)findViewById(android.R.id.list);
        listView.setOnItemClickListener(itemClick);
        listView.setFastScrollEnabled(true);

        listAdapter= new ArrayAdapter<CharSequence>(this,android.R.layout.simple_list_item_1);
        listView.setAdapter(listAdapter);

        findViewById(R.id.goto_parent_dir).setOnClickListener(gotoParentClick);
        
        startLoad();
    }

    @Override
    protected void loading() throws AndrolibException,IOException{

        String mode;
        InputStream is;
        String path=getIntent().getData().getPath();
        ZipFile zfile=null;

        mode=getIntent().getStringExtra("openMode");

        if(mode!=null&&mode.equals("zipArchive")){
            String entry=getIntent().getStringExtra("entry");   
            zfile=new ZipFile(path);
            ZipEntry zentry=zfile.getEntry(entry);
            is=zfile.getInputStream(zentry);
        }else{
            is=new FileInputStream(path);
        }

        ARSCDecoder.ARSCData arscData=ARSCDecoder.decode(is,true,true);

        if(arscData==null)
            throw new NullPointerException();

        // maping arsc data
        packagesMap.clear();

        ResPackage[] packages=arscData.getPackages();

        for(ResPackage rp:packages){

            List<ResType> typeList= rp.getConfigs();

            final int typeCount=typeList.size();
            final Map<ResTypeSpec,Map<String,List<Map<ResType,ResResSpec>>>> typesMap=new HashMap<>();

            packagesMap.put(rp,typesMap);

            for(int k=0;k<typeCount;k++){

                ResType rt=typeList.get(k);

                Set<ResResSpec> specs= rt.listResSpecs();
                Iterator<ResResSpec> iterator= specs.iterator();

                if(!iterator.hasNext())continue;

                for(ResResSpec spec=iterator.next();iterator.hasNext();spec=iterator.next()){
                    ResTypeSpec typeSpec=spec.getType();
                    Map<String,List<Map<ResType,ResResSpec>>> ResTypeMap=null;

                    if((ResTypeMap=typesMap.get(typeSpec))==null){
                        ResTypeMap=new HashMap<>();
                        typesMap.put(typeSpec,ResTypeMap);                         
                    }               

                    final String filepath=spec.getResource(rt).getFilePath();
                    String type=filepath.substring(0,filepath.indexOf('/'));

                    List<Map<ResType,ResResSpec>> ResSpecList=null;
                    if((ResSpecList=ResTypeMap.get(type))==null){
                        ResSpecList=new ArrayList<>();
                        ResTypeMap.put(type,ResSpecList);                        
                    }                 

                    Map<ResType,ResResSpec> map=new HashMap<>();
                    map.put(rt,spec);
                    ResSpecList.add(map);

                }
            }            
        }  

        if(zfile!=null)
            zfile.close();
    }

    @Override
    protected void onLoadDone(){
        setPackagesAdapter();
    }

    @Override
    protected void onLoadFailed(){
        String text=String.format(getString(R.string.open_file_err),"arsc");
        Toast.makeText(ArscEditorActivity.this,text,1).show();
        finish();
    }

    private void onListItemClick(ListView l,View v,int position,long id){
        final CharSequence name=(CharSequence)l.getAdapter().getItem(position);
        switch(deep){
            case DEEP_PACKAGES:{
                    Set<ResPackage> packages= packagesMap.keySet();
                    for(ResPackage rp:packages){
                        if(rp.getName().equals(name)){
                            currentResPackage=rp;
                            setTypeSpecsAdapter();
                            break;
                        }
                    }
                }
                break;
            case DEEP_TYPE_SPECS:{
                    Set<ResTypeSpec> typeSpecs=packagesMap.get(currentResPackage).keySet();
                    for(ResTypeSpec rt:typeSpecs){
                        if(rt.getName().equals(name)){
                            currentResTypeSpec=rt;
                            setTypesAdapter();
                            break;
                        }
                    }
                }
                break;
            case DEEP_TYPES:{
                    try{
                        Set<String> types=packagesMap.get(currentResPackage).get(currentResTypeSpec).keySet();
                        for(String t:types){
                            if(t.equals(name)){
                                currentResType=t;
                                setResSpecAdapter();
                                break;
                            }
                        }
                    }catch(AndrolibException e){}
                }
                break;
            case DEEP_RES_SPECS:{}
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            finish();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }       

    private ArrayAdapter clearAdapter(ArrayAdapter adapter){
        adapter.clear();
        return adapter;
    }

    private void setPackagesAdapter(){

        deep=DEEP_PACKAGES;

        ArrayAdapter adapter=clearAdapter(listAdapter);

        Set<ResPackage> packages= packagesMap.keySet();
        for(ResPackage rp:packages)
            adapter.add(rp.getName());
        
        ((TextView)findViewById(R.id.list_path_show))
            .setText(":");
        
    }

    private void setTypeSpecsAdapter(){
        deep=DEEP_TYPE_SPECS;

        ArrayAdapter<CharSequence> adapter=(ArrayAdapter<CharSequence>)clearAdapter(listAdapter);

        Map<ResTypeSpec,Map<String,List<Map<ResType,ResResSpec>>>> typesMap=packagesMap.get(currentResPackage);
        Set<ResTypeSpec> types= typesMap.keySet();
        for(ResTypeSpec t:types)
            adapter.add(t.getName());
            
        ((TextView)findViewById(R.id.list_path_show))
            .setText(":"+currentResPackage.getName());
        
    }

    private void setTypesAdapter(){
        deep=DEEP_TYPES;

        ArrayAdapter<CharSequence> adapter=(ArrayAdapter<CharSequence>)clearAdapter(listAdapter);

        Map<ResTypeSpec,Map<String,List<Map<ResType,ResResSpec>>>> typeSpecsMap=packagesMap.get(currentResPackage);

        Map<String,List<Map<ResType,ResResSpec>>> resTypesMap=typeSpecsMap.get(currentResTypeSpec);

        Set<String> resTypes=resTypesMap.keySet();

        for(String rt:resTypes){
            adapter.add(rt.toString());
        }
        
        ((TextView)findViewById(R.id.list_path_show))
            .setText(":"+currentResPackage.getName()+":"+currentResTypeSpec.getName());
        

    }

    private SpannableString generateSpannableString(
        String text1,
        ForegroundColorSpan text1Color,
        String text2,
        ForegroundColorSpan text2Color){
        final String allText=text1+"\n"+text2;
        final SpannableString sstr=new SpannableString(allText);     
        sstr.setSpan(text1Color,0,text1.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        sstr.setSpan(text2Color,text1.length()+1,allText.length(),Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        return sstr;
    }

    private void setResSpecAdapter() throws AndrolibException{
        deep=DEEP_RES_SPECS;

        ArrayAdapter<CharSequence> adapter=(ArrayAdapter<CharSequence>)clearAdapter(listAdapter);

        Map<ResTypeSpec,Map<String,List<Map<ResType,ResResSpec>>>> resTypeSpecMap=packagesMap.get(currentResPackage);

        Map<String,List<Map<ResType,ResResSpec>>> resTypesMap=resTypeSpecMap.get(currentResTypeSpec);

        List<Map<ResType,ResResSpec>> resSpecList=resTypesMap.get(currentResType);

        final ForegroundColorSpan purpleColorSpan=new ForegroundColorSpan(0xffff00ff);
        final ForegroundColorSpan greenColorSpan=new ForegroundColorSpan(0xff00ff00);

        for(Map<ResType,ResResSpec> rs:resSpecList){
            ResType type= rs.keySet().iterator().next();
            ResResSpec spec=rs.values().iterator().next();

            ResResource resource=spec.getResource(type);
            ResValue value=resource.getValue();

            SpannableString sstr=null;

            if(value instanceof ResIntValue)
                sstr=generateSpannableString(
                    spec.getName(),
                    purpleColorSpan,
                    Integer.toHexString(((ResIntValue)value).getValue()),
                    greenColorSpan
                );
            else if(value instanceof ResBoolValue)
                sstr=generateSpannableString(
                    spec.getName(),
                    purpleColorSpan,
                    Boolean.toString(((ResBoolValue)value).getValue()),
                    greenColorSpan
                );
            else if(value instanceof ResColorValue)
                sstr=generateSpannableString(
                    spec.getName(),
                    purpleColorSpan,
                    ((ResScalarValue)value).encodeAsResXmlValue(),
                    greenColorSpan
                );
            else if(value instanceof ResStringValue)
                sstr=generateSpannableString(
                    spec.getName(),
                    purpleColorSpan,
                    ((ResStringValue)value).encodeAsResXmlValue(),
                    greenColorSpan
                );
            else if(value instanceof ResArrayValue){
                ResScalarValue[] items=((ResArrayValue)value).getItems();
                String str="";
                for(ResScalarValue rv:items)
                    str+=rv.encodeAsResXmlItemValue()+"\n\n";

                sstr=generateSpannableString(
                    spec.getName(),
                    purpleColorSpan,
                    str,
                    greenColorSpan
                );
            }
            else
                sstr=generateSpannableString(
                    spec.getName(),
                    purpleColorSpan,
                    value.toString(),
                    greenColorSpan
                );

            adapter.add(sstr);
        }
        
        ((TextView)findViewById(R.id.list_path_show))
            .setText(":"
              +currentResPackage.getName()
              +":"
              +currentResTypeSpec.getName()
              +":"
              +currentResType);
        
    }
}
