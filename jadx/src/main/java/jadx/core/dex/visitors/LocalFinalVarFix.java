package jadx.core.dex.visitors;

import jadx.core.dex.instructions.args.*;
import jadx.core.dex.nodes.*;
import jadx.core.utils.*;
import java.util.*;

import jadx.core.dex.attributes.AFlag;
import jadx.exception.JadxException;
import android.util.Log;
import jadx.core.dex.instructions.InsnType;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.instructions.IndexInsnNode;
import jadx.core.dex.info.FieldInfo;

public class LocalFinalVarFix extends AbstractVisitor
{
    @Override
    public void visit(MethodNode mth) throws JadxException {
        for (BlockNode block : mth.getBasicBlocks()) {
            fixBlock(mth,block);
            //prInfo(mth,block);
		}
	}
    /*
    private static void prInfo(MethodNode mth, BlockNode block) {
        Log.i("A_",mth.getName()+block.getId());
        InsnList insnList = new InsnList(block.getInstructions());
        List<InsnNode> insns=new ArrayList<>(insnList.size());
        for(int i=0;i<insnList.size();++i){
            InsnNode insn;
            insns.add(insn=insnList.get(i));
            Log.i("A_",insn.getType().toString());
            switch(insn.getType()){
                case CONSTRUCTOR:
                case INVOKE:
                    Iterable<InsnArg> args=insn.getArguments();
                    Iterator<InsnArg> iter=args.iterator();
                    //List<RegisterArg> replaceRegs=new ArrayList<>();
                    while(iter.hasNext()){
                        InsnArg arg=iter.next();
                        Log.i("A_",arg.toString());
                        }
                    break;
            }
        }

    }*/
/*
static void prArg(InsnArg arg){
    if (arg.isRegister()) {
        //if(arg.getn
        Log.i("A_","R "+arg.toString());
        
        //code.add(mgen.getNameGen().useArg((RegisterArg) arg));
    } else if (arg.isLiteral()) {
        Log.i("A_","LITERAL");
        //code.add(lit((LiteralArg) arg));
    } else if (arg.isInsnWrap()) {
        Log.i("A_","WRAP");
        //Flags flag = wrap ? Flags.BODY_ONLY : Flags.BODY_ONLY_NOWRAP;
        //makeInsn(((InsnWrapArg) arg).getWrapInsn(), code, flag);
    } else if (arg.isNamed()) {
        Log.i("A_","Name");
        //code.add(((Named) arg).getName());
    } else if (arg.isField()) {
        Log.i("A_","Field");
       
    } else {
        Log.i("A_","unk");
       // throw new CodegenException("Unknown arg type " + arg);
    }
}*/
    private static void fixBlock(MethodNode mth, BlockNode block) {
        InsnList insnList = new InsnList(block.getInstructions());
        List<InsnNode> insns=new ArrayList<>(insnList.size());
        for(int i=0;i<insnList.size();++i){
            InsnNode insn;
            insns.add(insn=insnList.get(i));
            
            switch(insn.getType()){
                case CONSTRUCTOR:
                    ClassNode cls = mth.dex().resolveClass(((ConstructorInsn)insn).getClassType());
                    if (cls != null &&cls.isAnonymousClass()) {
                        Iterable<InsnArg> args=insn.getArguments();
                        Iterator<InsnArg> iter=args.iterator();
                        Map<Integer,RegisterArg> upadteRegs=new HashMap<>();
                        int r_index=1;
                        while(iter.hasNext()){
                            InsnArg arg=iter.next();
                            if(!arg.isRegister())
                                continue;
                            if(arg.isThis()){
                                ++r_index;
                                continue;
                            }
                            SSAVar svar=((RegisterArg)arg).getSVar();
                            if(svar!=null){
                                svar.add(AFlag.DONT_INLINE);
                                svar.add(AFlag.FINAL);
                                upadteRegs.put(r_index++,(RegisterArg)arg);                
                            }
                        }
                        
                        if(!upadteRegs.isEmpty())
                            fixRegisters(cls,upadteRegs);
                    }
                    break;
            }
        }
    
    }
    private static void fixRegisters(ClassNode anonymous_class,Map<Integer,RegisterArg> parameters){
        
        MethodNode constructor=anonymous_class.getAnonymousClassConstructor();

        List<BlockNode> blocks=constructor.getBasicBlocks();
        for(int i=0;i<blocks.size();i++){
            List<InsnNode> insns=constructor.getBasicBlocks().get(i).getInstructions();

            for(int j=0;j<insns.size();j++){
                InsnNode insn=insns.get(j);
                if(insn.getType()!=InsnType.IPUT)
                    continue;
                FieldInfo f_info=(FieldInfo)((IndexInsnNode) insn).getIndex();
                if(!f_info.getName().startsWith("val$"))
                    continue;
                RegisterArg r_arg= (RegisterArg)insn.getArg(0);
                RegisterArg r=parameters.get(r_arg.getRegNum());
                String name=f_info.getName().substring(4);
                if(r!=null)
                    r.setName(name);
            }
        }
    }
}
