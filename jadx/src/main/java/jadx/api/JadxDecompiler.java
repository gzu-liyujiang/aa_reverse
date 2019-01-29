package jadx.api;

import jadx.core.dex.visitors.*;

import jadx.annotation.NotNull;
import jadx.core.ProcessClass;
import jadx.core.codegen.CodeGen;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.RootNode;
import jadx.core.dex.visitors.blocksmaker.BlockExceptionHandler;
import jadx.core.dex.visitors.blocksmaker.BlockFinallyExtract;
import jadx.core.dex.visitors.blocksmaker.BlockFinish;
import jadx.core.dex.visitors.blocksmaker.BlockProcessor;
import jadx.core.dex.visitors.blocksmaker.BlockSplitter;
import jadx.core.dex.visitors.regions.CheckRegions;
import jadx.core.dex.visitors.regions.IfRegionVisitor;
import jadx.core.dex.visitors.regions.LoopRegionVisitor;
import jadx.core.dex.visitors.regions.ProcessVariables;
import jadx.core.dex.visitors.regions.RegionMakerVisitor;
import jadx.core.dex.visitors.regions.ReturnVisitor;
import jadx.core.dex.visitors.ssa.EliminatePhiNodes;
import jadx.core.dex.visitors.ssa.SSATransform;
import jadx.core.dex.visitors.typeinference.FinishTypeInference;
import jadx.core.dex.visitors.typeinference.TypeInference;
import jadx.core.extern.MethodParameterFix;
import jadx.core.utils.data.InputData;
import jadx.exception.DecodeException;
import jadx.exception.JadxException;
import jadx.exception.JadxRuntimeException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jadx.core.extern.R_Fix;

public final class JadxDecompiler{

    private InputData inputData;

    private RootNode root;
    private List<IDexTreeVisitor> passes;
    private CodeGen codeGen;

    private List<JavaClass> classes;
 
    private ILogger logger;
    
    private Map<Integer,Object> options;
    
    public JadxDecompiler(){
        this(IPreferences.DEFAULT.generateDefaultPreferences(),null);
    }
    
    public JadxDecompiler(@NotNull Map<Integer,Object> opts){
        this(opts,null);
    }
    
    public JadxDecompiler(@NotNull Map<Integer,Object> opts,ILogger log){
        this.options=opts;
        this.logger=log;
        JadxPreferences.setOptions(opts);
        init();
    }

    void init(){
        this.passes=generatePassesList();
        this.codeGen=new CodeGen();
    }

    public boolean loadData(byte[] dex_data){
        try{
            inputData=new InputData(dex_data);
            return true;
        }catch(DecodeException e){
            if(logger!=null)
                logger.error("DecodeException ---- ",e);
            return false;
        }catch(IOException e){
            if(logger!=null)
                logger.error("IOException ---- ",e);
            return false;
        }        
    }

    /*
    public void decompileAll(@NotNull IResultSaver resultSaver){
        if(root==null){
            try {
                parse();
            }
            catch (DecodeException e) {
                logger.error("dex parse failed,not decompile!",e);
            }
        }
        
        if(logger!=null)
            logger.info("processing ...");
        
        for(final JavaClass cls : getClasses()){
            if(cls.getClassNode().contains(AFlag.DONT_GENERATE)){
                continue;
            }
            cls.decompile();
            SaveCode.save(resultSaver,cls.getClassNode());              
        }
    }*/
    
    public void decompile(JavaClass cls,@NotNull IResultSaver resultSaver){
        cls.decompile();
        SaveCode.save(resultSaver,cls.getClassNode());    
    }

    public List<JavaClass> getClasses(){
        if(root==null){
            return Collections.emptyList();
        }
        if(classes==null){
            List<ClassNode> classNodeList = root.getClasses(false);
            List<JavaClass> clsList = new ArrayList<JavaClass>(classNodeList.size());
            for(ClassNode classNode : classNodeList){
                JavaClass javaClass = new JavaClass(classNode,this);
                clsList.add(javaClass);
            }
            classes=Collections.unmodifiableList(clsList);
        }
        return classes;
    }

