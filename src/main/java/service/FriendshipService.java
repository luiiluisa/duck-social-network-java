package service;

import model.Friendship;
import model.User;
import repository.FriendshipRepository;
import repository.UserRepository;

import java.util.*;

/**
 * Logica pentru relațiile de prietenie, comunități și componenta cu diametru maxim.
 */
public class FriendshipService {

    private final UserRepository userRepository;
    private final FriendshipRepository friendshipRepository;

    public FriendshipService(UserRepository userRepository, FriendshipRepository friendshipRepository) {
        this.userRepository = userRepository;
        this.friendshipRepository = friendshipRepository;
    }

    /**
     * Returneaza toate prieteniile existente.
     */
    public List<Friendship> findAllFriendships() {
        // presupunem că findAllEdges() întoarce List<Friendship>
        return new ArrayList<>(friendshipRepository.findAllEdges());
    }

    /**
     * Creează o prietenie intre doi utilizatori.
     */
    public boolean addFriend(Long userId1, Long userId2) {
        User a = findUser(userId1);
        User b = findUser(userId2);
        if (a == null || b == null) return false;
        return friendshipRepository.addFriendship(a, b);
    }

    /**
     * Elimina prietenia dintre doi utilizatori.
     */
    public boolean removeFriend(Long userId1, Long userId2) {
        User a = findUser(userId1);
        User b = findUser(userId2);
        if (a == null || b == null) return false;
        return friendshipRepository.removeFriendship(a, b);
    }

    public int removeAllForUserId(Long userId) {
        User u = findUser(userId);
        if (u == null) return 0;
        return friendshipRepository.removeAllForUser(u);
    }

    /**
     * Caută utilizatori după substring în username.
     */
    public List<User> searchByName(String text) {
        List<User> result = new ArrayList<>();
        if (text == null) return result;
        String q = text.toLowerCase();
        for (User u : userRepository.findAll()) {
            if (u.getUsername() != null && u.getUsername().toLowerCase().contains(q)) {
                result.add(u);
            }
        }
        return result;
    }

    /**
     * Numără componentele conexe din graful prieteniilor.
     */
    public int countCommunities() {
        List<User> users = userRepository.findAll();
        Map<Long, List<Long>> g = buildGraph(users);
        Set<Long> seen = new HashSet<>();
        int comps = 0;
        for (User u : users) {
            Long id = u.getId();
            if (id != null && !seen.contains(id)) {
                comps++;
                bfsMark(id, g, seen);
            }
        }
        return comps;
    }

    /**
     * Returnează lista de utilizatori din componenta conexă cu diametrul maxim.
     */
    public List<User> mostSociableCommunity() {
        List<User> users = userRepository.findAll();
        Map<Long, List<Long>> g = buildGraph(users);
        Set<Long> seen = new HashSet<>();
        List<User> best = new ArrayList<>();
        int bestDiameter = -1;

        for (User u : users) {
            Long start = u.getId();
            if (start == null || seen.contains(start)) continue;
            Set<Long> comp = bfsCollect(start, g, seen);

            int diameter = 0;
            for (Long v : comp) {
                diameter = Math.max(diameter, bfsEccentricity(v, g, comp));
            }

            if (diameter > bestDiameter) {
                bestDiameter = diameter;
                best = idsToUsers(comp, users);
            }
        }
        return best;
    }

    // ================== metode private ==================

    private User findUser(Long id) {
        if (id == null) return null;
        for (User u : userRepository.findAll()) {
            if (id.equals(u.getId())) return u;
        }
        return null;
    }

    /**
     * Construiește lista de adiacență din prietenii (a↔b).
     */
    private Map<Long, List<Long>> buildGraph(List<User> users) {
        Map<Long, List<Long>> g = new HashMap<>();
        for (User u : users)
            if (u.getId() != null)
                g.put(u.getId(), new ArrayList<>());

        for (Friendship f : friendshipRepository.findAllEdges()) {
            Long a = f.getUser1().getId(), b = f.getUser2().getId();
            if (a == null || b == null) continue;
            g.get(a).add(b);
            g.get(b).add(a);
        }
        return g;
    }

    /**
     * Marchează nodurile vizitate într-o componentă.
     */
    private void bfsMark(Long start, Map<Long, List<Long>> g, Set<Long> seen) {
        Deque<Long> q = new ArrayDeque<>();
        q.add(start);
        seen.add(start);
        while (!q.isEmpty()) {
            Long x = q.poll();
            for (Long y : g.getOrDefault(x, List.of())) {
                if (seen.add(y)) q.add(y);
            }
        }
    }

    /**
     * Returnează un set cu toți userii care fac parte dintr-o componentă specifică.
     */
    private Set<Long> bfsCollect(Long start, Map<Long, List<Long>> g, Set<Long> seenGlobal) {
        Set<Long> comp = new HashSet<>();
        Deque<Long> q = new ArrayDeque<>();
        q.add(start);
        seenGlobal.add(start);
        comp.add(start);
        while (!q.isEmpty()) {
            Long x = q.poll();
            for (Long y : g.getOrDefault(x, List.of())) {
                if (seenGlobal.add(y)) {
                    q.add(y);
                    comp.add(y);
                }
            }
        }
        return comp;
    }

    /**
     * Calculează excentricitatea unui nod (folosita la diametru).
     */
    private int bfsEccentricity(Long src, Map<Long, List<Long>> g, Set<Long> comp) {
        Map<Long, Integer> dist = new HashMap<>();
        Deque<Long> q = new ArrayDeque<>();
        q.add(src);
        dist.put(src, 0);
        while (!q.isEmpty()) {
            Long x = q.poll();
            for (Long y : g.getOrDefault(x, List.of())) {
                if (comp.contains(y) && !dist.containsKey(y)) {
                    dist.put(y, dist.get(x) + 1);
                    q.add(y);
                }
            }
        }
        int ecc = 0;
        for (Long v : comp) ecc = Math.max(ecc, dist.getOrDefault(v, 0));
        return ecc;
    }

    private List<User> idsToUsers(Set<Long> ids, List<User> users) {
        Map<Long, User> byId = new HashMap<>();
        for (User u : users)
            if (u.getId() != null)
                byId.put(u.getId(), u);

        List<User> out = new ArrayList<>();
        for (Long id : ids) {
            User u = byId.get(id);
            if (u != null) out.add(u);
        }
        return out;
    }

    public List<User> friendsOf(Long userId) {
        if (userId == null) return List.of();

        List<User> users = userRepository.findAll();
        Map<Long, User> byId = new HashMap<>();
        for (User u : users) if (u.getId() != null) byId.put(u.getId(), u);

        Set<User> friends = new HashSet<>();
        for (Friendship f : friendshipRepository.findAllEdges()) {
            Long a = f.getUser1().getId();
            Long b = f.getUser2().getId();
            if (a == null || b == null) continue;

            if (userId.equals(a)) friends.add(byId.get(b));
            if (userId.equals(b)) friends.add(byId.get(a));
        }

        friends.remove(null);
        return new ArrayList<>(friends);
    }


}
