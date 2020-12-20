package hu.zza.hyperskill.splitter.config;

import hu.zza.clim.ControlType;
import hu.zza.clim.Menu;
import hu.zza.clim.MenuEntry;
import hu.zza.clim.MenuStructure;
import hu.zza.clim.Position;
import hu.zza.clim.parameter.Parameter;
import hu.zza.clim.parameter.ParameterMatcher;
import hu.zza.clim.parameter.ParameterPattern;
import hu.zza.hyperskill.splitter.Console;
import hu.zza.hyperskill.splitter.transaction.Ledger;
import hu.zza.hyperskill.splitter.transaction.Manager;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;

import static hu.zza.hyperskill.splitter.config.MenuParameter.AMOUNT;
import static hu.zza.hyperskill.splitter.config.MenuParameter.COMMAND;
import static hu.zza.hyperskill.splitter.config.MenuParameter.DATE;
import static hu.zza.hyperskill.splitter.config.MenuParameter.FROM;
import static hu.zza.hyperskill.splitter.config.MenuParameter.ITEM;
import static hu.zza.hyperskill.splitter.config.MenuParameter.LIST;
import static hu.zza.hyperskill.splitter.config.MenuParameter.METHOD;
import static hu.zza.hyperskill.splitter.config.MenuParameter.NAME;
import static hu.zza.hyperskill.splitter.config.MenuParameter.TO;


