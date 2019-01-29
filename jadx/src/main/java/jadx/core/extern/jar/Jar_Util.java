package jadx.core.extern.jar;
import jadx.core.dex.instructions.args.ArgType;
import java.util.List;
import jadx.core.codegen.TypeGen;

public class Jar_Util
{
    public static String getClassPath(ArgType t){
        return t.getObject().replace('.','/');
    }
    
    public static int getMethodParametersCount(String descriptor){
        int n=0;
        int i=0;
        for(;;){
            char ch=descriptor.charAt(i);
            if(ch=='('){
                ++i;
                continue;
            }
            if(ch==')')
                break;

            switch(ch){
                case 'Z':
                case 'C':
                case 'B':
                case 'S':
                case 'I':
                case 'F':
                case 'J':
                case 'D':
                    ++i;
                    ++n;
                    break;
                case 'L':
                    while(descriptor.charAt(i++)!=';')
                        ;
                    ++n;
                    break;
                case '[':
                    if(descriptor.charAt(++i)=='L'){
                        while(descriptor.charAt(i++)!=';')
                            ;
                        ++n;
                    }else{
                        ++i;
                        ++n;
                    }
                    break;
            }
        }

        return n;
    }

    public static String getMethodDescriptor(List<ArgType> args,ArgType retType){
        StringBuilder signature = new StringBuilder();
        signature.append('(');
        for (ArgType arg : args) {
            signature.append(TypeGen.signature(arg));
        }
        signature.append(')');
        signature.append(TypeGen.signature(retType));
        return signature.toString();
    }
}
