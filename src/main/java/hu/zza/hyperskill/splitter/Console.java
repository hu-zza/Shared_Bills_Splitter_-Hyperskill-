package hu.zza.hyperskill.splitter;

import hu.zza.clim.Menu;
import hu.zza.clim.menu.ProcessedInput;
import hu.zza.hyperskill.splitter.config.MenuInitializer;
import java.util.Scanner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class Console implements CommandLineRunner {

  private static boolean waitingForUserInput;
  private static final Menu menu = MenuInitializer.initialize();

  public static int help(ProcessedInput processedInput) {
    menu.listOptions();
    return 0;
  }

  public static int exit(ProcessedInput processedInput) {
    waitingForUserInput = false;
    return 0;
  }

  @Override
  public void run(String[] args) throws Exception {
    waitingForUserInput = true;
    try (var scanner = new Scanner(System.in)) {
      while (waitingForUserInput) {
        if (scanner.hasNext()) {
          menu.chooseOption(scanner.nextLine());
        } else {
          waitingForUserInput = false;
        }
      }
    }
  }
}
