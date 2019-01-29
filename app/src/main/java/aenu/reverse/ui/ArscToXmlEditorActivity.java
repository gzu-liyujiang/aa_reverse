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
import java.io.InputStream;
import java.util.zip.ZipFile;
import java.io.FileInputStream;
import java.util.Map;
import java.util.HashMap;
import brut.androlib.res.data.ResTable;
import brut.androlib.res.AndrolibResources;
import brut.androlib.AndrolibException;
import brut.androlib.res.decoder.ARSCDecoder;
import brut.androlib.res.util.ExtMXSerializer;
import brut.androlib.res.util.ExtXmlSerializer;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResValuesFile;
import java.io.ByteArrayOutputStream;
import brut.androlib.res.data.ResResource;
import brut.androlib.res.xml.ResValuesXmlSerializable;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.AdapterView;
import android.view.View;
import com.myopicmobile.textwarrior.android.FreeScrollingTextField;
import com.myopicmobile.textwarrior.android.YoyoNavigationMethod;
import android.widget.ArrayAdapter;
import java.util.Comparator;
import android.widget.Toast;
import android.view.KeyEvent;
import com.myopicmobile.textwarrior.common.Document;
import com.myopicmobile.textwarrior.common.DocumentProvider;
import java.io.IOException;
import android.preference.PreferenceManager;

public class ArscToXmlEditorActivity extends LoadActivity
{
    private Map<String,String> atx_Maps;//aesc to xml list map
   
    private ListView listView;
    private FreeScrollingTextField xmlCodeView;
    
    private View currentView;  
    
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
        
        xmlCodeView=(FreeScrollingTextField)getLayoutInflater().inflate(R.layout.activity_code,null);
        xmlCodeView.setShowLineNumbers(true);
        xmlCodeView.setHighlightCurrentRow(true);
        xmlCodeView.setNavigationMethod(new YoyoNavigationMethod(xmlCodeView));
        
        startLoad();
    }
    
    @Override
    protected void loading(){
        try{
            String mode;
            InputStream in;
            String path=getIntent().getData().getPath();
            ZipFile zfile=null;

            mode=getIntent().getStringExtra(EditorIntent.EXTRA_OPEN_MODE);

            if(mode!=null&&mode.equals(EditorIntent.OPEN_MODE_IN_ZIP)){
                String entry=getIntent().getStringExtra(EditorIntent.EXTRA_OPEN_ENTRY);    
                zfile=new ZipFile(path);
                in=zfile.getInputStream(zfile.getEntry(entry));
            }else{
                in=new FileInputStream(path);
            }

            String framework_path=Preferences.decompile_getFrameworkPath(PreferenceManager.getDefaultSharedPreferences(this));
            atx_Maps=decoce(framework_path,in,zfile);

            in.close();

            if(zfile!=null)
                zfile.close();
        }catch(IOException e){
            throw new RuntimeException(e);
        }
        catch(AndrolibException e){
            throw new RuntimeException(e);           
        }
    }

    @Override
    protected void onLoadDone(){
        showXmlList();
    }

    @Override
    protected void onLoadFailed(){
        String text=String.format(getString(R.string.open_file_err),"arsc");
        Toast.makeText(this,text,1).show();
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode,KeyEvent event){
        if(keyCode==KeyEvent.KEYCODE_BACK){
            if(currentView==listView)
                finish();
            else
                showXmlList();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }
    
    private void onListItemClick(ListView l,View v,int position,long id){
        showXmlCode((String)l.getAdapter().getItem(position));
    }
    
    private void showXmlList(){
        if(listView.getAdapter()==null){
            ArrayAdapter<String> adapter=new ArrayAdapter<>(this,android.R.layout.simple_list_item_1);
            for(String str:atx_Maps.keySet())
                adapter.add(str);
            adapter.sort(new Comparator<String>(){
                @Override
                public int compare(String p1,String p2){
                    return p1.compareTo(p2);
                }
            });      
                
            listView.setAdapter(adapter);
        }
        
        setContentView(currentView=listView);
    }
    
    private void showXmlCode(String key){
        Document doc=new Document(xmlCodeView);
        doc.setText(atx_Maps.get(key));
        doc.setWordWrap(false);

        xmlCodeView.setDocumentProvider(new DocumentProvider(doc));
        xmlCodeView.setLanguage(JavaLanguage.instance);//FIXME
        xmlCodeView.respan();
        xmlCodeView.invalidate();
        
        setContentView(currentView=xmlCodeView);
    }
    
    public static Map<String/*Path*/,String/*Source*/> decoce(String framework_path,InputStream in,ZipFile apk) throws AndrolibException{       
    
        final AndrolibResources androidRes=new AndrolibResources(framework_path);
        ResTable resTable=apk!=null? androidRes.getResTable(apk,true):new ResTable();
        return decoce(in,resTable);      
    }
    
    public static Map<String/*Path*/,String/*Source*/> decoce(InputStream in,ResTable resTable) throws AndrolibException{      
        final Map<String,String> xml_map=new HashMap<>();

        ARSCDecoder.ARSCData data=ARSCDecoder.decode(in,true,true,resTable);

        ExtMXSerializer xmlSerializer = getResXmlSerializer();

        ResPackage res_package= data.getResTable().listMainPackages().iterator().next();

        for (ResValuesFile valuesFile : res_package.listValuesFiles()) {           
            generateValuesFile(valuesFile,xml_map,xmlSerializer);
        }

        return xml_map;      
    }
    
    public static ExtMXSerializer getResXmlSerializer() {
        ExtMXSerializer serial = new ExtMXSerializer();
        serial.setProperty(ExtXmlSerializer.PROPERTY_SERIALIZER_INDENTATION, "    ");
        serial.setProperty(ExtXmlSerializer.PROPERTY_SERIALIZER_LINE_SEPARATOR, System.getProperty("line.separator"));
        serial.setProperty(ExtXmlSerializer.PROPERTY_DEFAULT_ENCODING, "utf-8");
        serial.setDisabledAttrEscape(true);
        return serial;
    }
    
    private static void generateValuesFile(ResValuesFile valuesFile, Map<String,String> out
                                           ,         ExtMXSerializer     serial        ) throws AndrolibException {
        try {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            serial.setOutput((outStream), null);
            serial.startDocument(null, null);
            serial.startTag(null, "resources");
            
            for (ResResource res : valuesFile.listResources()) {
                if (valuesFile.isSynthesized(res)) {
                    continue;
                }
                ((ResValuesXmlSerializable) res.getValue()).serializeToResValuesXml(serial, res);
            }

            serial.endTag(null, "resources");
            serial.newLine();
            serial.endDocument();
            serial.flush();
            outStream.close();
            out.put(valuesFile.getPath(),outStream.toString());
        } catch (Exception ex) {
            throw new AndrolibException("Could not generate: " + valuesFile.getPath(), ex);
        }
    }
}
