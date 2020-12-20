package hu.zza.hyperskill.splitter;

import hu.zza.hyperskill.splitter.config.MenuInitializer;
import hu.zza.clim.Menu;
import hu.zza.clim.parameter.Parameter;
import hu.zza.clim.parameter.ParameterName;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Scanner;


@Component
public class Console implements CommandLineRunner
{
    private final static Menu    menu = MenuInitializer.initialize();
    private static       boolean waitingForUserInput;
    
    
    public static int help(Map<ParameterName, Parameter> parameterMap)
    {
        menu.listOptions();
        return 0;
    }
    
    
    public static int exit(Map<ParameterName, Parameter> parameterMap)
    {
        waitingForUserInput = false;
        return 0;
    }
    
    
    @Override
    public void run(String[] args)
    throws Exception
    {
        waitingForUserInput = true;
        try (var scanner = new Scanner(System.in))
        {
            while (waitingForUserInput)
            {
                if (scanner.hasNext())
                {
                    menu.chooseOption(scanner.nextLine());
                }
                else
                {
                    waitingForUserInput = false;
                }
            }
        }
    }
}

