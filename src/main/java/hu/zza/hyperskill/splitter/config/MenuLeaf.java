package hu.zza.hyperskill.splitter.config;

import hu.zza.hyperskill.splitter.menu.LeafPosition;
import hu.zza.hyperskill.splitter.menu.Message;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;


public enum MenuLeaf implements LeafPosition
{
    BALANCE, BALANCEPERFECT, BORROW, CASHBACK, EXIT, GROUP, HELP, PURCHASE, REPAY, SECRETSANTA, WRITEOFF;
    
    private static final List<String> nameList = Arrays.stream(values()).map(Enum::name).collect(Collectors.toList());
    
    
    /**
     * Checks if <code>name</code> is a valid constant name of MenuLeaf.
     *
     * @param name the string to validate.
     *
     * @return true if there is a proper constant.
     */
    public static boolean isConstantName(String name)
    {
        return nameList.contains(name);
    }
    
    
    static MenuLeaf getConstantByName(CharSequence name)
    {
        String upperCaseName = name.toString().toUpperCase();
        
        if (isConstantName(upperCaseName))
        {
            return Enum.valueOf(MenuLeaf.class, upperCaseName);
        }
        else
        {
            throw new IllegalArgumentException(Message.INVALID_COMMAND.getMessage());
        }
    }
}
