package hu.zza.hyperskill.splitter.transaction;

import static hu.zza.hyperskill.splitter.config.MenuParameter.AMOUNT;
import static hu.zza.hyperskill.splitter.config.MenuParameter.COMMAND;
import static hu.zza.hyperskill.splitter.config.MenuParameter.DATE;
import static hu.zza.hyperskill.splitter.config.MenuParameter.LIST;
import static hu.zza.hyperskill.splitter.config.MenuParameter.METHOD;
import static hu.zza.hyperskill.splitter.config.MenuParameter.NAME;

import hu.zza.clim.menu.ProcessedInput;
import hu.zza.clim.parameter.ParameterName;
import hu.zza.hyperskill.splitter.config.MenuConstant;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ParameterParser {

  static LocalDate getDate(ProcessedInput processedInput) {
    String rawDate = processedInput.getParameter(DATE).getOrDefault();

    Matcher matcher = Pattern.compile("\\d{4}(.)\\d{2}(.)\\d{2}").matcher(rawDate);

    if (matcher.find()) {
      try {
        String dateTimePattern = String.format("yyyy%sMM%sdd", matcher.group(1), matcher.group(2));
        return LocalDate.parse(rawDate, DateTimeFormatter.ofPattern(dateTimePattern));
      } catch (Exception e) {
        throw new IllegalArgumentException("Invalid date format.");
      }
    } else {
      return LocalDate.now();
    }
  }

  static String getName(ProcessedInput processedInput) {
    return processedInput.getParameter(NAME).getValue();
  }

  static Team getTeam(ProcessedInput processedInput) {
    return RepositoryManager.teamOf(processedInput.getParameter(NAME).getValue());
  }

  static String getCommand(ProcessedInput processedInput) {
    return processedInput.getParameter(COMMAND).getValue();
  }

  static BigDecimal getAmount(ProcessedInput processedInput) {
    return new BigDecimal(processedInput.getParameter(AMOUNT).getValue());
  }

  static MenuConstant getMethod(ProcessedInput processedInput) {
    return MenuConstant.valueOf(processedInput.getParameter(METHOD).getOrDefault());
  }

  static Account getAccount(
      ProcessedInput processedInput, ParameterName parameterName) {
    return RepositoryManager.accountOf(processedInput.getParameter(parameterName).getValue());
  }

  static List<Account> getAccountList(ProcessedInput processedInput) {
    String rawString = processedInput.getParameter(LIST).getOrDefault();
    return Manager.createTemporaryTeam(Arrays.asList(rawString.split(",\\s+")));
  }
}
