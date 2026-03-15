package validator;

public interface ValidationStrategy<T> {
    boolean validate(T t);
}
