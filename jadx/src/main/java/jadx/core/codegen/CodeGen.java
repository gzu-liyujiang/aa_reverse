package jadx.core.codegen;

import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.visitors.AbstractVisitor;
import jadx.exception.CodegenException;

public class CodeGen extends AbstractVisitor {
    
	public CodeGen() {
        
	}

	@Override
	public boolean visit(ClassNode cls) throws CodegenException {
		ClassGen clsGen = new ClassGen(cls);
		CodeWriter clsCode = clsGen.makeClass();
		clsCode.finish();
		cls.setCode(clsCode);
		return false;
	}

}
