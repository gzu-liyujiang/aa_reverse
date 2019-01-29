package jadx.core.dex.visitors;
import jadx.core.dex.nodes.MethodNode;
import jadx.exception.JadxException;
import jadx.core.dex.nodes.BlockNode;
import jadx.core.utils.InsnList;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.info.FieldInfo;
import jadx.core.dex.instructions.IndexInsnNode;

public class TypeConvertVisitor extends AbstractVisitor
{

    @Override
    public void visit(MethodNode mth) throws JadxException {
        for (BlockNode block : mth.getBasicBlocks()) {
            InsnList insnList = new InsnList(block.getInstructions());
            final int insnCount = insnList.size();
            
            for(int i=0;i<insnCount;i++){
                
                InsnNode insn=insnList.get(i);
                boolean convert_result_type=false;
                boolean convert_instance_type=false;
                
                switch(insn.getType()){
                    case MOVE:
                        if(!insn.getArg(0).getType().equals(insn.getResult().getType()))
                            convert_result_type=true;
                        break;
                    case IGET:
                    {
                        FieldInfo fieldInfo = (FieldInfo) ((IndexInsnNode) insn).getIndex();
                        
                        if(!insn.getArg(0).getType().equals(fieldInfo.getDeclClass().getType())){
                            convert_instance_type=true;              
                        }
                    }
                        break;
                    case IPUT:
                        {
                        FieldInfo fieldInfo = (FieldInfo) ((IndexInsnNode) insn).getIndex();
                        if(!insn.getArg(1).getType().equals(fieldInfo.getDeclClass().getType())){
                            convert_instance_type=true;              
                        }
                        if(!insn.getArg(0).getType().equals(fieldInfo.getType())){
                            convert_result_type=true;              
                        }
                        }
                        break;
                }
                
                if(convert_instance_type)
                    insn.add(AFlag.CONVERT_INSTANCE_TYPE);
                if(convert_result_type)
                    insn.add(AFlag.CONVERT_RESULT_TYPE);
            }
		}
    }
}
