package hu.zza.hyperskill.splitter.menu.parameter;

import hu.zza.hyperskill.splitter.menu.Message;
import hu.zza.hyperskill.splitter.menu.Position;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class ParameterMatcher
{
    private final Pattern                             commandRegex;
    private final HashMap<Position, ParameterPattern> patternMap;
    private       String                              text;
    
    
    public ParameterMatcher(String commandRegex, HashMap<Position, ParameterPattern> patternMap)
    {
        this(commandRegex, 0, "", patternMap);
    }
    
    
    public ParameterMatcher(String commandRegex, int flags, String text, HashMap<Position, ParameterPattern> patternMap
    )
    {
        this.commandRegex = Pattern.compile(commandRegex, flags);
        this.text         = text;
        this.patternMap   = patternMap;
    }
    
    
    public Pattern getCommandRegex()
    {
        return commandRegex;
    }
    
    
    public void setText(String text)
    {
        this.text = text;
    }
    
    
    public Map<ParameterName, Parameter> processText(Position command)
    {
        ParameterPattern parameterPattern = patternMap.get(command);
        if (parameterPattern == null) throw new IllegalArgumentException(Message.INVALID_POSITION.getMessage());
        
        List<ParameterName> parameterNames = parameterPattern.getParameterNames();
        List<Parameter>     parameterList  = prepareParameterList(parameterPattern);
        
        String regex = ParameterPattern.getRegex(parameterPattern.getDelimiter(), parameterList);
        
        Matcher matcher = Pattern.compile(regex).matcher(text);
        
        if (matcher.find())
        {
            var matchResult = matcher.toMatchResult();
            
            var updateList = parameterList.stream().filter(Parameter::isPresent).collect(Collectors.toList());
            
            for (int i = 1; i <= matchResult.groupCount(); i++)
            {
                try
                {
                    updateList.get(i - 1).parse(matchResult.group(i));
                }
                catch (IllegalArgumentException exception)
                {
                    throw new IllegalArgumentException(String.format(exception.getMessage(),
                                                                     parameterNames.get(i).toString()
                    ));
                }
            }
        }
        else
        {
            throw new IllegalArgumentException(Message.INVALID_ARGUMENT.getMessage());
        }
        
        Map<ParameterName, Parameter> result = new HashMap<>();
        
        for (int i = 0; i < parameterNames.size(); i++)
        {
            result.put(parameterNames.get(i), parameterList.get(i));
        }
        
        return result;
    }
    
    
    private List<Parameter> prepareParameterList(ParameterPattern parameterPattern)
    {
        String          delimiter     = parameterPattern.getDelimiter();
        int             optionalCount = parameterPattern.getOptionalCount();
        List<Parameter> parameterList = parameterPattern.getParameterClonesList();
        if (optionalCount == 0) return parameterList;
        
        
        int   optionalIndex       = 0;
        int   requiredIndex       = 0;
        int[] positionsOfOptional = new int[optionalCount];
        
        for (int i = 0; i < parameterList.size(); i++)
        {
            if (parameterList.get(i).isOptional())
            {
                positionsOfOptional[optionalIndex++] = i;
            }
        }
        
        
        return updateAndGetParameterList(delimiter, optionalCount, parameterList, positionsOfOptional);
    }
    
    
    private List<Parameter> updateAndGetParameterList(String delimiter,
                                                      int optionalCount,
                                                      List<Parameter> parameterList,
                                                      int[] positionsOfOptional
    )
    {
        // First try to match with all (optionalCount) optionals ( + all non-optionals),
        // then with optionalCount - 1, and so on... At least without any optional (all non-optionals only).
        // The inner cycle iterates through all combinations with given count (i) of optionals.
        for (int i = optionalCount; 0 <= i; i--)
        {
            for (int[] selectedIndices : generateCombinations(optionalCount, i))
            {
                var selectedOptional = new int[i];
                for (int j = 0; j < i; j++)
                {
                    selectedOptional[j] = positionsOfOptional[selectedIndices[j]];
                }
                
                setPresentFields(parameterList, positionsOfOptional, selectedOptional);
                
                if (Pattern.matches(ParameterPattern.getRegex(delimiter, parameterList), text)) return parameterList;
            }
        }
        throw new IllegalArgumentException(Message.INVALID_ARGUMENT.getMessage());
    }
    
    
    private void setPresentFields(List<Parameter> parameterList, int[] optionalIndices, int[] selectedIndices)
    {
        for (int i : optionalIndices)
        {
            parameterList.get(i).setPresent(false);
        }
        
        for (int i : selectedIndices)
        {
            parameterList.get(i).setPresent(true);
        }
    }
    
    
    /**
     * Generates all <code>r</code> sized combinations for range 0..<code>n</code> (included, excluded).
     *
     * @param n Upper boundary for the generation. (excluded)
     * @param r Size of a generated set.
     *
     * @return Combinations in lexicographic order.
     */
    private List<int[]> generateCombinations(int n, int r)
    {
        List<int[]> combinations = new ArrayList<>();
        int[]       combination  = new int[r];
        
        // Initialize with lowest lexicographic combination
        for (int i = 0; i < r; i++)
        {
            combination[i] = i;
        }
        
        // PATCH...
        if (r == 0) return List.of(new int[] {0});
        if (r == n) return List.of(combination);
        
        
        while (combination[r - 1] < n)
        {
            combinations.add(combination.clone());
            
            // Generate next combination in lexicographic order
            int t = r - 1;
            while (t != 0 && combination[t] == n - r + t)
            {
                t--;
            }
            combination[t]++;
            for (int i = t + 1; i < r; i++)
            {
                combination[i] = combination[i - 1] + 1;
            }
        }
        
        return combinations;
    }
}
