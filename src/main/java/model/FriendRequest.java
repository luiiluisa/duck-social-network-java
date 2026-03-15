package model;

import model.Enums.FriendRequestStatus;

import java.time.LocalDateTime;

public class FriendRequest {
    private Long id;
    private User from;
    private User to;
    private FriendRequestStatus status;
    private LocalDateTime createdAt;

    public FriendRequest(Long id, User from, User to, FriendRequestStatus status, LocalDateTime createdAt) {
        this.id = id;
        this.from = from;
        this.to = to;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public User getFrom() { return from; }
    public User getTo() { return to; }
    public FriendRequestStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setStatus(FriendRequestStatus status) { this.status = status; }
}
