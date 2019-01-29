package jadx.core.extern.jar;

public class Jar_Method
{
    String name;
    String descriptor;
    String[] parameters;
    Jar_Method(String name,String descriptor){
        this.name=name;
        this.descriptor=descriptor;
    }

    public String getName(){
        return name;
    }
    public String getDescriptor(){
        return descriptor;
    }
    public String[] getParameters(){
        return parameters;
    }
    void setParameters(String[] p){
        parameters=p;
    }
}
