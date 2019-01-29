package jadx.api;

import jadx.core.codegen.CodeWriter;
import jadx.core.dex.attributes.AFlag;
import jadx.core.dex.attributes.nodes.LineAttrNode;
import jadx.core.dex.info.AccessInfo;
import jadx.core.dex.nodes.ClassNode;
import jadx.core.dex.nodes.FieldNode;
import jadx.core.dex.nodes.MethodNode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jadx.annotation.Nullable;

public final class JavaClass implements JavaNode {

    private final JadxDecompiler decompiler;
    private final ClassNode cls;
    private final JavaClass parent;

    private List<JavaClass> innerClasses = Collections.emptyList();
    private List<JavaField> fields = Collections.emptyList();
    private List<JavaMethod> methods = Collections.emptyList();
   
    private Map<ClassNode, JavaClass> classesMap = new HashMap<ClassNode, JavaClass>();
    private Map<MethodNode, JavaMethod> methodsMap = new HashMap<MethodNode, JavaMethod>();
    private Map<FieldNode, JavaField> fieldsMap = new HashMap<FieldNode, JavaField>();
    
    JavaClass(ClassNode classNode, JadxDecompiler decompiler) {
        this.decompiler = decompiler;
        this.cls = classNode;
        this.parent = null;
    }

    /**
     * Inner classes constructor
     */
    JavaClass(ClassNode classNode, JavaClass parent) {
        this.decompiler = null;
        this.cls = classNode;
        this.parent = parent;
    }

    public String getCode() {
        CodeWriter code = cls.getCode();
        if (code == null) {
            decompile();
            code = cls.getCode();
            if (code == null) {
                return "";
            }
        }
        return code.getCodeStr();
    }

    public synchronized void decompile() {
        if (decompiler == null) {
            return;
        }
        if (cls.getCode() == null) {
            decompiler.processClass(cls);
            load();
        }
    }

    public ClassNode getClassNode() {
        return cls;
    }

    private void load() {
        int inClsCount = cls.getInnerClasses().size();
        if (inClsCount != 0) {
            List<JavaClass> list = new ArrayList<JavaClass>(inClsCount);
            for (ClassNode inner : cls.getInnerClasses()) {
                if (!inner.contains(AFlag.DONT_GENERATE)) {
                    JavaClass javaClass = new JavaClass(inner, this);
                    javaClass.load();
                    list.add(javaClass);
                }
            }
            this.innerClasses = Collections.unmodifiableList(list);
        }

        int fieldsCount = cls.getFields().size();
        if (fieldsCount != 0) {
            List<JavaField> flds = new ArrayList<JavaField>(fieldsCount);
            for (FieldNode f : cls.getFields()) {
                if (!f.contains(AFlag.DONT_GENERATE)) {
                    JavaField javaField = new JavaField(f, this);
                    flds.add(javaField);
                }
            }
            this.fields = Collections.unmodifiableList(flds);
        }

        int methodsCount = cls.getMethods().size();
        if (methodsCount != 0) {
            List<JavaMethod> mths = new ArrayList<JavaMethod>(methodsCount);
            for (MethodNode m : cls.getMethods()) {
                if (!m.contains(AFlag.DONT_GENERATE)) {
                    JavaMethod javaMethod = new JavaMethod(this, m);
                    mths.add(javaMethod);
                }
            }
            Collections.sort(mths, new Comparator<JavaMethod>() {
                    @Override
                    public int compare(JavaMethod o1, JavaMethod o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                });
            this.methods = Collections.unmodifiableList(mths);
        }
    }

    private JadxDecompiler getRootDecompiler() {
        if (parent != null) {
            return parent.getRootDecompiler();
        }
        return decompiler;
    }

    @Override
    public String getName() {
        return cls.getShortName();
    }

    @Override
    public String getFullName() {
        return cls.getFullName();
    }

    public String getPackage() {
        return cls.getPackage();
    }

    @Override
    public JavaClass getDeclaringClass() {
        return parent;
    }

    @Override
    public JavaClass getTopParentClass() {
        return parent == null ? this : parent.getTopParentClass();
    }

    public AccessInfo getAccessInfo() {
        return cls.getAccessFlags();
    }

    public List<JavaClass> getInnerClasses() {
        decompile();
        return innerClasses;
    }

    public List<JavaField> getFields() {
        decompile();
        return fields;
    }

    public List<JavaMethod> getMethods() {
        decompile();
        return methods;
    }

    public int getDecompiledLine() {
        return cls.getDecompiledLine();
    }

    @Override
    public boolean equals(Object o) {
        return this == o || o instanceof JavaClass && cls.equals(((JavaClass) o).cls);
    }

    @Override
    public int hashCode() {
        return cls.hashCode();
    }

    @Override
    public String toString() {
        return cls.getFullName() + "[ " + getFullName() + " ]";
    }
}
