package service;

import model.Card;
import model.Duck;
import model.User;
import repository.BDrepo.CardBDRepository;
import repository.UserRepository;

import java.util.List;

public class CardService {

    private CardBDRepository cardRepository;
    private UserRepository userRepository;

    public CardService(CardBDRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
    }

    public Card createCard(String numeCard) {
        Card c = new Card(numeCard);
        return cardRepository.save(c);
    }

    public boolean deleteCard(Long id) {
        return cardRepository.delete(id);
    }

    public List<Card> listCards() {
        return cardRepository.findAll();
    }

    public boolean addDuck(Long cardId, Long duckUserId) {
        Card card = findCard(cardId);
        if (card == null) {
            System.out.println("Nu exista card cu id-ul dat.");
            return false;
        }

        Duck d = getDuck(duckUserId);
        if (d == null) {
            System.out.println("Nu exista rata cu id-ul dat sau userul nu este Duck.");
            return false;
        }

        return cardRepository.addDuckToCard(cardId, d);
    }

    public boolean removeDuck(Long cardId, Long duckUserId) {
        Card card = findCard(cardId);
        if (card == null) {
            System.out.println("Nu exista card cu id-ul dat.");
            return false;
        }

        Duck d = getDuck(duckUserId);
        if (d == null) {
            System.out.println("Nu exista rata cu id-ul dat sau userul nu este Duck");
            return false;
        }

        return cardRepository.removeDuckFromCard(cardId, d);
    }

    public double averagePerformance(Long cardId) {
        Card c = findCard(cardId);
        if (c == null) return 0.0;
        return c.getPerformantaMedie();
    }

    private Duck getDuck(Long id) {
        if (id == null) return null;
        for (User u : userRepository.findAll()) {
            if (id.equals(u.getId()) && u instanceof Duck) return (Duck) u;
        }
        return null;
    }

    public int detachDuckFromAll(Long duckUserId) {
        return cardRepository.removeDuckEverywhereById(duckUserId);
    }

    private Card findCard(Long id) {
        if (id == null) return null;
        return cardRepository.findById(id);
    }
}
