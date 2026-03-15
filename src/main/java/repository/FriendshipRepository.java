package repository;

import model.Friendship;
import model.User;

import java.util.HashSet;
import java.util.Set;

/**
 * Repository in-memory pentru muchiile de prietenie (graf neorientat).
 * Folosește Set pentru a evita duplicate.
 */
public class FriendshipRepository {

    private Set<Friendship> edges = new HashSet<>();

    /**
     * Adaugă o prietenie între doi utilizatori.
     * @return true dacă s-a inserat, false dacă exista sau intrare invalidă
     */
    public boolean addFriendship(User a, User b) {
        if (a == null || b == null || a == b) return false;
        return edges.add(new Friendship(a, b));
    }

    /**
     * Elimină prietenia dintre doi utilizatori.
     * @return true dacă a existat și s-a eliminat
     */
    public boolean removeFriendship(User a, User b) {
        if (a == null || b == null) return false;
        return edges.remove(new Friendship(a, b));
    }

    public int removeAllForUser(User u) {
        if (u == null) return 0;
        int before = edges.size();
        edges.removeIf(f -> f.getUser1().equals(u) || f.getUser2().equals(u));
        return before - edges.size();
    }

    /**
     * Returnează o copie a muchiilor existente (pentru calculul comunităților).
     */
    public Set<Friendship> findAllEdges() {
        return new HashSet<>(edges);
    }
}
