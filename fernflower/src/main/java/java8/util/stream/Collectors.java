package java8.util.stream;
import java8.util.function.Supplier;
import java8.util.function.BiConsumer;
import java8.util.function.Function;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;
import java.util.Set;
import java8.util.function.BinaryOperator;
import java.util.Collections;
import java.util.HashMap;

public final class Collectors {
    //static final Set<Collector.Characteristics> CH_NOID = Collections.emptySet();
    
     /**
     * Simple implementation class for {@code Collector}.
     *
     * @param <T> the type of elements to be collected
     * @param <R> the type of the result
     */
    static class CollectorImpl<T, A, R> implements Collector<T, A, R> {
        private final Supplier<A> supplier;
        private final BiConsumer<A, T> accumulator;
        private final BinaryOperator<A> combiner;
        private final Function<A, R> finisher;
        private final Set<Characteristics> characteristics;

        CollectorImpl(Supplier<A> supplier,
                      BiConsumer<A, T> accumulator,
                      BinaryOperator<A> combiner,
                      Function<A,R> finisher,
                      Set<Characteristics> characteristics) {
            this.supplier = supplier;
            this.accumulator = accumulator;
            this.combiner = combiner;
            this.finisher = finisher;
            this.characteristics = characteristics;
        }
        CollectorImpl(Supplier<A> supplier, BiConsumer<A, T> accumulator) {
            this(supplier, accumulator, null,null,null);
        }

        CollectorImpl(Supplier<A> supplier, BiConsumer<A, T> accumulator, Function<A, R> finisher) {
            this(supplier, accumulator, null,finisher,null);
        }
        
        @Override
        public BiConsumer<A, T> accumulator() {
            return accumulator;
        }

        @Override
        public Supplier<A> supplier() {
            return supplier;
        }

        @Override
        public BinaryOperator<A> combiner() {
            return combiner;
        }

        @Override
        public Function<A, R> finisher() {
            return finisher;
        }

        @Override
        public Set<Characteristics> characteristics() {
            return characteristics;
        }
    }
    /**
     * Returns a {@code Collector} that concatenates input elements into new string.
     * 
     * @param delimiter  the delimiter between each element
     * @return a {@code Collector}
     */
    public static Collector<CharSequence, ?, String> joining(CharSequence delimiter) {
        return joining(delimiter, "", "");
    }

    /**
     * Returns a {@code Collector} that concatenates input elements into new string.
     * 
     * @param delimiter  the delimiter between each element
     * @param prefix  the prefix of result
     * @param suffix  the suffix of result
     * @return a {@code Collector}
     */
    public static Collector<CharSequence, ?, String> joining(CharSequence delimiter, CharSequence prefix, CharSequence suffix) {
        return joining(delimiter, prefix, suffix, prefix.toString() + suffix.toString());
    }

    /**
     * Returns a {@code Collector} that concatenates input elements into new string.
     * 
     * @param delimiter  the delimiter between each element
     * @param prefix  the prefix of result
     * @param suffix  the suffix of result
     * @param emptyValue  the string which replaces empty element if exists
     * @return a {@code Collector}
     */
    public static Collector<CharSequence, ?, String> joining(
        final CharSequence delimiter,
        final CharSequence prefix,
        final CharSequence suffix,
        final String emptyValue) {
        return new CollectorImpl<CharSequence, StringBuilder, String>(

            new Supplier<StringBuilder>() {
                @Override
                public StringBuilder get() {
                    return new StringBuilder();
                }
            },

            new BiConsumer<StringBuilder, CharSequence>() {
                @Override
                public void accept(StringBuilder t, CharSequence u) {
                    if (t.length() > 0) {
                        t.append(delimiter);
                    } else {
                        t.append(prefix);
                    }
                    t.append(u);
                }
            },

            new Function<StringBuilder, String>() {
                @Override
                public String apply(StringBuilder value) {
                    if (value.length() == 0) {
                        return emptyValue;
                    } else {
                        value.append(suffix);
                        return value.toString();
                    }
                }
            }
        );
    }
    
