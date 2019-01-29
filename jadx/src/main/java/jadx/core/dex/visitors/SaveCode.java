package jadx.core.dex.visitors;

import jadx.core.codegen.CodeWriter;
import jadx.core.dex.nodes.ClassNode;
//import jadx.exception.CodegenException;

import java.io.File;
import jadx.api.IResultSaver;

public class SaveCode extends AbstractVisitor {
	
	public static void save(IResultSaver saver, ClassNode cls) {
		CodeWriter clsCode = cls.getCode();
		String fileName = cls.getClassInfo().getFullPath();
		clsCode.save(saver, fileName);
	}
}
