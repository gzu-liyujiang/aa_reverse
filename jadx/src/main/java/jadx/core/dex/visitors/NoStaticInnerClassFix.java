package jadx.core.dex.visitors;

import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.nodes.FieldReplaceAttr;
import jadx.core.dex.info.AccessInfo;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.InstructionRemover;
import jadx.exception.JadxException;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import android.util.*;
import java.util.Set;
import jadx.core.dex.instructions.InvokeType;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.args.FieldArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.Named;
import jadx.core.dex.instructions.args.NamedArg;
import java.util.ArrayList;
import jadx.core.utils.InsnList;

public class NoStaticInnerClassFix extends AbstractVisitor {

    @Override
    public boolean visit(ClassNode cls) throws JadxException {
        for (ClassNode inner : cls.getInnerClasses()) {
            visit(inner);
        }
        if (cls.getAccessFlags().isSynthetic()
            && cls.getFields().isEmpty()
            && cls.getMethods().isEmpty()) {
            
            cls.add(AFlag.DONT_GENERATE);
            return false;
        }
        
        if (!cls.getClassInfo().isInner()) {
            return false;
		}
        
        if (!cls.isAnonymousClass()&&cls.getAccessFlags().isStatic()) {
            return false;
		}
        
        FixInvoke(cls);
        
		return false;
    }
    
	private void FixInvoke(ClassNode inner_class){
        
        List<MethodNode> constructors=getConstructors(inner_class);
        boolean is_anonymous_class=inner_class.isAnonymousClass();
        for(FieldNode fn:inner_class.getFields()){
            if(fn.isSynthetic()
               ||fn.getName().startsWith("this$")
               ||fn.getName().startsWith("val$"))
            {
                for(MethodNode constructor:constructors){
                    List<BlockNode> blocks=constructor.getBasicBlocks();
                    for(BlockNode block:blocks){
                        //BlockNode block=constructor.getBasicBlocks().get(i);
                        List<InsnNode> insns=block.getInstructions();
                        for(InsnNode insn:insns){
                            //InsnNode insn=insns.get(j);
                            if(insn.getType()!=InsnType.IPUT)
                                continue;
                            if(fn.getFieldInfo().equals(((IndexInsnNode) insn).getIndex())){
                                RegisterArg r_arg= (RegisterArg)insn.getArg(0);
                                if(fn.getName().startsWith("this$")){
                                   // Log.i("A_",inner_class.getFullName());
                                    fn.addAttr(new FieldReplaceAttr(ClassInfo.fromType(inner_class.dex(),r_arg.getType())));
                                    fn.add(AFlag.DONT_GENERATE);
                                    InsnList.remove(block, insn);
                                }
                                else if(fn.getName().startsWith("val$")&&is_anonymous_class)
                                    fn.addAttr(new FieldReplaceAttr(new NamedArg(fn.getName().substring(4),r_arg.getType())));
                                break;
                            }
                        }
                    }
                }
            }   
		}
    }
    
    private List<MethodNode> getConstructors(ClassNode _class){
        List<MethodNode> constructors=new ArrayList<>();
        for(MethodNode mn:_class.getMethods()){
            if(mn.isConstructor())
                constructors.add(mn);
        }
        return constructors;
    }
    
	private Map<FieldNode,Object /* ClassInfo|InsnArg */ > parseSyntheticFields(ClassNode anonymous_class){
		Map<FieldNode,Object /* ClassInfo|InsnArg */ > map=new HashMap<>();
		MethodNode constructor=anonymous_class.getAnonymousClassConstructor();
		for(FieldNode fn:anonymous_class.getFields()){
            if(fn.isSynthetic()
                ||fn.getName().startsWith("this$")
                ||fn.getName().startsWith("val$"))
            {
                List<BlockNode> blocks=constructor.getBasicBlocks();
                for(int i=0;i<blocks.size();i++){
                    List<InsnNode> insns=constructor.getBasicBlocks().get(i).getInstructions();
                    for(int j=0;j<insns.size();j++){
                        InsnNode insn=insns.get(j);
                        if(insn.getType()!=InsnType.IPUT)
                            continue;
                        if(fn.getFieldInfo().equals(((IndexInsnNode) insn).getIndex())){
                            RegisterArg r_arg= (RegisterArg)insn.getArg(0);
                            if(fn.getName().startsWith("this$"))
                                fn.addAttr(new FieldReplaceAttr(ClassInfo.fromType(anonymous_class.dex(),r_arg.getType())));
                            else if(fn.getName().startsWith("val$"))
                                fn.addAttr(new FieldReplaceAttr(new NamedArg(fn.getName().substring(4),r_arg.getType())));
                            break;
                        }
                    }
                }
            }	
		}
		return map;
	}
}
