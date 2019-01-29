package aenu.reverse.ui;
import com.myopicmobile.textwarrior.common.ILanguage;
import com.myopicmobile.textwarrior.common.ILexer;
import com.myopicmobile.textwarrior.common.ILexer.LexCallback;
import com.myopicmobile.textwarrior.common.DocumentProvider;
import com.myopicmobile.textwarrior.common.Pair;
import java.util.List;
import java.util.ArrayList;
import java.io.StringReader;
import lexer.JavaLexer;
import lexer.JavaType;

public class JavaLanguage implements ILanguage ,ILexer
{
    
    public static final JavaLanguage instance= new JavaLanguage();
    
    private LexThread lexT;
    
    private JavaLanguage(){}
    
    @Override
    public void tokenize(DocumentProvider hDoc, ILexer.LexCallback cb){
        cancelTokenize();
        lexT=new LexThread(this,hDoc,cb);
        lexT.start();
    }

    @Override
    public void cancelTokenize(){
        if(lexT!=null){
            lexT.interrupt();
            lexT=null;
        }
    }
    
    @Override
    public boolean isSentenceTerminator(char c){
        return (c=='.');
    }

    @Override
    public boolean isWhitespace(char c){
        return (
        c==' '||
        c=='\n'||
        c=='\t'||
        c=='\r'||
        c=='\f'||
        c==EOF);
    }

    @Override
    public ILexer getLexer(){
        // TODO: Implement this method
        return this;
    }
    
    static private class LexThread extends Thread{

        private DocumentProvider doc;
        private ILexer.LexCallback callback;
        private final Pair zero=new Pair(0,NORMAL);
        private ILexer lexer;
        private LexThread(ILexer l,DocumentProvider dp,ILexer.LexCallback cb){
            doc=dp;
            callback=cb;
            lexer=l;
        }

        @Override
        public void run(){
            List<Pair> r=tokenize();
            if(r.isEmpty())
                r.add(zero);
            callback.lexDone(r);
        }

        public List<Pair> tokenize(){
           
            List<Pair> tokens = new ArrayList<>();

            StringReader stringReader=new StringReader(doc.toString());
            //CLexer cLexer=new CLexer(stringReader);
            JavaLexer cLexer=new JavaLexer(stringReader);

            JavaType cType=null;
            int idx=0;
            String identifier=null;//存储标识符
            //language.clearUserWord();
            while (cType!=JavaType.EOF){
                try {
                    cType=cLexer.yylex();
                    switch (cType)
                    {
                        //关键字
                        case KEYWORD:
                        tokens.add(new Pair(idx, KEYWORD));
                        break;
                        //注释
                        case COMMENT:
                        tokens.add(new Pair(idx, DOUBLE_SYMBOL_DELIMITED_MULTILINE));
                        break;
                        //预处理，宏
                        //case PRETREATMENT_LINE:
                        //case DEFINE_LINE:
                        //  tokens.add(new Pair(idx, SINGLE_SYMBOL_LINE_A));
                        //  break;
                        //字符串，字符
                        case STRING:
                        case CHARACTER_LITERAL:
                        tokens.add(new Pair(idx, SINGLE_SYMBOL_DELIMITED_A));
                        break;
                        //数字
                        case INTEGER_LITERAL:
                        case FLOATING_POINT_LITERAL:
                        tokens.add(new Pair(idx, NUMBER));
                        break;
                        case IDENTIFIER:
                        identifier=cLexer.yytext();
                        tokens.add(new Pair(idx, NORMAL));
                        break;
                        //处理标识符后面的符号
                        case  LPAREN://左括号
                        case  RPAREN://右括号
                        case  LBRACK://左中括号
                        case COMMA://逗号
                        //case WHITE_CHAR://空格
                        case SEMICOLON://分号
                        //case OPERATOR://运算符
                        /*if(identifier!=null) {
                            language.addUserWord(identifier);
                            language.updateUserWord();
                            identifier=null;
                        }*/
                        tokens.add(new Pair(idx, NORMAL));
                        break;
                        default:
                        tokens.add(new Pair(idx, NORMAL));
                    }
                    idx+=cLexer.yytext().length();
                } catch (Exception e) {
                    e.printStackTrace();
                    idx++;//错误了，索引也要往后挪
                }
            }

            /*
            if (tokens.isEmpty()){
                // return value cannot be empty
                tokens.add(new Pair(0, NORMAL));
            }*/
            //printList(tokens);
            return tokens;
		}
    }
}