    public List<JavaPackage> getPackages(){
        List<JavaClass> classList = getClasses();
        if(classList.isEmpty()){
            return Collections.emptyList();
        }
        Map<String, List<JavaClass>> map = new HashMap<String, List<JavaClass>>();
        for(JavaClass javaClass : classList){
            String pkg = javaClass.getPackage();
            List<JavaClass> clsList = map.get(pkg);
            if(clsList==null){
                clsList=new ArrayList<JavaClass>();
                map.put(pkg,clsList);
            }
            clsList.add(javaClass);
        }
        List<JavaPackage> packages = new ArrayList<JavaPackage>(map.size());
        for(Map.Entry<String, List<JavaClass>> entry : map.entrySet()){
            packages.add(new JavaPackage(entry.getKey(),entry.getValue()));
        }
        Collections.sort(packages);
        for(JavaPackage pkg : packages){
            Collections.sort(pkg.getClasses(),new Comparator<JavaClass>() {
                @Override
                public int compare(JavaClass o1,JavaClass o2){
                    return o1.getName().compareTo(o2.getName());
                }
            });
        }
        return Collections.unmodifiableList(packages);
    }
    
    public static List<IDexTreeVisitor> generatePassesList() {
        List<IDexTreeVisitor> passes = new ArrayList<IDexTreeVisitor>();
        {      
            passes.add(new BlockSplitter());
            passes.add(new BlockProcessor());
            passes.add(new BlockExceptionHandler());
            passes.add(new BlockFinallyExtract());
            passes.add(new BlockFinish());
   
            passes.add(new SSATransform());
            //passes.add(new DebugInfoVisitor());
            passes.add(new TypeInference());
            
            passes.add(new ConstInlineVisitor());
            passes.add(new FinishTypeInference());
            passes.add(new EliminatePhiNodes());

            passes.add(new ModVisitor());
            
            passes.add(new LocalFinalVarFix());
            
            String jars_dir=JadxPreferences.Option_GetStr(JadxPreferences.OPT_JAVA_LIBS_DIR);
            
            passes.add(new MethodParameterFix(jars_dir));
            
            //passes.add(new R_Fix());
            
            passes.add(new CodeShrinker());
            passes.add(new ReSugarCode());

            passes.add(new RegionMakerVisitor());
            passes.add(new IfRegionVisitor());
            passes.add(new ReturnVisitor());
               
            passes.add(new CodeShrinker());
            passes.add(new SimplifyVisitor());
            passes.add(new CheckRegions());
            
            passes.add(new ExtractFieldInit());
           // passes.add(new ClassModifier());
            
            passes.add(new EnumVisitor());
            passes.add(new PrepareForCodeGen());
            passes.add(new LoopRegionVisitor());
            
            passes.add(new ProcessVariables(jars_dir));

            passes.add(new DependencyCollector());
            passes.add(new MethodInlineVisitor());
            
            passes.add(new NoStaticInnerClassFix());
            
           // passes.add(new TypeConvertVisitor());
            
            
            //FIXME
            //passes.add(new RenameVisitor());
        }
        return passes;
    }

    public void parse() throws DecodeException{
       
        root=new RootNode();
        
        if(logger!=null)
            logger.info("loading ...");
        root.load(inputData);

        initVisitors();
        initOptions();
    }

    private void initVisitors(){
        for(IDexTreeVisitor pass : passes){
            try{
                pass.init(root);
            }catch(Exception e){
                if(logger!=null)
                  logger.error("Visitor init failed: {}",e);
            }
        }
    }
    
    private void initOptions() {
        
    }
    
    void processClass(ClassNode cls){
        ProcessClass.process(cls,passes,codeGen);
    }
   
    @Override
    public String toString(){
        return "jadx decompiler";
    }

}
