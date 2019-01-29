package jadx.core.extern.jar;
import java.util.List;
import java.util.ArrayList;

public class Jar_Class
{
    String super_class_path;
    String[] interfaces_path;
    String class_path;

    List<Jar_Method> methods=new ArrayList<>();

    Jar_Class(String class_path,String super_class_path,String[] interfaces_path){
        this.class_path=class_path;
        this.super_class_path=super_class_path;
        this.interfaces_path=interfaces_path;
    }

    String getClassPath(){
        return class_path;
    }

    public String getSuperClassPath(){
        return super_class_path;
    }

    public String[] getInterfacesPath(){
        return interfaces_path;
    }

    public List<Jar_Method> getMethods(){
        return methods;
    }

    void addMethod(Jar_Method m){
        methods.add(m);
    }
}
