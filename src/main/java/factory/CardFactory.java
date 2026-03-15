package factory;

import model.Card;

public class CardFactory {
    public Card create(String name) {
        return new Card(name);
    }
}