    /**
     * Returns a {@code Collector} that fills new {@code List} with input elements.
     * 
     * @param <T> the type of the input elements
     * @return a {@code Collector}
     */
    public static <T> Collector<T, ?, List<T>> toList() {
        return new CollectorImpl<T, List<T>, List<T>>(

            new Supplier<List<T>>() {
                @Override
                public List<T> get() {
                    return new ArrayList<T>();
                }
            },

            new BiConsumer<List<T>, T>() {
                @Override
                public void accept(List<T> t, T u) {
                    t.add(u);
                }
            }
        );
    }
    /**
     * Returns a {@code Collector} that accumulates elements into a
     * {@code Map} whose keys and values are the result of applying the provided
     * mapping functions to the input elements.
     *
     * <p>If the mapped
     * keys contain duplicates (according to {@link Object#equals(Object)}),
     * the value mapping function is applied to each equal element, and the
     * results are merged using the provided merging function.
     *
     * <p>There are no guarantees on the type, mutability, serializability,
     * or thread-safety of the {@code Map} returned.
     *
     * @apiNote
     * There are multiple ways to deal with collisions between multiple elements
     * mapping to the same key.  The other forms of {@code toMap} simply use
     * a merge function that throws unconditionally, but you can easily write
     * more flexible merge policies.  For example, if you have a stream
     * of {@code Person}, and you want to produce a "phone book" mapping name to
     * address, but it is possible that two persons have the same name, you can
     * do as follows to gracefully deal with these collisions, and produce a
     * {@code Map} mapping names to a concatenated list of addresses:
     * <pre>{@code
     * Map<String, String> phoneBook
     *   = people.stream().collect(
     *     toMap(Person::getName,
     *           Person::getAddress,
     *           (s, a) -> s + ", " + a));
     * }</pre>
     *
     * @implNote
     * The returned {@code Collector} is not concurrent.  For parallel stream
     * pipelines, the {@code combiner} function operates by merging the keys
     * from one map into another, which can be an expensive operation.  If it is
     * not required that results are merged into the {@code Map} in encounter
     * order, using {@link #toConcurrentMap(Function, Function, BinaryOperator)}
     * may offer better parallel performance.
     *
     * @param <T> the type of the input elements
     * @param <K> the output type of the key mapping function
     * @param <U> the output type of the value mapping function
     * @param keyMapper a mapping function to produce keys
     * @param valueMapper a mapping function to produce values
     * @param mergeFunction a merge function, used to resolve collisions between
     *                      values associated with the same key, as supplied
     *                      to {@link Map#merge(Object, Object, BiFunction)}
     * @return a {@code Collector} which collects elements into a {@code Map}
     * whose keys are the result of applying a key mapping function to the input
     * elements, and whose values are the result of applying a value mapping
     * function to all input elements equal to the key and combining them
     * using the merge function
     *
     * @see #toMap(Function, Function)
     * @see #toMap(Function, Function, BinaryOperator, Supplier)
     * @see #toConcurrentMap(Function, Function, BinaryOperator)
     */
    public static <T, K, U>
    Collector<T, ?, Map<K,U>> toMap(Function<? super T, ? extends K> keyMapper,
                                    Function<? super T, ? extends U> valueMapper,
                                    BinaryOperator<U> mergeFunction) {
        return toMap(keyMapper, valueMapper, mergeFunction, Collectors.<K, U>hashMapSupplier());
    }
    
    /**
     * Returns a {@code Collector} that fills new {@code Map} with input elements.
     * 
     * @param <T> the type of the input elements
     * @param <K> the result type of key mapping function
     * @param <V> the result type of value mapping function
     * @param <M> the type of the resulting {@code Map}
     * @param keyMapper  a mapping function to produce keys
     * @param valueMapper  a mapping function to produce values
     * @param mapFactory  a supplier function that provides new {@code Map}
     * @return a {@code Collector}
     */
    public static <T, K, V, M extends Map<K, V>> Collector<T, ?, M> toMap(
        final Function<? super T, ? extends K> keyMapper,
        final Function<? super T, ? extends V> valueMapper,
        final BinaryOperator<V> mergeFunction,
        final Supplier<M> mapFactory) {
            
        return new CollectorImpl<T, M, M>(

            mapFactory,

            new BiConsumer<M, T>() {
                @Override
                public void accept(M map, T t) {
                    final K key = keyMapper.apply(t);
                    final V value = valueMapper.apply(t);
                    final V oldValue = map.get(key);
                    final V newValue = (oldValue == null) ? value :
                        mergeFunction.apply( oldValue,value);
                    if (newValue == null) {
                        map.remove(key);
                    } else {
                        map.put(key, newValue);
                    }
                }
            }
        );
    }
    
    @SuppressWarnings("unchecked")
    static <A, R> Function<A, R> castIdentity() {
        return new Function<A, R>() {

            @Override
            public R apply(A value) {
                return (R) value;
            }
        };
    }
    
    private final static <K, V>  Supplier<Map<K, V>> hashMapSupplier() {
        return new Supplier<Map<K, V>>() {

            @Override
            public Map<K, V> get() {
                return new HashMap<K, V>();
            }
        };
    }
}
