package hu.zza.hyperskill.splitter.menu;

public enum Message
{
    INVALID_COMMAND("Unknown command."), INVALID_ARGUMENT("Illegal command arguments"),
    INVALID_POSITION("Incorrect or unavailable menu position."),
    PARSING_EXCEPTION("Parsing the input '%s' for parameter '%s' fails.%nMaybe it does not satisfy some restraints."),
    PROCESSING_EXCEPTION("%2$s%n"), // "The menu can not process the given input: %s%nCause: %s%n%n"
    INITIALIZATION_EXCEPTION("Incorrect parameters. Object initialization fails.");
    
    private String message;
    
    
    Message(String message)
    {
        this.message = message;
    }
    
    
    public String getMessage()
    {
        return message;
    }
}
