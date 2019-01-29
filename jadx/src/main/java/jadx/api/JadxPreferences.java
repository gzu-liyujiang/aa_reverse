package jadx.api;
import java.util.Map;

public class JadxPreferences implements IPreferences{

    private static Map<Integer,Object> options_map;
    
    /*package*/ static void setOptions(Map<Integer,Object> opts){
        options_map=opts;
    }
    
    public static boolean 
    Option_GetZ(int opt){
        return (boolean)options_map.get(opt);
    }

    public static int 
    Option_GetI(int opt){
        return (int)options_map.get(opt);
    }

    public static String 
    Option_GetStr(int opt){
        return (String)options_map.get(opt);
    }
}
