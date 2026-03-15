package repository;

import model.User;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import model.Duck;
import java.util.Comparator;


public class UserRepository {

    private Map<Long, User> data = new HashMap<>();

    /**
     * Salveaza un user si genereaza id
     * @return utilizatorul salvat, sau null dacă e invalid
     */
    public User save(User user) {
        if (user == null) return null;

        // dacă userul NU are id → îi generăm noi unul
        if (user.getId() == null) {
            long max = 0;
            for (Long key : data.keySet()) {
                if (key != null && key > max) max = key;
            }
            user.setId(max + 1);
        }

        data.put(user.getId(), user);
        return user;
    }

    /**
     * Actualizeaza un utilizator existent
     * @return utilizatorul actualizat sau null dacă nu exista acel id
     */
    public User update(User user) {
        if (user == null || user.getId() == null) return null;
        if (!data.containsKey(user.getId())) return null;
        data.put(user.getId(), user);
        return user;
    }

    /**
     * Șterge un utilizator dupa id.
     * @return true daca a existat si s-a sters
     */
    public boolean delete(Long id) {
        if (id == null) return false;
        return data.remove(id) != null;
    }

    /**
     * Returneaza toți utilizatorii existenți.
     */
    public List<User> findAll() {
        return new ArrayList<>(data.values());
    }

    /**
     * Gasește un utilizator dupa id
     */
    public User findById(Long id) {
        if (id == null) return null;
        return data.get(id);
    }

    /**
     * Paginare simplă pentru rațe (varianta in-memory).
     * Va fi suprascrisă de UserBDRepository pentru BD.
     */
    public List<Duck> findDucksPage(int page, int pageSize) {
        List<Duck> ducks = new ArrayList<>();
        for (User u : data.values()) {
            if (u instanceof Duck d) {
                ducks.add(d);
            }
        }

        // sortăm după id ca să fie ordonare stabilă
        ducks.sort(Comparator.comparingLong(d -> d.getId() == null ? Long.MAX_VALUE : d.getId()));

        int from = page * pageSize;
        if (from >= ducks.size()) {
            return new ArrayList<>();
        }
        int to = Math.min(from + pageSize, ducks.size());

        return new ArrayList<>(ducks.subList(from, to));
    }

    /**
     * Numărul total de rațe (in-memory).
     * Varianta reală pentru BD este în UserBDRepository.
     */
    public int countDucks() {
        int cnt = 0;
        for (User u : data.values()) {
            if (u instanceof Duck) cnt++;
        }
        return cnt;
    }
}



