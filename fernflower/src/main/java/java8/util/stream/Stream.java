
package java8.util.stream;
import java.util.Collection;
import java.util.Iterator;
import java8.util.function.Predicate;
import java8.util.function.Function;
import java8.util.function.Consumer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.HashSet;
import java8.util.function.ToIntFunction;

public class Stream<X>{
    
    public static <x> Stream<x> from(Collection<x> c){
        return new Stream<x>(c);
    }
    
    protected Collection<X> collection;
    
    protected Stream(Collection<X> c){
        collection=c;
    }
    
    public Stream<X> filter(Predicate<X> p){
        Iterator<X> iter=collection.iterator();
        List<X> list=new ArrayList<X>();
        while(iter.hasNext()){
            X x=iter.next();
            if(p.test(x))
                list.add(x);
        }
        
        return new Stream<X>(list);
    }
    
    public <R> Stream<R> map(Function<X,R> mapper){
        Iterator<X> iter=collection.iterator();      
        if(!iter.hasNext()) return new Stream<R>((Collection<R>)Collections.emptyList());
        Collection<R> list=new ArrayList<R>(collection.size());
        for(X x=iter.next();iter.hasNext();x=iter.next()){
            list.add(mapper.apply(x));
        }
        return new Stream<R>(list);
    }
    
    public void forEach(Consumer<X> action){
        Iterator<X> iter=collection.iterator();
        if(!iter.hasNext()) return ;
        for(X x=iter.next();iter.hasNext();x=iter.next()){
            action.accept(x);
        }
    }
    
    public X findFirst(){
        Iterator<X> iter=collection.iterator();
        if(iter.hasNext())return iter.next();
        return null;
    }
    
    public Stream<X> sorted(Comparator<X> comparator){
        List<X> list=new ArrayList<X>(collection);
        Collections.sort(list,comparator);
        return new Stream<X>(list);
    }
    
    public boolean anyMatch(Predicate<X> p){
        Iterator<X> iter=collection.iterator();
        if(!iter.hasNext()) return false;
        boolean result=false;
        for(X x=iter.next();iter.hasNext();x=iter.next()){
            if(p.test(x))result=true;
        }
        return result;
    }
   
    public Stream<X> distinct(){
        
        Iterator<X> iter=collection.iterator();      
        if(!iter.hasNext()) return new Stream<X>((Collection<X>)Collections.emptyList());
        Collection<X> set=new HashSet<X>(collection.size());
        for(X x=iter.next();iter.hasNext();x=iter.next()){
            set.add(x);
        }
        return new Stream<X>(set);
    }
    
    public <R, A> R collect(Collector<? super X, A, R> collector) {
        Iterator<X> iterator=collection.iterator();            
        A container = collector.supplier().get();
        
        while (iterator.hasNext()) {
            final X value = iterator.next();
            collector.accumulator().accept(container, value);
        }
        if (collector.finisher() != null)
            return collector.finisher().apply(container);
        return Collectors.<A, R>castIdentity().apply(container);
    }
    
    public int count(){
        return collection.size();
    }
    
    public IntStream mapToInt(ToIntFunction<? super X> mapper){
        Iterator<X> iter=collection.iterator();      
        Collection<Integer> list=new ArrayList<Integer>(collection.size());
        for(X x=iter.next();iter.hasNext();x=iter.next()){
            list.add(mapper.applyAsInt(x));
        }
        return new IntStream(list);
    }  
}
