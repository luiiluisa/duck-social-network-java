package exceptions;

public class FriendshipException extends RuntimeException {
    public FriendshipException() {}
    public FriendshipException(String message) { super(message); }
    public FriendshipException(String message, Throwable cause) { super(message, cause); }
    public FriendshipException(Throwable cause) { super(cause); }
}
