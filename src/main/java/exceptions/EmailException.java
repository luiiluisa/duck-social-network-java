package exceptions;

public class EmailException extends ValidationException {
    public EmailException() {}
    public EmailException(String message) { super(message); }
    public EmailException(String message, Throwable cause) { super(message, cause); }
    public EmailException(Throwable cause) { super(cause); }
}
