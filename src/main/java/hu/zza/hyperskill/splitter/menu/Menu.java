package hu.zza.hyperskill.splitter.menu;

import hu.zza.hyperskill.splitter.menu.parameter.Parameter;
import hu.zza.hyperskill.splitter.menu.parameter.ParameterMatcher;
import hu.zza.hyperskill.splitter.menu.parameter.ParameterName;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;


public class Menu
{
    private final MenuStructure             menuStructure;
    private final ControlType               controlType;
    private final Map<String, NodePosition> nodeNameMap;
    private final Map<String, LeafPosition> leafNameMap;
    private final ParameterMatcher          parameterMatcher;
    private       Position                  position;
    private       Position                  command;
    private       Position[]                options;
    
    
    private Menu(MenuStructure menuStructure,
                 ControlType controlType,
                 Class<? extends NodePosition> nodeEnum,
                 Class<? extends LeafPosition> leafEnum,
                 Position initialPosition,
                 ParameterMatcher parameterMatcher
    )
    {
        this.menuStructure    = menuStructure;
        this.controlType      = controlType;
        this.position         = initialPosition;
        this.parameterMatcher = parameterMatcher;
        
        Map<String, NodePosition> tmpNodeMap = new HashMap<>();
        for (var node : nodeEnum.getEnumConstants())
        {
            tmpNodeMap.put(node.name(), node);
        }
        this.nodeNameMap = Map.copyOf(tmpNodeMap);
        
        Map<String, LeafPosition> tmpLeafMap = new HashMap<>();
        for (var leaf : leafEnum.getEnumConstants())
        {
            tmpLeafMap.put(leaf.name(), leaf);
        }
        this.leafNameMap = Map.copyOf(tmpLeafMap);
        
        refreshOptions();
    }
    
    
    public static Menu of(MenuStructure menuStructure,
                          Class<? extends NodePosition> nodeEnum,
                          Class<? extends LeafPosition> leafEnum
    )
    {
        return of(menuStructure, ControlType.ORDINAL, nodeEnum, leafEnum);
    }
    
    
    public static Menu of(MenuStructure menuStructure,
                          ControlType controlType,
                          Class<? extends NodePosition> nodeEnum,
                          Class<? extends LeafPosition> leafEnum
    )
    {
        return of(menuStructure, controlType, nodeEnum, leafEnum, null);
    }
    
    
    public static Menu of(MenuStructure menuStructure,
                          ControlType controlType,
                          Class<? extends NodePosition> nodeEnum,
                          Class<? extends LeafPosition> leafEnum,
                          ParameterMatcher parameterMatcher
    )
    {
        return of(menuStructure, controlType, nodeEnum, leafEnum, null, parameterMatcher);
    }
    
    
    public static Menu of(MenuStructure menuStructure,
                          ControlType controlType,
                          Class<? extends NodePosition> nodeEnum,
                          Class<? extends LeafPosition> leafEnum,
                          Position initialPosition,
                          ParameterMatcher parameterMatcher
    )
    {
        try
        {
            if (initialPosition == null) initialPosition = nodeEnum.getEnumConstants()[0];
            Menu menu = new Menu(menuStructure, controlType, nodeEnum, leafEnum, initialPosition, parameterMatcher);
            return menu;
        }
        catch (Exception e)
        {
            throw new IllegalArgumentException(Message.INITIALIZATION_EXCEPTION.getMessage());
        }
        
    }
    
    
    private static void warnAboutInput(String input, Exception e)
    {
        System.out.printf(Message.PROCESSING_EXCEPTION.getMessage(), input, e.getMessage());
        //System.err.printf(Message.PROCESSING_EXCEPTION.getMessage(), parameter, e.getMessage());
    }
    
    
    public void listOptions()
    {
        refreshOptions();
        if (options.length == 0) return;
        switch (controlType)
        {
            case ORDINAL:
            case ORDINAL_TRAILING_ZERO:
                printMenu();
                break;
            
            case NOMINAL:
            case PARAMETRIC:
                Arrays.stream(options).map(menuStructure::get).map(MenuEntry::getName).forEach(System.out::println);
                break;
            
            default:
                break;
        }
    }
    
    
    public void chooseOption(String input)
    {
        if (input == null || input.isBlank()) return;
        
        refreshOptions();
        try
        {
            switch (controlType)
            {
                case NOMINAL:
                    chooseOptionByNominal(getPositionByName(input), Map.of());
                    break;
                
                case ORDINAL:
                case ORDINAL_TRAILING_ZERO:
                    int ordinal = Integer.parseInt(input);
                    chooseOptionByOrdinal(ordinal, Map.of());
                    break;
                
                case PARAMETRIC:
                    extractAndUpdateCommandField(input);
                    parameterMatcher.setText(input);
                    chooseOptionByNominal(command, parameterMatcher.processText(command));
                    break;
                
                default:
                    break;
            }
            refreshOptions();
        }
        catch (Exception e)
        {
            warnAboutInput(input, e);
        }
    }
    
    
    private Position getPositionByName(String name)
    {
        String upperCaseName = name.toUpperCase();
        
        if (nodeNameMap.containsKey(upperCaseName))
        {
            return nodeNameMap.get(upperCaseName);
        }
        else if (leafNameMap.containsKey(upperCaseName))
        {
            return leafNameMap.get(upperCaseName);
        }
        else
        {
            throw new IllegalArgumentException(Message.INVALID_COMMAND.getMessage());
        }
    }
    
    
    private void printMenu()
    {
        boolean trailingZero = controlType == ControlType.ORDINAL_TRAILING_ZERO;
        
        int i = trailingZero ? 1 : 0;
        
        for (; i < options.length; i++) printMenuEntry(menuStructure.get(options[i]), i);
        
        if (trailingZero) printMenuEntry(menuStructure.get(options[0]), 0);
    }
    
    
    private void chooseOptionByOrdinal(int ordinal, Map<ParameterName, Parameter> parameterMap)
    {
        if (ordinal < 0 || options.length <= ordinal)
        {
            throw new IllegalArgumentException(Message.INVALID_POSITION.getMessage());
        }
        
        setMenuPosition(options[ordinal], parameterMap);
    }
    
    
    private void chooseOptionByNominal(Position nominal, Map<ParameterName, Parameter> parameterMap)
    {
        if (Arrays.asList(options).contains(nominal))
        {
            setMenuPosition(nominal, parameterMap);
        }
        else
        {
            throw new IllegalArgumentException(Message.INVALID_POSITION.getMessage());
        }
    }
    
    
    private void refreshOptions()
    {
        options = menuStructure.get(position).getLinks();
    }
    
    
    private void extractAndUpdateCommandField(String commandString)
    {
        parameterMatcher
                .getCommandRegex()
                .matcher(commandString)
                .results()
                .findFirst()
                .ifPresent(m -> command = getPositionByName(m.group(1)));
    }
    
    
    private void printMenuEntry(MenuEntry menuEntry, Integer ordinal)
    {
        if (menuEntry != null)
        {
            if (ordinal != null)
            {
                System.out.printf("%d. %s%n", ordinal, menuEntry.getName());
            }
            else
            {
                System.out.printf("%s%n", menuEntry.getName());
            }
        }
    }
    
    
    private void setMenuPosition(Position key, Map<ParameterName, Parameter> parameterMap)
    {
        if (menuStructure.containsKey(key))
        {
            position = menuStructure.get(key).select(parameterMap);
        }
        else
        {
            throw new IllegalArgumentException(Message.INVALID_COMMAND.getMessage());
        }
    }
}
