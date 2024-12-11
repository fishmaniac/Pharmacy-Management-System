package PharmacyManagementSystem;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

enum Level {
    Trace,
    Debug,
    Info,
    Warning,
    Error,
    TUI, // Terminal UI
    Audit,
}

public class Log {
    public static final Level LOG_LEVEL = Level.Trace;

    public static void logStack() {
        System.out.println(levelPrefix(Level.Trace) + "Stack Trace: " + getCaller());
    }

    public static void trace(String message) {
        log(Level.Trace, true, message);
    }

    public static void debug(String message) {
        log(Level.Debug, true, message);
    }

    public static void info(String message) {
        log(Level.Info, true, message);
    }

    public static void warning(String message) {
        log(Level.Warning, true, message);
    }

    public static void error(String message) {
        log(Level.Error, true, message);
    }

    public static void tui(String message) {
        log(Level.TUI, false, "\t" + message);
    }

    public static void audit(String message) {
        log(Level.Audit, true, message + " - " + LocalDateTime.now() + " - Initiated by user: " + Backend.get().getLoggedIn());
    }

    public static void auditAnonymous(String message) {
        log(Level.Audit, true, message);
    }

    private static void log(Level log_level, boolean show_level, String message) {
        if (log_level.ordinal() >= LOG_LEVEL.ordinal()) {
            System.out.println((show_level ? levelPrefix(log_level) : "") + message);
        }
    }

    private static String getCaller() {
        return StackWalker.getInstance()
                .walk(
                        frames ->
                                frames.skip(1)
                                        .map(frame -> frame.getMethodName())
                                        .collect(Collectors.joining("->")));
    }

    private static String levelPrefix(Level log_level) {
        switch (log_level) {
            case Trace:
                return "[TRACE]\t";
            case Debug:
                return "[DEBUG]\t";
            case Info:
                return "[INFO]\t";
            case Warning:
                return "[WARNING]\t";
            case Error:
                return "[ERROR]\t";
            case TUI:
                return "[TUI]\t";
            case Audit:
                return "[AUDIT]\t";
            default:
                throw new IllegalArgumentException("Invalid levelPrefix log level: " + log_level);
        }
    }
}
