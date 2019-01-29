package jadx.core.dex.nodes;

import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.ConstStorage;
import jadx.core.utils.ErrorsCounter;
import jadx.core.utils.StringUtils;
import jadx.core.utils.data.InputData;
import jadx.exception.DecodeException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RootNode {
    
    private final ErrorsCounter errorsCounter = new ErrorsCounter();
    private final StringUtils stringUtils;
    private final ConstStorage constValues;

    private DexNode dexNode;
    
    public RootNode() {
        this.stringUtils = new StringUtils();
        this.constValues = new ConstStorage();
    }

    public void load(InputData inputData) throws DecodeException {
        dexNode = new DexNode(this, inputData);
        dexNode.loadClasses();
        initInnerClasses();
    }
    private void initInnerClasses() {
       // for (DexNode dexNode : dexNodes) {
            dexNode.initInnerClasses();
        //}
    }

    public List<ClassNode> getClasses(boolean includeInner) {
        List<ClassNode> classes = new ArrayList<ClassNode>();
        DexNode dex= dexNode;
        {
            if (includeInner) {
                classes.addAll(dex.getClasses());
            } else {
                for (ClassNode cls : dex.getClasses()) {
                    if (!cls.getClassInfo().isInner()) {
                        classes.add(cls);
                    }
                }
            }
        }
        return classes;
    }

    public ClassNode searchClassByName(String fullName) {
       /* for (DexNode dexNode : dexNodes)*/
       
       {
            ClassInfo clsInfo = ClassInfo.fromName(dexNode, fullName);
            ClassNode cls = dexNode.resolveClass(clsInfo);
            if (cls != null) {
                return cls;
            }
        }
        return null;
    }

    public List<ClassNode> searchClassByShortName(String shortName) {
        List<ClassNode> list = new ArrayList<ClassNode>();
        //for (DexNode dexNode : dexNodes)
        {
            for (ClassNode cls : dexNode.getClasses()) {
                if (cls.getClassInfo().getShortName().equals(shortName)) {
                    list.add(cls);
                }
            }
        }
        return list;
    }

    /*
    public List<DexNode> getDexNodes() {
        return dexNodes;
    }*/

    public DexNode getDexNode() {
        return dexNode;
    }
    
    public ErrorsCounter getErrorsCounter() {
        return errorsCounter;
    }

    public StringUtils getStringUtils() {
        return stringUtils;
    }

    public ConstStorage getConstValues() {
        return constValues;
    }
}
