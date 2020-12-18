package hu.zza.hyperskill.splitter.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;


public abstract class MenuParser
{
    public static LocalDate toLocalDateFromDotted(CharSequence charSequence)
    {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return LocalDate.parse(charSequence, formatter);
    }
    
    
    public static List<String> toStringList(CharSequence charSequence)
    {
        return Arrays.asList(charSequence.toString().split(",\\s+"));
    }
}
