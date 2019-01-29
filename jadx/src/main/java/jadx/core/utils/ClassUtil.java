package jadx.core.utils;
import jadx.core.dex.attributes.AType;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.nodes.ClassNode;

public class ClassUtil
{
    ArgType getSuperClassType(ClassNode node){
        return node.getSuperClass();
    }
}
