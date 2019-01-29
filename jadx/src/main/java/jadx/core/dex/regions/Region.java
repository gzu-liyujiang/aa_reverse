package jadx.core.dex.regions;

import jadx.core.dex.nodes.IContainer;
import jadx.core.dex.nodes.IRegion;

import java.util.ArrayList;
import java.util.List;

public final class Region extends AbstractRegion {

	private final List<IContainer> blocks;

	public Region(IRegion parent) {
		super(parent);
		this.blocks = new ArrayList<IContainer>(1);
	}

	@Override
	public List<IContainer> getSubBlocks() {
		return blocks;
	}

	public void add(IContainer region) {
		if (region instanceof AbstractRegion) {
			((AbstractRegion) region).setParent(this);
		}
		blocks.add(region);
	}

    /*
    public IContainer findRegion(String name){
        for(IContainer region: blocks){
            if(region.baseString().equals(name))
                return region;
        }
        return null;
    }
    
    public void updateRegion(String old_region,IContainer new_region){
        for(int i=0;i<blocks.size();++i){
            if(blocks.get(i).baseString().equals(old_region)){
                blocks.set(i,new_region);
            }           
        }
    }*/
    
	@Override
	public boolean replaceSubBlock(IContainer oldBlock, IContainer newBlock) {
		int i = blocks.indexOf(oldBlock);
		if (i != -1) {
			blocks.set(i, newBlock);
			return true;
		}
		return false;
	}

	@Override
	public String baseString() {
		StringBuilder sb = new StringBuilder();
		sb.append(blocks.size());
		for (IContainer cont : blocks) {
			sb.append(cont.baseString());
		}
		return sb.toString();
	}

	@Override
	public String toString() {
		return "R:" + baseString();
	}
}
