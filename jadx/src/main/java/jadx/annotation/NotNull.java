package jadx.annotation;
import java.lang.annotation.*;

@Documented()
@Retention(value=RetentionPolicy.CLASS)
@Target(value={ElementType.METHOD,ElementType.FIELD,ElementType.PARAMETER,ElementType.LOCAL_VARIABLE,})
public @interface NotNull
{
    public String value() default "";
}

