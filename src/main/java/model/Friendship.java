package model;

import java.util.Objects;

/**
 * Reprezintă o prietenie bidirecțională între doi utilizatori.
 */
public class Friendship {
    private User user1;
    private User user2;

    public Friendship(User user1, User user2) {
        this.user1 = user1;
        this.user2 = user2;
    }

    public User getUser1() { return user1; }
    public User getUser2() { return user2; }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Friendship that = (Friendship) o;
        long a1 = Math.min(user1.getId(), user2.getId());
        long b1 = Math.max(user1.getId(), user2.getId());
        long a2 = Math.min(that.user1.getId(), that.user2.getId());
        long b2 = Math.max(that.user1.getId(), that.user2.getId());
        return a1 == a2 && b1 == b2;
    }

    @Override
    public int hashCode() {
        long a = Math.min(user1.getId(), user2.getId());
        long b = Math.max(user1.getId(), user2.getId());
        return Objects.hash(a, b);
    }
}
