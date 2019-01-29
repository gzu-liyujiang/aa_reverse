package jadx.core.codegen;

import jadx.core.Consts;
import jadx.core.deobf.NameMapper;
import jadx.core.dex.attributes.nodes.LoopLabelAttr;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.NamedArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.StringUtils;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import jadx.api.JadxPreferences;

public class NameGen {

	private static final Map<String, String> OBJ_ALIAS;

	private final Set<String> varNames = new HashSet<String>();
	private final MethodNode mth;
	
	static {
		OBJ_ALIAS = new HashMap<String, String>();
		OBJ_ALIAS.put(Consts.CLASS_STRING, "str");
		OBJ_ALIAS.put(Consts.CLASS_CLASS, "cls");
		OBJ_ALIAS.put(Consts.CLASS_THROWABLE, "th");
		OBJ_ALIAS.put(Consts.CLASS_OBJECT, "obj");
		OBJ_ALIAS.put("java.util.Iterator", "it");
		OBJ_ALIAS.put("java.lang.Boolean", "bool");
		OBJ_ALIAS.put("java.lang.Short", "sh");
		OBJ_ALIAS.put("java.lang.Integer", "num");
		OBJ_ALIAS.put("java.lang.Character", "ch");
		OBJ_ALIAS.put("java.lang.Byte", "b");
		OBJ_ALIAS.put("java.lang.Float", "f");
		OBJ_ALIAS.put("java.lang.Long", "l");
		OBJ_ALIAS.put("java.lang.Double", "d");
	}

	public NameGen(MethodNode mth) {
		this.mth = mth;
	}

	public String assignArg(RegisterArg arg) {
		String name = makeArgName(arg);
		
		name = getUniqueVarName(name);
		arg.setName(name);
		return name;
	}

	public String assignNamedArg(NamedArg arg) {
		String name = arg.getName();
		
		name = getUniqueVarName(name);
		arg.setName(name);
		return name;
	}

	public String useArg(RegisterArg arg) {
		String name = arg.getName();
		if (name == null) {
			return getFallbackName(arg);
		}
		return name;
	}

	// TODO: avoid name collision with variables names
	public String getLoopLabel(LoopLabelAttr attr) {
		String name = "loop" + attr.getLoop().getId();
		varNames.add(name);
		return name;
	}

	private String getUniqueVarName(String name) {
		String r = name;
		int i = 2;
        
        String _="";
        int style=JadxPreferences.Option_GetI(JadxPreferences.OPT_LOCAL_VAR_STYLE);
        if(style==JadxPreferences.LOCAL_VAR_STYLE_SNAKE
          ||style==JadxPreferences.LOCAL_VAR_STYLE_HUNGARY_SNAKE)
            _="_";
          
		while (varNames.contains(r)) {
			r = name + _ +i;
			i++;
		}
		varNames.add(r);
		return r;
	}

	private String makeArgName(RegisterArg arg) {
		
		String name = arg.getName();
		String varName;
		if (name != null) {
			if ("this".equals(name)) {
				return name;
			}
			varName = name;
		} else {
			varName = guessName(arg);
		}
        
        varName=Character.toString(varName.charAt(0))+varName.substring(1);
        switch(JadxPreferences.Option_GetI(JadxPreferences.OPT_LOCAL_VAR_STYLE)){
            case JadxPreferences.LOCAL_VAR_STYLE_SNAKE:
            default:
                varName=varName.replaceAll("[A-Z]","_$0");
                if(varName.charAt(0)=='_')
                    varName=varName.substring(1);
                varName=varName.toLowerCase();
                break;
            case JadxPreferences.LOCAL_VAR_STYLE_HUMP:
                varName=Character.toLowerCase(varName.charAt(0)) + varName.substring(1);
                break;
            case JadxPreferences.LOCAL_VAR_STYLE_ABBREVIATE:
            {
                String _varName=varName.replaceAll("[a-z]","");
                if(_varName.length()==0)
                    _varName=Character.toString(varName.charAt(0));
                varName=_varName.toLowerCase();
                
            }
                 break;
            case JadxPreferences.LOCAL_VAR_STYLE_ABBREVIATE_LARGE:
                {
                    String _varName=varName.replaceAll("[a-z]","");
                    if(_varName.length()==0)
                        _varName=Character.toString(varName.charAt(0));
                    varName=_varName.toUpperCase();

                }
                break;
            case JadxPreferences.LOCAL_VAR_STYLE_LAST_CUT:
                varName=varName.replaceAll("[A-Z]","_$0");
                int lp=varName.lastIndexOf('_');
                if(lp!=-1)
                    varName=varName.substring(lp+1).toLowerCase();
                break;
            case JadxPreferences.LOCAL_VAR_STYLE_FIRST_CUT:{
                    varName=varName.replaceAll("[A-Z]","_$0");
                    boolean _=varName.charAt(0)=='_';
                    int fp=varName.indexOf('_',_?1:0);
                    if(fp!=-1)
                        varName=varName.substring(_?1:0,fp);
                    else
                        varName=varName.charAt(0)=='_'?
                          varName.substring(1):varName;
            }
                
                break;
            case JadxPreferences.LOCAL_VAR_STYLE_HUMP_LARGE:
                varName=Character.toUpperCase(varName.charAt(0)) + varName.substring(1);              
                break;            
            case JadxPreferences.LOCAL_VAR_STYLE_HUNGARY:{
                String _varName=varName.replaceAll("[a-z]","");
                if(_varName.length()==1)
                    varName=varName.toLowerCase();
                else if(_varName.length()>1){
                    char c=_varName.charAt(_varName.length()-1);
                    int p=varName.lastIndexOf(c);
                    
                    _varName=_varName.toLowerCase().substring(0,_varName.length()-1);
                    varName=_varName+varName.substring(p);
                }
            }
               break;   
            case JadxPreferences.LOCAL_VAR_STYLE_HUNGARY_SNAKE:
                {
                    String _varName=varName.replaceAll("[a-z]","");
                    if(_varName.length()==1)
                        varName=varName.toLowerCase();
                    else if(_varName.length()>1){
                        char c=_varName.charAt(_varName.length()-1);
                        int p=varName.lastIndexOf(c);

                        _varName=_varName.toLowerCase().substring(0,_varName.length()-1);
                        varName=_varName+'_'+varName.substring(p).toLowerCase();
                    }
                }              
                break;   
        }
        
		if (NameMapper.isReserved(varName)) {
			return varName + "R";
		}
		return varName;
	}

