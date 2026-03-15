package network;

import factory.CardFactory;
import factory.EventFactory;
import factory.UserFactory;
import model.User;
import repository.*;
import repository.BDrepo.*;
import service.*;
import validator.*;

public class DuckSocialNetwork {

    private static DuckSocialNetwork instance;

    private UserRepository userRepo;
    private FriendshipRepository friendshipRepo;
    private MessageBDRepository messageRepo;
    private EventBDRepository eventRepo;
    private CardBDRepository cardRepo;

    private UserValidator userValidator;
    private DuckValidator duckValidator;
    private MessageValidator messageValidator;
    private EventValidation eventValidation;

    private UserFactory userFactory;
    private EventFactory eventFactory;
    private CardFactory cardFactory;

    private UserService userService;
    private FriendshipService friendshipService;
    private MessageService messageService;
    private EventService eventService;
    private CardService cardService;
    private FriendRequestBDRepository friendRequestRepo;
    private FriendRequestService friendRequestService;
    private RaceEventService raceEventService;



    public FriendRequestService getFriendRequestService() { return friendRequestService; }


    private User loggedUser;

    public User getLoggedUser() { return loggedUser; }
    public void setLoggedUser(User u) { this.loggedUser = u; }

    private DuckSocialNetwork() {
        userRepo = new UserBDRepository();
        friendshipRepo = new FriendshipBDRepository(userRepo);
        messageRepo = new MessageBDRepository(userRepo);
        eventRepo = new EventBDRepository(userRepo);
        cardRepo = new CardBDRepository();
        friendRequestRepo = new FriendRequestBDRepository(userRepo);


        userValidator = new UserValidator();
        duckValidator = new DuckValidator();
        messageValidator = new MessageValidator();
        eventValidation = new EventValidation();

        userFactory = new UserFactory();
        eventFactory = new EventFactory();
        cardFactory = new CardFactory();

        userService = new UserService(userRepo, userValidator, userFactory);
        friendshipService = new FriendshipService(userRepo, friendshipRepo);
        messageService = new MessageService(messageRepo, userRepo, messageValidator);
        eventService = new EventService(eventRepo, userRepo, eventValidation);
        cardService = new CardService(cardRepo, userRepo);
        friendRequestService = new FriendRequestService(friendRequestRepo, userRepo, friendshipService);
        raceEventService = new RaceEventService();


        userService.setFriendshipService(friendshipService);
    }

    public static DuckSocialNetwork getInstance() {
        if (instance == null) instance = new DuckSocialNetwork();
        return instance;
    }
    public RaceEventService getRaceEventService() { return raceEventService; }


    public UserRepository getUserRepository() { return userRepo; }
    public UserService getUserService() { return userService; }
    public FriendshipService getFriendshipService() { return friendshipService; }
    public MessageService getMessageService() { return messageService; }
    public EventService getEventService() { return eventService; }
    public CardService getCardService() { return cardService; }

    public UserFactory getUserFactory() { return userFactory; }
    public EventFactory getEventFactory() { return eventFactory; }
    public CardFactory getCardFactory() { return cardFactory; }
}
