package jadx.core.dex.visitors;

import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.dex.nodes.RootNode;
import jadx.exception.JadxException;
import jadx.core.dex.nodes.FieldNode;

public class AbstractVisitor implements IDexTreeVisitor {

	@Override
	public void init(RootNode root) throws JadxException {
	}

	@Override
	public boolean visit(ClassNode cls) throws JadxException {
		return true;
	}
    
    @Override
    public void visit(FieldNode fld) throws JadxException{
    }
    
	@Override
	public void visit(MethodNode mth) throws JadxException {
	}

}
