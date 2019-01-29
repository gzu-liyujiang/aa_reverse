package jadx.api;
import java.util.Map;
import java.util.HashMap;

interface IPreferences{
    
    int OPT_ESCAPE_UNICODE=0x100;

    /**
     * Replace constant values with static final fields with same value
     */

    int OPT_REPLACE_CONSTS=0x200;//常量替换

    //local var style , false use _ style

    int OPT_HUMP_STYLE=0x300;

    //byte,char,short,int,long 

    int OPT_INT_FORMAT=0x400;
    
    int OPT_INDENT_STRING=0x500;
    
    int OPT_SHOW_INCONSISTENT_CODE=0x600;
    
    int OPT_LOCAL_VAR_STYLE=0x700;
    
    int OPT_JAVA_LIBS_DIR=0x800;
    
    int LOCAL_VAR_STYLE_SNAKE=0X701;//hello_world
    int LOCAL_VAR_STYLE_HUMP=0X702;//helloWorld
    int LOCAL_VAR_STYLE_HUMP_LARGE=0X703;//helloWorld
    int LOCAL_VAR_STYLE_ABBREVIATE=0x704;//hw
    int LOCAL_VAR_STYLE_ABBREVIATE_LARGE=0x705;//HW
    int LOCAL_VAR_STYLE_FIRST_CUT=0x706;//hello
    int LOCAL_VAR_STYLE_LAST_CUT=0x707;//world
    int LOCAL_VAR_STYLE_HUNGARY=0x708;//hWorld
    int LOCAL_VAR_STYLE_HUNGARY_SNAKE=0x709;//h_world
    
    static class DEFAULT{
        static public Map<Integer,Object> generateDefaultPreferences(){
            
            Map<Integer,Object> maps=new HashMap<>();
           
            maps.put(OPT_ESCAPE_UNICODE,false);
            maps.put(OPT_REPLACE_CONSTS,false);
            maps.put(OPT_HUMP_STYLE,false);
            maps.put(OPT_INT_FORMAT,"0x%x");
            maps.put(OPT_INDENT_STRING,"    ");
            maps.put(OPT_SHOW_INCONSISTENT_CODE,true);
            maps.put(OPT_LOCAL_VAR_STYLE,LOCAL_VAR_STYLE_SNAKE);
            maps.put(OPT_JAVA_LIBS_DIR,null);
            return maps;
        }
    }
    
}