	private String getFallbackName(RegisterArg arg) {
		return "r" + arg.getRegNum();
	}

	private String guessName(RegisterArg arg) {
		SSAVar sVar = arg.getSVar();
		if (sVar != null && sVar.getName() == null) {
			RegisterArg assignArg = sVar.getAssign();
			InsnNode assignInsn = assignArg.getParentInsn();
			if (assignInsn != null) {
				String name = makeNameFromInsn(assignInsn);
				if (name != null && !NameMapper.isReserved(name)) {
					assignArg.setName(name);
					return name;
				}
			}
		}
		return makeNameForType(arg.getType());
	}

	private String makeNameForType(ArgType type) {
		if (type.isPrimitive()) {
			return makeNameForPrimitive(type);
		} else if (type.isArray()) {
			return makeNameForType(type.getArrayRootElement()) + "Arr";
		} else {
			return makeNameForObject(type);
		}
	}

	private static String makeNameForPrimitive(ArgType type) {
		return type.getPrimitiveType().getShortName().toLowerCase();
	}

	private String makeNameForObject(ArgType type) {
		if (type.isObject()) {
			String alias = getAliasForObject(type.getObject());
			if (alias != null) {
				return alias;
			}
			ClassInfo extClsInfo = ClassInfo.extCls(mth.dex(), type);
			String shortName = extClsInfo.getShortName();
            if(Character.isDigit(shortName.charAt(0)))
            {
                shortName="__";
            }
			String vName = fromName(shortName);
			if (vName != null) {
				return vName;
			}
		}
		return StringUtils.escape(type.toString());
	}

	private static String fromName(String name) {
		if (name == null || name.isEmpty()) {
			return null;
		}
		if (name.toUpperCase().equals(name)) {
			// all characters are upper case
			return name.toLowerCase();
		}
        return name;
		//String v1 = Character.toLowerCase(name.charAt(0)) + name.substring(1);
		//if (!v1.equals(name)) {
		//	return v1;
		//}
		//if (name.length() < 3) {
		//	return name + "Var";
		//}
		//return null;
	}

	private static String getAliasForObject(String name) {
		return OBJ_ALIAS.get(name);
	}

	private String makeNameFromInsn(InsnNode insn) {
		switch (insn.getType()) {
			case INVOKE:
				InvokeNode inv = (InvokeNode) insn;
				return makeNameFromInvoke(inv.getCallMth());

			case CONSTRUCTOR:
				ConstructorInsn co = (ConstructorInsn) insn;
				return makeNameForObject(co.getClassType().getType());

			case ARRAY_LENGTH:
				return "length";

			case ARITH:
			case TERNARY:
			case CAST:
				for (InsnArg arg : insn.getArguments()) {
					if (arg.isInsnWrap()) {
						InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
						String wName = makeNameFromInsn(wrapInsn);
						if (wName != null) {
							return wName;
						}
					}
				}
				break;

			default:
				break;
		}
		return null;
	}

	private String makeNameFromInvoke(MethodInfo callMth) {
		String name = callMth.getName();
		if (name.startsWith("get") || name.startsWith("set")) {
			return fromName(name.substring(3));
		}
		ArgType declType = callMth.getDeclClass().getAlias().getType();
		if ("iterator".equals(name)) {
			return "it";
		}
		if ("toString".equals(name)) {
			return makeNameForType(declType);
		}
		if ("forName".equals(name) && declType.equals(ArgType.CLASS)) {
			return OBJ_ALIAS.get(Consts.CLASS_CLASS);
		}
		return name;
	}
}
