package hu.zza.hyperskill.splitter.transaction;

import static hu.zza.hyperskill.splitter.config.MenuParameter.AMOUNT;
import static hu.zza.hyperskill.splitter.config.MenuParameter.COMMAND;
import static hu.zza.hyperskill.splitter.config.MenuParameter.DATE;
import static hu.zza.hyperskill.splitter.config.MenuParameter.METHOD;

import hu.zza.clim.parameter.Parameter;
import hu.zza.clim.parameter.ParameterName;
import hu.zza.hyperskill.splitter.config.MenuConstant;
import hu.zza.hyperskill.splitter.config.MenuParameter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class ParameterParser {

  static LocalDate getDate(Map<ParameterName, Parameter> parameterMap) {
    String rawDate = parameterMap.get(DATE).getOrDefault();

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

  static String getName(Map<ParameterName, Parameter> parameterMap) {
    return parameterMap.get(MenuParameter.NAME).getValue();
  }

  static Team getTeam(Map<ParameterName, Parameter> parameterMap) {
    return RepositoryManager.teamOf(parameterMap.get(MenuParameter.NAME).getValue());
  }

  static MenuLeaf getCommand(Map<ParameterName, Parameter> parameterMap) {
    return MenuLeaf.valueOf(parameterMap.get(COMMAND).getValue());
  }

  static BigDecimal getAmount(Map<ParameterName, Parameter> parameterMap) {
    return new BigDecimal(parameterMap.get(AMOUNT).getValue());
  }

  static MenuConstant getMethod(Map<ParameterName, Parameter> parameterMap) {
    return MenuConstant.valueOf(parameterMap.get(METHOD).getOrDefault());
  }

  static Account getAccount(
      Map<ParameterName, Parameter> parameterMap, ParameterName parameterName) {
    return RepositoryManager.accountOf(parameterMap.get(parameterName).getValue());
  }

  static List<Account> getAccountList(Map<ParameterName, Parameter> parameterMap) {
    String rawString = parameterMap.get(MenuParameter.LIST).getOrDefault();
    return Manager.createTemporaryTeam(Arrays.asList(rawString.split(",\\s+")));
  }
}
