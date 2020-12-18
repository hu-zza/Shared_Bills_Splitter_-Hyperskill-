package hu.zza.hyperskill.splitter.menu.parameter;

import hu.zza.hyperskill.splitter.menu.Message;

import java.util.function.Function;
import java.util.function.Supplier;


public class Parameter<T>
{
    private final String                    regex;
    private final Function<CharSequence, T> parseFunction;
    private final boolean                   optional;
    private final Supplier<T>               defaultValueSupplier;
    private       T                         value;
    private       boolean                   present;
    
    
    public Parameter(String regex, Function<CharSequence, T> parseFunction)
    {
        this(regex, parseFunction, null);
    }
    
    
    public Parameter(String regex, Function<CharSequence, T> parseFunction, Supplier<T> defaultValueSupplier
    )
    {
        if (regex == null || regex.isBlank())
        {
            throw new IllegalArgumentException("Parameter 'pattern' can not be null.");
        }
        
        if (parseFunction == null) throw new IllegalArgumentException("Parameter 'parseFunction' can not be null.");
        
        this.regex                = regex;
        this.parseFunction        = parseFunction;
        this.optional             = defaultValueSupplier != null;
        this.defaultValueSupplier = defaultValueSupplier;
        this.present              = true;
    }
    
    
    String getRegex()
    {
        return regex;
    }
    
    
    boolean isOptional()
    {
        return optional;
    }
    
    
    boolean isPresent()
    {
        return !isOptional() || present; // = isOptional() ? present : true
    }
    
    
    void setPresent(boolean present)
    {
        this.present = present;
    }
    
    
    public T getValue()
    {
        return value;
    }
    
    
    /**
     * It returns the field <code>value</code> of the <code>Parameter</code> or an object by its <code>defaultValueSupplier</code>
     * if the former is null. (For optional <code>Parameter</code> objects.)
     * <p>
     * If this Parameter is not optional, the <code>defaultValueSupplier</code> is null, so it returns <code>(T) new Object()</code>.
     *
     * @return An object of type T: The value of the Parameter / defaultValueSupplier / new Object().
     */
    @SuppressWarnings("unchecked")
    public T getOrDefault()
    {
        return value != null ? value : defaultValueSupplier != null ? defaultValueSupplier.get() : (T) new Object();
    }
    
    
    public Parameter<T> with(Supplier<T> defaultValueSupplier)
    {
        return new Parameter<T>(regex, parseFunction, defaultValueSupplier);
    }
    
    
    @Override
    protected Parameter<T> clone()
    {
        return new Parameter<T>(regex, parseFunction, defaultValueSupplier);
    }
    
    
    void parse(CharSequence text)
    {
        try
        {
            value = parseFunction.apply(text);
        }
        catch (Exception e)
        {
            if (optional)
            {
                value = defaultValueSupplier.get();
            }
            else
            {
                throw new IllegalArgumentException(String.format(Message.PARSING_EXCEPTION.getMessage(), text, "%s"));
            }
        }
    }
}
