package jadx.core.extern;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.core.dex.nodes.ClassNode;
import jadx.exception.JadxException;
import jadx.core.dex.nodes.MethodNode;
import java.util.List;
import java.util.ArrayList;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.nodes.parser.FieldInitAttr;
import jadx.core.dex.info.AccessInfo;
import java.util.Map;
import java.util.HashMap;
import jadx.core.dex.nodes.InsnNode;


public class R_Fix extends AbstractVisitor
{
    final static int R_ATTR=0x7f01;
    final static int R_DRAWABLE=0x7f02;
    final static int R_LAYOUT=0x7f03;
    final static int R_XML=0x7f04;
    final static int R_STYLE=0x7f05;
    final static int R_ARRAY=0x7f06;
    final static int R_ID=0x7f07;
    final static int R_STRING=0x7f08;
    
    @Override
    public boolean visit(ClassNode cls) throws JadxException
    {   
        final Map<Integer,String> R_map=r_map(cls.dex().getClasses());
        fixClass(cls,R_map);
        return false;
    }

    private final Map<Integer,String> r_map(List<ClassNode> classes){
        
        final Map<Integer,String> __MAP=new HashMap<Integer,String>();
        
        final ArrayList<ClassNode> R_list=new ArrayList<>();
        for(ClassNode cn:classes)
            if(cn.getShortName().equals("R"))
                R_list.add(cn);
        //return r_list;      
        for(ClassNode R:R_list){
            List<ClassNode> inner=R.getInnerClasses();
            for(ClassNode i:inner){
                List<FieldNode> fields=i.getFields();
                for(FieldNode f:fields){
                    AccessInfo accFlags = f.getAccessFlags();
                    if (accFlags.isStatic() && accFlags.isFinal()) {
                        FieldInitAttr fv = f.get(AType.FIELD_INIT);
                        if (fv != null
                        && fv.getValue() != null
                        && fv.getValueType() == FieldInitAttr.InitType.CONST
                        && fv != FieldInitAttr.NULL_VALUE) {
                            int V=fv.getValue();
                            switch(V>>>16){
                                case R_ATTR:
                                    __MAP.put(new Integer(V),"R.attr."+f.getName());
                                    break;
                                case R_DRAWABLE:
                                    __MAP.put(new Integer(V),"R.drawable."+f.getName());
                                    break;
                                case R_LAYOUT:
                                    __MAP.put(new Integer(V),"R.layout."+f.getName());
                                    break;
                                case R_XML:
                                    __MAP.put(new Integer(V),"R.xml."+f.getName());
                                    break;
                                case R_STYLE:
                                    __MAP.put(new Integer(V),"R.style."+f.getName());
                                    break;
                                case R_ARRAY:
                                    __MAP.put(new Integer(V),"R.array."+f.getName());
                                    break;
                                case R_ID:
                                    __MAP.put(new Integer(V),"R.id."+f.getName());
                                    break;
                                case R_STRING:
                                    __MAP.put(new Integer(V),"R.string."+f.getName());
                                    break;
                                default: break;
                            }
                        }
                    }
                }
            }
        }    
        return __MAP;
    }
    
    
    
    private void fixClass(ClassNode cls,Map<Integer,String> R_map){
        fix_R(cls.getMethods(),R_map);
        for(ClassNode inner:cls.getInnerClasses())
            fixClass(inner,R_map);      
    }

    private void fix_R(List<MethodNode> methods,Map<Integer,String> R_map){
        for(MethodNode m_node:methods){
            InsnNode[] nodes=m_node.getInstructions();
        }
    }
}
