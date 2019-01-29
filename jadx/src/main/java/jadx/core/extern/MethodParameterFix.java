package jadx.core.extern;

import jadx.core.dex.nodes.ClassNode;
import jadx.exception.JadxException;
import jadx.core.dex.nodes.MethodNode;
import android.util.Log;
import java.util.List;
import jadx.core.utils.Utils;
import java.io.File;
import java.util.zip.ZipFile;
import java.io.IOException;
import org.jetbrains.java.decompiler.struct.lazy.LazyLoader;
import org.jetbrains.java.decompiler.main.extern.IBytecodeProvider;
import java.util.zip.ZipEntry;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import org.jetbrains.java.decompiler.struct.StructClass;
import java.util.ArrayList;
import org.jetbrains.java.decompiler.util.VBStyleCollection;
import org.jetbrains.java.decompiler.struct.StructMethod;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.codegen.TypeGen;
import jadx.core.dex.instructions.args.RegisterArg;
import org.jetbrains.java.decompiler.main.Fernflower;
import java.io.PrintStream;
import org.jetbrains.java.decompiler.struct.attr.StructLocalVariableTableAttribute;
import jadx.core.dex.info.MethodInfo;
import org.jetbrains.java.decompiler.main.DecompilerContext;
import org.jetbrains.java.decompiler.main.collectors.CounterContainer;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.nodes.DexNode;
import jadx.core.extern.jar.Jar_Loader;
import jadx.core.extern.jar.Jar_Class;
import jadx.core.extern.jar.Jar_Util;
import jadx.core.extern.jar.Jar_Method;

public class MethodParameterFix extends AbstractVisitor
{
    private String libs_dir;
    
    public MethodParameterFix(String libs_dir){
        this.libs_dir=libs_dir;
    }

    @Override
    public boolean visit(ClassNode cls) throws JadxException
    {
        DecompilerContext. initContext(null);
        DecompilerContext.setCounterContainer(new CounterContainer());
        
        Jar_Loader sdk_loader=new Jar_Loader(libs_dir);
        fixClass(cls, loadJarClasses(getTopSuperClassNode(cls),sdk_loader), sdk_loader);
        sdk_loader.close();
        
        DecompilerContext.setCurrentContext(null);

        return false;
    }

    //继承至sdk中的类
    public ClassNode getTopSuperClassNode(ClassNode class_node){
        DexNode dex_node=class_node.dex();
        ClassNode super_class_node=class_node;
        for(;;){
            ArgType super_class=super_class_node.getSuperClass();
            if(super_class==null)
                return super_class_node;
            if((class_node=dex_node.resolveClass(super_class))==null)
                return super_class_node;
            super_class_node=class_node;
        }
    }
    
    private List<Jar_Class> loadJarClasses(ClassNode class_node,Jar_Loader sdk_loader){
       // Log.w("A-",class_node.getFullName());
        
        List<Jar_Class> sdk_classes=new ArrayList<>();
        List<Jar_Class> sdk_class;
        
        ArgType cls;
        if((cls=class_node.getSuperClass())!=null)
            if((sdk_class=sdk_loader.loadClassAndSuperClasses(Jar_Util.getClassPath(cls)))!=null)
                sdk_classes.addAll(sdk_class);
                
        for(ArgType i:class_node.getInterfaces())
            if((sdk_class=sdk_loader.loadClassAndSuperClasses(Jar_Util.getClassPath(i)))!=null)
                sdk_classes.addAll(sdk_class);
                
        return sdk_classes;
    }
    
    private void fixClass(ClassNode class_node,List<Jar_Class> sdk_classes,Jar_Loader sdk_loader){
        for(Jar_Class sdk_class:sdk_classes)
           // print_sdk(sdk_class);
            fixMethodParameters(sdk_class.getMethods(),class_node.getMethods());
        for(ClassNode inner:class_node.getInnerClasses())
            fixClass(inner,loadJarClasses(getTopSuperClassNode(inner),sdk_loader),sdk_loader);
    }
    /*
    private void print_sdk(SDK_Class sdk_class){
        
       // if(sdk_class.getClassPath().equals("android/preference/DialogPreference"))
        //    return;
        List<SDK_Method> sdk_methods=sdk_class.getMethods();
        Log.w("A_",sdk_class.getClassPath());
        for(SDK_Method sdk_mth:sdk_methods){
            // if(name.equals(sdk_mth)&&scriptor.equals(sdk_mth.getDescriptor())){
            //    List<RegisterArg> args=node.getArguments(false);
            //if(!sdk_mth.getName().equals("onCreate"))continue;
            Log.w("A_","sdk "+sdk_mth.getName()+" -- "+sdk_mth.getDescriptor());
            String[] parameters=sdk_mth.getParameters();
            if(parameters!=null){
                for(int i=0;i<parameters.length;i++){
                    Log.w("A_",parameters[i]);
                    //args.get(i).setName(parameters[i]);
                }
            }
        }
    }*/

    private void fixMethodParameters(List<Jar_Method> sdk_methods,List<MethodNode> method_nodes){
        
        for(MethodNode node:method_nodes){
            
            MethodInfo m_info=node.getMethodInfo();
            String name=m_info.getName();
            String scriptor=Jar_Util.getMethodDescriptor(m_info.getArgumentsTypes(),m_info.getReturnType());

            for(Jar_Method sdk_mth:sdk_methods){
                if(name.equals(sdk_mth.getName())&&scriptor.equals(sdk_mth.getDescriptor())){
                    List<RegisterArg> args=node.getArguments(false);
                    String[] parameters=sdk_mth.getParameters();
                    if(parameters!=null){
                        for(int i=0;i<parameters.length;i++){
                            String p=parameters[i];
                            if(p!=null)
                                args.get(i).setName(p);
                        }
                    }
                }
            }
        }
    }


}
