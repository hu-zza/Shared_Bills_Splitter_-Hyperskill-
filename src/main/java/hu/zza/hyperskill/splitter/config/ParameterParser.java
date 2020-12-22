package hu.zza.hyperskill.splitter.config;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public abstract class ParameterParser
{
    public static LocalDate parseLocalDate(CharSequence charSequence)
    {
        Matcher matcher = Pattern.compile("\\d{4}(.)\\d{2}(.)\\d{2}").matcher(charSequence);
        
        if (matcher.find())
        {
            String dateTimePattern = String.format("yyyy%sMM%sdd", matcher.group(1), matcher.group(2));
            return LocalDate.parse(charSequence, DateTimeFormatter.ofPattern(dateTimePattern));
        }
        else
        {
            return LocalDate.now();
        }
    }
    
    
    public static List<String> parseStringList(CharSequence charSequence)
    {
        return Arrays.asList(charSequence.toString().split(",\\s+"));
    }
}
