
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
import android.util.Log;
import brut.androlib.AndrolibException;
import brut.androlib.res.AndrolibResources;
import brut.androlib.res.data.ResPackage;
import brut.androlib.res.data.ResTable;
import brut.androlib.res.decoder.AXmlResourceParser;
import brut.androlib.res.decoder.ResAttrDecoder;
import com.myopicmobile.textwarrior.android.FreeScrollingTextField;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.zip.ZipFile;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import java.io.IOException;
import brut.androlib.res.android.TypedValue;
import android.os.Bundle;
import com.myopicmobile.textwarrior.android.YoyoNavigationMethod;
import android.widget.Toast;
import com.myopicmobile.textwarrior.common.Document;
import com.myopicmobile.textwarrior.common.DocumentProvider;
import android.preference.PreferenceManager;

public class XmlEditorActivity extends LoadActivity{

    private boolean isAxml;
    private String xmlText;
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
        InputStream in;
        String path=getIntent().getData().getPath();
        ZipFile zfile=null;
        ByteArrayOutputStream cache=new ByteArrayOutputStream();

        mode=getIntent().getStringExtra(EditorIntent.EXTRA_OPEN_MODE);

        if(mode!=null&&mode.equals(EditorIntent.OPEN_MODE_IN_ZIP)){
            String entry=getIntent().getStringExtra(EditorIntent.EXTRA_OPEN_ENTRY);      
            zfile=new ZipFile(path);                
            in=zfile.getInputStream(zfile.getEntry(entry));
        }
        else{
            in=new FileInputStream(path);
        }

        {//copy input stream data
            final byte[] buf=new byte[4096];
            int n;

            cache.reset();
            while((n=in.read(buf))!=-1)
                cache.write(buf,0,n);

            in.close();//close stream          
            //reset stream
            in=new ByteArrayInputStream(cache.toByteArray());               
        }

        {//check is axml
            int chunk=0;
            for(int i=0;i<4;i++){
                chunk|=in.read()<<i*8;
            }
            if(chunk==0x00080003)
                isAxml=true;
            else
                isAxml=false;

            in.close();
            //reset stream
            in=new ByteArrayInputStream(cache.toByteArray());                         
        }

        if(isAxml){
            String framework_path=Preferences.decompile_getFrameworkPath(PreferenceManager.getDefaultSharedPreferences(this));
            xmlText=parserAxml(framework_path,zfile,in);
        }
        else
            xmlText=new String(cache.toByteArray());
            
        in.close();          