public abstract class MenuInitializer
{
    public static Menu initialize()
    {
        /////////////////////////
        /////////////////////////
        //  STRUCTURAL SETTINGS
        //
        
        var menuStructure = new MenuStructure();
        
        //////////
        // NODES
        
        menuStructure.put(new MenuEntry.Node(MenuNode.ROOT,
                                             "Shared Bills Splitter",
                                             MenuLeaf.BALANCE,
                                             MenuLeaf.BALANCEPERFECT,
                                             MenuLeaf.BORROW,
                                             MenuLeaf.CASHBACK,
                                             MenuLeaf.EXIT,
                                             MenuLeaf.GROUP,
                                             MenuLeaf.HELP,
                                             MenuLeaf.PURCHASE,
                                             MenuLeaf.REPAY,
                                             MenuLeaf.SECRETSANTA,
                                             MenuLeaf.WRITEOFF
        ));
        
        
        //////////
        // LEAVES
        
        // Constant forward link arrays for the most typical cases.
        // The schema: FORWARD_<success>_<fail>
        
        final MenuNode[] FORWARD_ROOT_ROOT = {MenuNode.ROOT, MenuNode.ROOT};
        
        
        // GENERAL leaf
        
        menuStructure.put(new MenuEntry.Leaf(MenuLeaf.EXIT, "exit", Console::exit, FORWARD_ROOT_ROOT));
        
        menuStructure.put(new MenuEntry.Leaf(MenuLeaf.HELP, "help", Console::help, FORWARD_ROOT_ROOT));
        
        
        // ROOT (parent node)
        
        menuStructure.put(new MenuEntry.Leaf(MenuLeaf.BALANCE, "balance", Ledger::getBalance, FORWARD_ROOT_ROOT));
        
        menuStructure.put(new MenuEntry.Leaf(MenuLeaf.BALANCEPERFECT,
                                             "balancePerfect",
                                             Ledger::getPerfectBalance,
                                             FORWARD_ROOT_ROOT
        ));
        
        menuStructure.put(new MenuEntry.Leaf(MenuLeaf.BORROW,
                                             "borrow",
                                             Ledger::makeMicroTransaction,
                                             FORWARD_ROOT_ROOT
        ));
        
        menuStructure.put(new MenuEntry.Leaf(MenuLeaf.CASHBACK,
                                             "cashBack",
                                             Ledger::makeMacroTransaction,
                                             FORWARD_ROOT_ROOT
        ));
        
        menuStructure.put(new MenuEntry.Leaf(MenuLeaf.GROUP, "group", Manager::manageTeam, FORWARD_ROOT_ROOT));
        
        menuStructure.put(new MenuEntry.Leaf(MenuLeaf.PURCHASE,
                                             "purchase",
                                             Ledger::makeMacroTransaction,
                                             FORWARD_ROOT_ROOT
        ));
        
        menuStructure.put(new MenuEntry.Leaf(MenuLeaf.REPAY, "repay", Ledger::makeMicroTransaction, FORWARD_ROOT_ROOT));
        
        menuStructure.put(new MenuEntry.Leaf(MenuLeaf.SECRETSANTA,
                                             "secretSanta",
                                             Manager::secretSanta,
                                             FORWARD_ROOT_ROOT
        ));
        
        menuStructure.put(new MenuEntry.Leaf(MenuLeaf.WRITEOFF, "writeOff", Ledger::writeOff, FORWARD_ROOT_ROOT));
        
        
        menuStructure.setFinalized();
        
        
        /////////////////////////
        /////////////////////////
        //  PARAMETRIC MAGIC
        //
        
        // REGEXES
        
        final String delimiter    = " ";
        final String dottedDate   = "\\d{4}\\.\\d{2}\\.\\d{2}";
        final String captureFrame = "(\\b%s\\b)";
        
        final String dottedDateRegex    = String.format(captureFrame, dottedDate);
        final String wordRegex          = String.format(captureFrame, "\\w+");
        final String upperCaseWordRegex = String.format(captureFrame, "[A-Z0-9_]+");
        final String numberRegex        = "([+-]?\\d+(?:\\.\\d{1,2})?)";
        final String listRegex          = "\\(([+-]?\\w+(?:, [+-]?\\w+)*)\\)";
        
        
        // PARAMETERS : required
        
        final var commandParameter = new Parameter<MenuLeaf>(wordRegex, MenuLeaf::getConstantByName);
        
        final var constantParameter = new Parameter<MenuConstant>(wordRegex, MenuConstant::getConstantByName);
        
        final var wordParameter = new Parameter<String>(wordRegex, String::valueOf);
        
        final var upperCaseWordParameter = new Parameter<String>(upperCaseWordRegex, String::valueOf);
        
        final var numberParameter = new Parameter<String>(numberRegex, String::valueOf);
        
        final var listParameter = new Parameter<List<String>>(listRegex, MenuParser::toStringList);
        
        
        // PARAMETERS : optional
        
        final var optionalDateParameter = new Parameter<LocalDate>(dottedDateRegex,
                                                                   MenuParser::toLocalDateFromDotted,
                                                                   LocalDate::now
        );
        
        final var optionalConstantParameter = constantParameter.with(() -> MenuConstant.CLOSE);
        
        final var optionalListParameter = listParameter.with(List::of);
        
        
        // BUILDING patternMap
        
        HashMap<Position, ParameterPattern> patternMap = new HashMap<>();
        ParameterPattern                    parameterPattern;
        
        
        parameterPattern = new ParameterPattern(delimiter, List.of(COMMAND), List.of(commandParameter));
        
        patternMap.put(MenuLeaf.HELP, parameterPattern);
        patternMap.put(MenuLeaf.EXIT, parameterPattern);
        
        
        parameterPattern = new ParameterPattern(delimiter,
                                                List.of(DATE, COMMAND),
                                                List.of(optionalDateParameter, commandParameter)
        );
        
        patternMap.put(MenuLeaf.WRITEOFF, parameterPattern);
        
        
        parameterPattern = new ParameterPattern(delimiter,
                                                List.of(COMMAND, NAME),
                                                List.of(commandParameter, upperCaseWordParameter)
        );
        
        patternMap.put(MenuLeaf.SECRETSANTA, parameterPattern);
        
        
        parameterPattern = new ParameterPattern(delimiter,
                                                List.of(DATE, COMMAND, METHOD, LIST),
                                                List.of(optionalDateParameter,
                                                        commandParameter,
                                                        optionalConstantParameter,
                                                        optionalListParameter
                                                )
        );
        
        patternMap.put(MenuLeaf.BALANCE, parameterPattern);
        patternMap.put(MenuLeaf.BALANCEPERFECT, parameterPattern);
        
        
        parameterPattern = new ParameterPattern(delimiter,
                                                List.of(COMMAND, METHOD, NAME, LIST),
                                                List.of(commandParameter,
                                                        constantParameter,
                                                        upperCaseWordParameter,
                                                        optionalListParameter
                                                )
        );
        
        patternMap.put(MenuLeaf.GROUP, parameterPattern);
        
        
        parameterPattern = new ParameterPattern(delimiter,
                                                List.of(DATE, COMMAND, FROM, TO, AMOUNT),
                                                List.of(optionalDateParameter,
                                                        commandParameter,
                                                        wordParameter,
                                                        wordParameter,
                                                        numberParameter
                                                )
        );
        
        patternMap.put(MenuLeaf.BORROW, parameterPattern);
        patternMap.put(MenuLeaf.REPAY, parameterPattern);
        
        
        parameterPattern = new ParameterPattern(delimiter,
                                                List.of(DATE, COMMAND, NAME, ITEM, AMOUNT, LIST),
                                                List.of(optionalDateParameter,
                                                        commandParameter,
                                                        wordParameter,
                                                        wordParameter,
                                                        numberParameter,
                                                        listParameter
                                                )
        );
        
        patternMap.put(MenuLeaf.PURCHASE, parameterPattern);
        patternMap.put(MenuLeaf.CASHBACK, parameterPattern);
        
        
        String           commandRegex     = "^(?:" + dottedDate + "\\s)?(\\w+)\\b";
        ParameterMatcher parameterMatcher = new ParameterMatcher(commandRegex, patternMap);
        
        return Menu.of(menuStructure, ControlType.PARAMETRIC, MenuNode.class, MenuLeaf.class, parameterMatcher);
    }
}
