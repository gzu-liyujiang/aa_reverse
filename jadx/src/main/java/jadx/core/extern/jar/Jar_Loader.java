package jadx.core.extern.jar;
import java.util.zip.ZipFile;
import java.io.IOException;
import jadx.exception.JadxException;
import java.util.List;
import java.util.zip.ZipEntry;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import android.util.Log;
import org.jetbrains.java.decompiler.struct.StructClass;
import org.jetbrains.java.decompiler.util.VBStyleCollection;
import java.util.ArrayList;
import org.jetbrains.java.decompiler.struct.StructMethod;
import org.jetbrains.java.decompiler.struct.attr.StructLocalVariableTableAttribute;
import java.io.PrintStream;
import java.io.File;

public class Jar_Loader
{
    final List<ZipFile> jar_files=new ArrayList<>();

    public Jar_Loader(String libs_dir) throws JadxException{
        try
        {
            if(libs_dir==null)
                return;
                
            File[] files=new File(libs_dir).listFiles();
            
            if(files==null)
                return;
                
            for(File f:files)
                if(f.getName().endsWith(".jar"))
                    jar_files.add(new ZipFile(f));
        }
        catch (IOException e)
        {
            throw new JadxException(e.getMessage());
        }

    }

    public List<Jar_Class> loadClassAndSuperClasses(String class_path){
        List<Jar_Class> classes=new ArrayList<>();
        Jar_Class base=loadOne(class_path);
        if(base!=null)
            classes.add(base);
        for(;;){
            if(base.class_path.equals("java/lang/Object"))
                break;
            if(base.super_class_path==null||base.super_class_path.equals("java/lang/Object"))
                break;
            base=loadOne(base.super_class_path);
            classes.add(base);
        }
        return classes;
    }

    public Jar_Class loadOne(String classPath){

        try
        {           
            ZipFile jar=null;
            
            for(ZipFile zf:jar_files){
                if(zf.getEntry(classPath+".class")!=null){
                    jar=zf;
                    break;
                }       
            }
            
            if(jar==null)
                return null;
                
            ZipEntry entry=jar.getEntry(classPath+".class");
            
            ByteArrayOutputStream cache=new ByteArrayOutputStream();
            InputStream in=jar.getInputStream(entry);
            byte[] buf=new byte[4096];
            int n;
            while((n=in.read(buf))!=-1)
                cache.write(buf,0,n);
            return loadOne(cache.toByteArray());            }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            return null;
        }

    }

    Jar_Class loadOne(byte[] class_datas){
        try
        {
            StructClass _class=new StructClass(class_datas,true,null);
            VBStyleCollection<StructMethod,String> _methods=_class.getMethods();
            ArrayList<String> _method_list=_methods.getLstKeys();

            Jar_Class sdk_cls=new Jar_Class(
                _class.qualifiedName,
                _class.superClass!=null?_class.superClass.getString():null,
                _class.getInterfaceNames());

            for(String key:_method_list){
                StructMethod _method=_class.getMethod(key);
                Jar_Method sdk_mth=new Jar_Method(
                    _method.getName(),
                    _method.getDescriptor());
                if(_method.getParameters()!=null){//from AIDE
                    sdk_mth.setParameters(_method.getParameters());          
                }
                else{
                    int p_count=Jar_Util.getMethodParametersCount(_method.getDescriptor());
                    String[] parameters=p_count!=0?new String[p_count]:null;
                    StructLocalVariableTableAttribute vars=_method.getLocalVariableAttr();
                    if(vars==null)continue;
                    List<String> names=vars.getNames();
                    for(int i=0, off=0;i<p_count;++i){

                        String p=names.get(i+off);

                        if(p.equals("this")){
                            ++off;
                            --i;
                        }
                        else{
                            parameters[i]=p;
                        }

                    }

                    sdk_mth.setParameters(parameters);
                }

                sdk_cls.addMethod(sdk_mth);
            }
            return sdk_cls;
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
            return null;
        }        
    }

    public void close() throws JadxException{
        try
        {
            for(ZipFile zf:jar_files)
                zf.close();
        }
        catch (IOException e)
        {
            throw new JadxException(e.getMessage());          
        }
    }
}
