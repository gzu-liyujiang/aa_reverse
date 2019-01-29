package jadx.core.codegen;

import jadx.annotation.Nullable;

import jadx.api.IResultSaver;
import jadx.api.JadxPreferences;

public class CodeWriter {

    public StringBuilder buf = new StringBuilder();

    private final static String NL="\n\r";
    @Nullable
    private String code;
    private String indentStr;
    private int indentCount;

    private int line = 1;

    public CodeWriter() {
        this.indentCount = 0;
        this.indentStr = JadxPreferences.Option_GetStr(JadxPreferences.OPT_INDENT_STRING);
    }

    public void setIndentStr(String indentStr){
        this.indentStr=indentStr;
    }
    
    public CodeWriter startLine() {
        addLine();
        addLineIndent();
        return this;
    }

    public CodeWriter startLine(char c) {
        addLine();
        addLineIndent();
        add(c);
        return this;
    }

    public CodeWriter startLine(String str) {
        addLine();
        addLineIndent();
        add(str);
        return this;
    }

    public CodeWriter startLineWithNum(int sourceLine) {
        if (sourceLine == 0) {
            startLine();
            return this;
        }

        startLine();

        return this;
    }

    public CodeWriter add(String str) {
        buf.append(str);
        return this;
    }

    public CodeWriter add(char c) {
        buf.append(c);
        return this;
    }

    CodeWriter add(CodeWriter code) {
        line--;
        line += code.line;
        buf.append(code.buf);
        return this;
    }

    public CodeWriter newLine() {
        addLine();
        return this;
    }

    public CodeWriter addIndent() {
        int indent_count=indentCount;
        while(--indent_count>-1)
            add(indentStr);
        return this;
    }

    private void addLine() {
        buf.append(NL);
        line++;
    }

    private CodeWriter addLineIndent() {
        addIndent();
        return this;
    }

    public void incIndent() {
        incIndent(1);
    }

    public void decIndent() {
        decIndent(1);
    }

    public void incIndent(int c) {
        this.indentCount += c;
    }

    public void decIndent(int c) {
        if((indentCount -= c)<0)
            indentCount=0;
    }

    public int getLine() {
        return line;
    }


    public void finish() {
        removeFirstEmptyLine();
        buf.trimToSize();
        code = buf.toString();
        buf = null;
    }

    private void removeFirstEmptyLine() {
        if (buf.indexOf(NL) == 0) {
            buf.delete(0, NL.length());
        }
    }

    public int bufLength() {
        return buf.length();
    }

    public String getCodeStr() {
        return code;
    }

    @Override
    public String toString() {
        return buf == null ? code : buf.toString();
    }

    public void save(IResultSaver saver,String classFullPath) {
        if (code == null) {
            finish();
        }
        saver.saveResult(classFullPath,code);
    }

}
