package service;

import model.FriendRequest;
import model.Enums.FriendRequestStatus;
import model.User;
import repository.BDrepo.FriendRequestBDRepository;
import repository.UserRepository;

import java.util.List;

public class FriendRequestService {

    private final FriendRequestBDRepository requestRepo;
    private final UserRepository userRepo;
    private final FriendshipService friendshipService;

    public FriendRequestService(FriendRequestBDRepository requestRepo,
                                UserRepository userRepo,
                                FriendshipService friendshipService) {
        this.requestRepo = requestRepo;
        this.userRepo = userRepo;
        this.friendshipService = friendshipService;
    }

    public FriendRequest sendRequest(Long fromId, Long toId) {
        if (fromId == null || toId == null) return null;
        if (fromId.equals(toId)) return null;

        User from = userRepo.findById(fromId);
        User to = userRepo.findById(toId);
        if (from == null || to == null) return null;

        return requestRepo.saveRequest(fromId, toId);
    }

    public List<FriendRequest> pendingFor(Long userId) {
        return requestRepo.findPendingForUser(userId);
    }

    public boolean reject(Long requestId, Long receiverId) {
        FriendRequest fr = requestRepo.findById(requestId);
        if (fr == null) return false;
        if (fr.getTo() == null || fr.getTo().getId() == null) return false;
        if (!fr.getTo().getId().equals(receiverId)) return false; // doar destinatarul poate decide
        if (fr.getStatus() != FriendRequestStatus.PENDING) return false;

        return requestRepo.updateStatus(requestId, FriendRequestStatus.REJECTED);
    }

    public boolean accept(Long requestId, Long receiverId) {
        FriendRequest fr = requestRepo.findById(requestId);
        if (fr == null) return false;
        if (fr.getTo() == null || fr.getTo().getId() == null) return false;
        if (!fr.getTo().getId().equals(receiverId)) return false;
        if (fr.getStatus() != FriendRequestStatus.PENDING) return false;

        Long a = fr.getFrom().getId();
        Long b = fr.getTo().getId();

        // 1) marchezi ACCEPTED
        boolean ok = requestRepo.updateStatus(requestId, FriendRequestStatus.ACCEPTED);
        if (!ok) return false;

        // 2) creezi prietenia finala in tabela friendships
        // (FriendshipBDRepository oricum are normalOrder; service-ul tau face addFriend)
        friendshipService.addFriend(a, b);

        return true;
    }



}
