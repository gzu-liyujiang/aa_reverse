
package java8.util.stream;
import java.util.Collection;
import java.util.Iterator;

public class IntStream extends Stream<Integer>
{
    private final Iterator<Integer> iter;
    IntStream(Collection<Integer> c){
        super(c);
        iter=collection.iterator();
    }
    public int num(){
        return iter.next();
    }
}