        if(zfile!=null)
            zfile.close();
    }       
    
    @Override
    protected void onLoadDone(){
        Document doc=new Document(free_text_view);
        doc.setText(xmlText);
        doc.setWordWrap(false);//设置为true会无法滑动！

        free_text_view.setDocumentProvider(new DocumentProvider(doc));
        free_text_view.setLanguage(JavaLanguage.instance);//FIXME
        free_text_view.respan();
        free_text_view.invalidate();
    }

    @Override
    protected void onLoadFailed(){
        String text=String.format(getString(R.string.open_file_err),"xml");
        Toast.makeText(XmlEditorActivity.this,text,1).show();
        finish();
    }  
    
    public static String parserAxml(String framework_path,ZipFile apk,InputStream in) throws AndrolibException, XmlPullParserException, IOException{

        final AndrolibResources androidRes=new AndrolibResources(framework_path);
        final AXmlResourceParser parser=new AXmlResourceParser();

        ResTable resTable=apk!=null? androidRes.getResTable(apk,true):null;
        ResAttrDecoder attrDecoder=null;

        if(resTable!=null){
            ResPackage rp=resTable.listMainPackages().iterator().next();

            attrDecoder=new ResAttrDecoder();
            attrDecoder.setCurrentPackage(rp);
        }

        parser.setAttrDecoder(attrDecoder);

        StringBuilder str_builder=new StringBuilder();       
        StringBuilder indent=new StringBuilder(32);
        final String indentStep="  ";

        parser.open(in);
        
        int type=parser.next();

        for(;;){

            if(type==XmlPullParser.END_DOCUMENT){
                parser.close();
                return str_builder.toString();
            }

            switch(type){
                case XmlPullParser.START_DOCUMENT:
                    {
                        str_builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
                        type=parser.next();
                        break;
                    }
                case XmlPullParser.START_TAG:
                    {            
                        int namespaceCountBefore=parser.getNamespaceCount(parser.getDepth()-1);
                        int namespaceCount=parser.getNamespaceCount(parser.getDepth());      
                        int attributeCount=parser.getAttributeCount();
                        {
                            String format=attributeCount!=0?"%s<%s%s\n":"%s<%s%s";

                            String prefix=parser.getPrefix();
                            prefix=prefix!=null?prefix.length()!=0?prefix+":":"":"";

                            str_builder.append(String.format(format,
                                                             indent,
                                                             prefix,
                                                             parser.getName()));


                        }

                        indent.append(indentStep);

                        int i;

                        for(i=namespaceCountBefore;i!=namespaceCount;++i){
                            String format=attributeCount!=0?"%sxmlns:%s=\"%s\"\n":"%sxmlns:%s=\"%s\"";
                            
                            str_builder.append(String.format(format,
                                                             indent,
                                                             parser.getNamespacePrefix(i),
                                                             parser.getNamespaceUri(i)));
                        }

                        for(i=0;i<attributeCount;++i){
                            String format=i!=attributeCount-1?"%s%s%s=\"%s\"\n":"%s%s%s=\"%s\"";

                            String prefix=parser.getAttributePrefix(i);
                            prefix=prefix!=null?prefix.length()!=0?prefix+":":"":"";
                            
                            str_builder.append(String.format(format,
                                                             indent,
                                                             prefix,
                                                             parser.getAttributeName(i),
                                                             attrDecoder!=null
                                                             ?parser.getAttributeValue(i)
                                                             :axml_getAttributeValue(parser,i)));
                        }

                        type=parser.next();
                        if(type==XmlPullParser.END_TAG){
                            str_builder.append("/>\n");
                            indent.setLength(indent.length()-indentStep.length());                           
                            type=parser.next();
                        }
                        else
                            str_builder.append(">\n");
                        break;
                    }
                case XmlPullParser.END_TAG:
                    {
                        indent.setLength(indent.length()-indentStep.length());

                        String prefix=parser.getPrefix();
                        prefix=prefix!=null?prefix.length()!=0?prefix+":":"":"";

                        str_builder.append(String.format("%s</%s%s>\n",
                                                         indent,
                                                         prefix,
                                                         parser.getName()));
                        type=parser.next();               
                        break;
                    }
                case XmlPullParser.TEXT:
                    {
                        str_builder.append(String.format("%s%s",indent,parser.getText()));
                        type=parser.next();
                        break;
                    }
            }
        }
    }

    private static String axml_getAttributeValue(AXmlResourceParser parser,int index){
        int type=parser.getAttributeValueType(index);
        int data=parser.getAttributeValueData(index);     

        if(type==TypedValue.TYPE_STRING){
            return parser.getAttributeValue(index);
        }
        if(type==TypedValue.TYPE_ATTRIBUTE){
            return String.format("%s",parser.getAttributeValue(index));
        }
        if(type==TypedValue.TYPE_REFERENCE){
            return String.format("%s",parser.getAttributeValue(index));
        }
        if(type==TypedValue.TYPE_FLOAT){
            return String.valueOf(Float.intBitsToFloat(data));
        }
        if(type==TypedValue.TYPE_INT_HEX){
            return String.format("0x%08X",data);
        }
        if(type==TypedValue.TYPE_INT_BOOLEAN){
            return data!=0?"true":"false";
        }
        if(type==TypedValue.TYPE_DIMENSION){
            return Float.toString(complexToFloat(data))+
                DIMENSION_UNITS[data&TypedValue.COMPLEX_UNIT_MASK];
        }
        if(type==TypedValue.TYPE_FRACTION){
            return Float.toString(complexToFloat(data))+
                FRACTION_UNITS[data&TypedValue.COMPLEX_UNIT_MASK];
        }
        if(type>=TypedValue.TYPE_FIRST_COLOR_INT&&type<=TypedValue.TYPE_LAST_COLOR_INT){
            return String.format("#%08X",data);
        }
        if(type>=TypedValue.TYPE_FIRST_INT&&type<=TypedValue.TYPE_LAST_INT){
            return String.valueOf(data);
        }
        return String.format("<0x%X, type 0x%02X>",data,type);      
    }

    private static float complexToFloat(int complex){
        return (float)(complex&0xFFFFFF00)*RADIX_MULTS[(complex>>4)&3];
    }

    private static final float RADIX_MULTS[]={
        0.00390625F,3.051758E-005F,1.192093E-007F,4.656613E-010F
    };
    private static final String DIMENSION_UNITS[]={
        "px","dip","sp","pt","in","mm","",""
    };
    private static final String FRACTION_UNITS[]={
        "%","%p","","","","","",""
    };
}
