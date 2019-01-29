package org.jetbrains.java.decompiler.struct.attr;
import java.util.List;
import org.jetbrains.java.decompiler.util.DataInputFullStream;
import org.jetbrains.java.decompiler.struct.consts.ConstantPool;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;

//  by      : aenu
//  date    : 2017 11 28

public class StructParameterTableAttribute extends StructGeneralAttribute { 
    
    //big endian 
    
    //size arguments_count*10+2
    
    //uint16_t arguments_count
   
    //uint16_t ?
    //uint16_t ?
    //uint16_t parameter_index
    //uint16_t ?
    //uint16_t ?
    
    private String[] entries=null;
    
    @Override
    public void initContent(DataInputFullStream data, ConstantPool pool) throws IOException {
        int count = data.readUnsignedShort();
        if(count==0) return;
        
        entries=new String[count];
        
        for(int i=0;i<count;i++){
            data.readUnsignedShort();
            data.readUnsignedShort();
            int parameter_index=data.readUnsignedShort();        
            entries[i]=pool.getPrimitiveConstant(parameter_index).getString();  
            data.readUnsignedShort();
            data.readUnsignedShort();
        }
     }
    
    public String[] getEntries(){
        return entries;
    }
    
} 
