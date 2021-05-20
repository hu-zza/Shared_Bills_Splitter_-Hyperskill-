package hu.zza.hyperskill.splitter.config;

import static hu.zza.hyperskill.splitter.config.MenuParameter.AMOUNT;
import static hu.zza.hyperskill.splitter.config.MenuParameter.COMMAND;
import static hu.zza.hyperskill.splitter.config.MenuParameter.DATE;
import static hu.zza.hyperskill.splitter.config.MenuParameter.FROM;
import static hu.zza.hyperskill.splitter.config.MenuParameter.ITEM;
import static hu.zza.hyperskill.splitter.config.MenuParameter.LIST;
import static hu.zza.hyperskill.splitter.config.MenuParameter.METHOD;
import static hu.zza.hyperskill.splitter.config.MenuParameter.NAME;
import static hu.zza.hyperskill.splitter.config.MenuParameter.TO;

import hu.zza.clim.HeaderStyle;
import hu.zza.clim.Menu;
import hu.zza.clim.MenuBuilder;
import hu.zza.clim.MenuStructureBuilder;
import hu.zza.clim.ParameterMatcherBuilder;
import hu.zza.clim.UserInterface;
import hu.zza.clim.menu.MenuStructure;
import hu.zza.clim.parameter.Parameter;
import hu.zza.clim.parameter.ParameterMatcher;
import hu.zza.clim.parameter.Parameters;
import hu.zza.hyperskill.splitter.Console;
import hu.zza.hyperskill.splitter.transaction.Ledger;
import hu.zza.hyperskill.splitter.transaction.Manager;
import java.time.LocalDate;
import java.util.List;

public abstract class MenuInitializer {

  public static Menu initialize() {

    MenuStructure menuStructure =
        new MenuStructureBuilder()
            .setRawMenuStructure(
                "{\"Shared Bills Splitter\" : [\"balance\", \"balancePerfect\", \"borrow\", \"cashBack\", "
                    + "\"exit\", \"group\", \"help\", \"purchase\", \"repay\", \"secretSanta\", \"writeOff\"]}")
            .setLeaf("exit", Console::exit, "Shared Bills Splitter")
            .setLeaf("help", Console::help, "Shared Bills Splitter")
            .setLeaf("balance", Ledger::getBalance, "Shared Bills Splitter")
            .setLeaf("balancePerfect", Ledger::getPerfectBalance, "Shared Bills Splitter")
            .setLeaf("borrow", Ledger::makeMicroTransaction, "Shared Bills Splitter")
            .setLeaf("cashBack", Ledger::makeMacroTransaction, "Shared Bills Splitter")
            .setLeaf("group", Manager::manageTeam, "Shared Bills Splitter")
            .setLeaf("purchase", Ledger::makeMacroTransaction, "Shared Bills Splitter")
            .setLeaf("repay", Ledger::makeMicroTransaction, "Shared Bills Splitter")
            .setLeaf("secretSanta", Manager::secretSanta, "Shared Bills Splitter")
            .setLeaf("writeOff", Ledger::writeOff, "Shared Bills Splitter")
            .build();

    final String delimiter = " ";
    final String dottedDate = "\\d{4}.\\d{2}.\\d{2}";
    final String captureFrame = "(\\b%s\\b)";

    final String dottedDateRegex = String.format(captureFrame, dottedDate);
    final String wordRegex = String.format(captureFrame, "\\w+");
    final String upperCaseWordRegex = String.format(captureFrame, "[A-Z0-9_]+");
    final String numberRegex = "([+-]?\\d+(?:\\.\\d{1,2})?)";
    final String listRegex = "\\(([+-]?\\w+(?:, [+-]?\\w+)*)\\)";

    final Parameter wordParameter = Parameters.of(wordRegex);
    final Parameter constantParameter = wordParameter.with(String::toUpperCase);
    final Parameter upperCaseWordParameter = Parameters.of(upperCaseWordRegex);
    final Parameter numberParameter = Parameters.of(numberRegex);
    final Parameter listParameter = Parameters.of(listRegex);

    final Parameter optionalDateParameter =
        Parameters.of(dottedDateRegex, () -> String.valueOf(LocalDate.now()));

    final Parameter optionalConstantParameter = constantParameter.with("CLOSE");
    final Parameter optionalListParameter = listParameter.with("");

    ParameterMatcher parameterMatcher =
        new ParameterMatcherBuilder()
            .setCommandRegex("^(?:" + dottedDate + "\\s)?(\\w+)\\b")
            .setLeafParameters("help", delimiter, List.of(COMMAND), List.of(constantParameter))
            .setLeafParameters("exit", delimiter, List.of(COMMAND), List.of(constantParameter))
            .setLeafParameters(
                "writeOff",
                delimiter,
                List.of(DATE, COMMAND),
                List.of(optionalDateParameter, constantParameter))
            .setLeafParameters(
                "secretSanta",
                delimiter,
                List.of(COMMAND, NAME),
                List.of(constantParameter, upperCaseWordParameter))
            .setLeafParameters(
                "balance",
                delimiter,
                List.of(DATE, COMMAND, METHOD, LIST),
                List.of(
                    optionalDateParameter,
                    constantParameter,
                    optionalConstantParameter,
                    optionalListParameter))
            .setLeafParameters(
                "balancePerfect",
                delimiter,
                List.of(DATE, COMMAND, METHOD, LIST),
                List.of(
                    optionalDateParameter,
                    constantParameter,
                    optionalConstantParameter,
                    optionalListParameter))
            .setLeafParameters(
                "group",
                delimiter,
                List.of(COMMAND, METHOD, NAME, LIST),
                List.of(
                    constantParameter,
                    constantParameter,
                    upperCaseWordParameter,
                    optionalListParameter))
            .setLeafParameters(
                "borrow",
                delimiter,
                List.of(DATE, COMMAND, FROM, TO, AMOUNT),
                List.of(
                    optionalDateParameter,
                    constantParameter,
                    wordParameter,
                    wordParameter,
                    numberParameter))
            .setLeafParameters(
                "repay",
                delimiter,
                List.of(DATE, COMMAND, FROM, TO, AMOUNT),
                List.of(
                    optionalDateParameter,
                    constantParameter,
                    wordParameter,
                    wordParameter,
                    numberParameter))
            .setLeafParameters(
                "purchase",
                delimiter,
                List.of(DATE, COMMAND, NAME, ITEM, AMOUNT, LIST),
                List.of(
                    optionalDateParameter,
                    constantParameter,
                    wordParameter,
                    wordParameter,
                    numberParameter,
                    listParameter))
            .setLeafParameters(
                "cashBack",
                delimiter,
                List.of(DATE, COMMAND, NAME, ITEM, AMOUNT, LIST),
                List.of(
                    optionalDateParameter,
                    constantParameter,
                    wordParameter,
                    wordParameter,
                    numberParameter,
                    listParameter))
            .build();

    return new MenuBuilder()
        .setMenuStructure(menuStructure)
        .setClimOptions(UserInterface.PARAMETRIC, HeaderStyle.HIDDEN)
        .setParameterMatcher(parameterMatcher)
        .build();
    }
}
