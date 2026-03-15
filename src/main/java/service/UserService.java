package service;

import factory.UserFactory;
import model.User;
import model.Duck;
import repository.UserRepository;
import validator.UserValidator;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Logica pentru utilizatori: CRUD, validare și (de)serializare CSV.
 */
public class UserService {

    private final UserRepository userRepository;
    private final UserValidator userValidator;
    private final UserFactory userFactory;
    private FriendshipService friendshipService;
    private String autosavePath;

    public UserService(UserRepository userRepository, UserValidator userValidator, UserFactory userFactory) {
        this.userRepository = userRepository;
        this.userValidator = userValidator;
        this.userFactory = userFactory;
    }


    public void enableAutosave(String filePath) { this.autosavePath = filePath; }
    public void disableAutosave() { this.autosavePath = null; }
    private void autosave() {
        if (autosavePath != null) saveToCsv(autosavePath);
    }


    public List<Duck> findDucksPage(int page, int pageSize) {
        return userRepository.findDucksPage(page, pageSize);
    }

    public int countDucks() {
        return userRepository.countDucks();
    }


    public User addUser(User user) {
        if (userValidator.validate(user)) {
            User saved = userRepository.save(user);
            if (saved != null) autosave();
            return saved;
        }
        return null;
    }


    public void setFriendshipService(FriendshipService fs) { this.friendshipService = fs; }
    public boolean removeUser(Long id) {
        if (id == null) return false;
        if (friendshipService != null) {
            friendshipService.removeAllForUserId(id);
        }
        boolean ok = userRepository.delete(id);
        if (ok) autosave();
        return ok;
    }


    public List<User> findAll() {
        return userRepository.findAll();
    }


    /**
     * Încarcă utilizatori din fișier CSV (o inregistrare pe linie), folosind factory + validator.
     * @return numărul de utilizatori încărcați cu succes
     */
    public int loadFromCsv(String filePath) {
        List<String> lines;
        try {
            lines = Files.readAllLines(Path.of(filePath));
        } catch (Exception e) {
            return 0;
        }

        int count = 0;
        for (String line : lines) {
            if (line == null) continue;
            String trimmed = line.trim();
            if (trimmed.isEmpty()) continue;

            User u = userFactory.createFromCsv(trimmed);
            if (u == null) continue;

            if (userValidator.validate(u)) {
                if (userRepository.save(u) != null) {
                    count++;
                }
            }
        }
        return count;
    }

    /**
     * Salvează toți utilizatorii în CSV
     * @return numărul de linii scrise
     */
    public int saveToCsv(String filePath) {
        try {
            var lines = new ArrayList<String>();
            for (User u : userRepository.findAll()) {
                if (!userValidator.validate(u)) {
                    throw new exceptions.ValidationException("User invalid, nu poate fi salvat!");
                }
                if (u instanceof model.Person p) {
                    lines.add(String.join(",",
                            "PERSON",
                            nv(p.getUsername()),
                            nv(p.getEmail()),
                            nv(p.getPassword()),
                            nv(p.getNume()),
                            nv(p.getPrenume()),
                            p.getDataNasterii() == null ? "" : p.getDataNasterii().toString(),
                            nv(p.getOcupatie()),
                            String.valueOf(p.getNivelEmpatie())
                    ));
                } else if (u instanceof model.Duck d) {
                    lines.add(String.join(",",
                            "DUCK",
                            nv(d.getUsername()),
                            nv(d.getEmail()),
                            nv(d.getPassword()),
                            d.getTip() == null ? "" : d.getTip().name(),
                            String.valueOf(d.getViteza()),
                            String.valueOf(d.getRezistenta())
                    ));
                }
            }
            Path path = Path.of(filePath);
            if (path.getParent() != null) {
                try { Files.createDirectories(path.getParent()); } catch (Exception ignored) {}
            }
            Files.write(path, lines);
            return lines.size();
        } catch (Exception e) {
            return 0;
        }
    }

    private static String nv(String s) { return s == null ? "" : s; }

    public User login(String usernameOrEmail, String rawPassword) {
        if (usernameOrEmail == null || usernameOrEmail.isBlank()) return null;
        if (rawPassword == null) rawPassword = "";

        for (User u : userRepository.findAll()) {
            boolean match = usernameOrEmail.equals(u.getUsername()) || usernameOrEmail.equals(u.getEmail());
            if (match) {
                String stored = u.getPassword();
                return security.PasswordUtils.verifyPassword(rawPassword, stored) ? u : null;
            }
        }
        return null;
    }

}
