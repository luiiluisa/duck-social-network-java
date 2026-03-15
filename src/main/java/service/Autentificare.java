package service;

import model.User;
import repository.UserRepository;

public class Autentificare {

    private UserRepository userRepository;
    private User current;

    public Autentificare(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean login(String username, String password) {
        if (username == null || password == null) return false;
        for (User u : userRepository.findAll()) {
            if (username.equals(u.getUsername()) && password.equals(u.getPassword())) {
                current = u;
                return true;
            }
        }
        return false;
    }

    public void logout() { current = null; }

    public boolean isLoggedIn() { return current != null; }

    public User currentUser() { return current; }
}
