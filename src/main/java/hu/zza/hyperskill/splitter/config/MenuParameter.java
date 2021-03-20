package hu.zza.hyperskill.splitter.config;

import hu.zza.clim.parameter.ParameterName;

public enum MenuParameter implements ParameterName {
  DATE,
  COMMAND,
  METHOD,
  NAME,
  FROM,
  TO,
  AMOUNT,
  LIST,
  ITEM;

  @Override
  public String toString() {
    return super.toString().toLowerCase();
  }
}
