package validator;

import exceptions.ValidationException;
import model.User;

public class UserValidator implements ValidationStrategy<User> {
    @Override
    public boolean validate(User u) {
        if (u == null) throw new ValidationException("User null");
        //if (u.getId() == null) throw new ValidationException("Id lipsa");
        if (u.getUsername() == null || u.getUsername().trim().isEmpty())
            throw new ValidationException("Username gol");
        if (u.getEmail() == null || !u.getEmail().contains("@"))
            throw new ValidationException("Email invalid");
        if (u.getPassword() == null || u.getPassword().length() < 3)
            throw new ValidationException("Parola prea scurta");
        return true;
    }
}
