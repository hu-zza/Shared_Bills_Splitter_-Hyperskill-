package hu.zza.hyperskill.splitter.config;

import hu.zza.hyperskill.splitter.menu.Message;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public enum MenuConstant
{
    OPEN, CLOSE, CREATE, ADD, REMOVE, SHOW;
    
    private static final List<String> nameList = Arrays.stream(values()).map(Enum::name).collect(Collectors.toList());
    
    
    /**
     * Checks if <code>name</code> is a valid constant name of MenuConstant.
     *
     * @param name the string to validate.
     *
     * @return true if there is a proper constant.
     */
    public static boolean isConstantName(String name)
    {
        return nameList.contains(name);
    }
    
    
    static MenuConstant getConstantByName(CharSequence name)
    {
        String upperCaseName = name.toString().toUpperCase();
        
        if (isConstantName(upperCaseName))
        {
            return Enum.valueOf(MenuConstant.class, upperCaseName);
        }
        else
        {
            throw new IllegalArgumentException(Message.INVALID_ARGUMENT.getMessage());
        }
    }
}
