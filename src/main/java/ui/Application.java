package ui;

import model.*;
import model.Enums.TipDuck;
import network.DuckSocialNetwork;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/** aplicatia principala */
public class Application {

    private static final Scanner SC = new Scanner(System.in);
    private static final RaceEvent RACE = new RaceEvent();

    public static void main(String[] args) {
        DuckSocialNetwork app = DuckSocialNetwork.getInstance();

        int cnt = app.getUserService().findAll().size();
        System.out.println("In baza de date exista " + cnt + " utilizatori.");

        while (true) {
            showMenu();
            String op = read("> ");
            try {
                switch (op) {
                    case "1"  -> addUser(app);
                    case "2"  -> removeUser(app);
                    case "3"  -> listUsers(app);
                    case "4"  -> addFriend(app);
                    case "5"  -> removeFriend(app);
                    case "6"  -> countCommunities(app);
                    case "7"  -> mostSociableCommunity(app);
                    case "8"  -> createCard(app);
                    case "9"  -> listCards(app);
                    case "10" -> addDuckToCard(app);
                    case "11" -> removeDuckFromCard(app);
                    case "12" -> deleteCard(app);
                    case "13" -> setRaceDistances();
                    case "14" -> simulateRace(app);
                    case "15" -> createEvent(app);
                    case "16" -> listEvents(app);
                    case "17" -> deleteEvent(app);
                    case "18" -> subscribeUserToEvent(app);
                    case "19" -> unsubscribeUserFromEvent(app);
                    case "20" -> notifyEventSubscribers(app);
                    case "21" -> sendMessage(app);

                    case "0", "q", "Q" -> {
                        System.out.println("Bye!");
                        return;
                    }
                    default -> System.out.println("Optiune invalida.");
                }
            } catch (Exception ex) {
                System.out.println("Eroare: " + ex.getMessage());
            }
        }
    }

    private static void showMenu() {
        System.out.println();
        System.out.println("=== DuckSocialNetwork ===");
        System.out.println("1) Add user");
        System.out.println("2) Remove user");
        System.out.println("3) List users");
        System.out.println("4) Add friend");
        System.out.println("5) Remove friend");
        System.out.println("6) Count communities");
        System.out.println("7) Most sociable community");
        System.out.println("8) Create card");
        System.out.println("9) List cards");
        System.out.println("10) Add duck to card");
        System.out.println("11) Remove duck from card");
        System.out.println("12) Delete card");
        System.out.println("13) Set race distances (d > 0)");
        System.out.println("14) Simulate race (sends report as message)");
        System.out.println("15) Create event");
        System.out.println("16) List events");
        System.out.println("17) Delete event");
        System.out.println("18) Subscribe user to event");
        System.out.println("19) Unsubscribe user from event");
        System.out.println("20) Notify event subscribers");
        System.out.println("21) Send message (manual)");
        System.out.println("0) Exit");
    }

    private static void addUser(DuckSocialNetwork app) {
        String username = read("username: ");
        String email = read("email: ");
        String password = read("password: ");
        String tip = read("Tip utilizator (person/duck): ").toLowerCase().trim();

        User created;
        if ("person".equals(tip)) {
            String nume = read("nume: ");
            String prenume = read("prenume: ");
            String dn = read("data nasterii (yyyy-mm-dd sau gol): ");
            LocalDate dataN = dn.isBlank() ? null : LocalDate.parse(dn);
            String ocupatie = read("ocupatie: ");
            double empatie = Double.parseDouble(read("nivelEmpatie: "));
            created = new Person(null, username, email, password, nume, prenume, dataN, ocupatie, empatie);
        } else if ("duck".equals(tip)) {
            TipDuck td = parseTipDuck(read("TipDuck (FLYING / SWIMMING): "));
            double v = Double.parseDouble(read("viteza: "));
            double r = Double.parseDouble(read("rezistenta: "));
            created = (td == TipDuck.FLYING)
                    ? new FlyingDuck(null, username, email, password, v, r)
                    : new SwimmingDuck(null, username, email, password, v, r);
        } else {
            System.out.println("Tip necunoscut.");
            return;
        }

        User res = app.getUserService().addUser(created);
        System.out.println(res != null ? "User adaugat." : "Eroare la adaugare.");
    }

    private static void removeUser(DuckSocialNetwork app) {
        Long id = Long.parseLong(read("id: "));
        app.getCardService().detachDuckFromAll(id);

        boolean ok = app.getUserService().removeUser(id);
        System.out.println(ok ? "User sters." : "Nu exista user cu acest id.");
    }

    private static void listUsers(DuckSocialNetwork app) {
        List<User> all = app.getUserService().findAll();
        if (all.isEmpty()) {
            System.out.println("(fara utilizatori)");
            return;
        }
        for (User u : all)
            System.out.println(u.getId() + " | " + u.getUsername() + " | " + u.getClass().getSimpleName());
    }

    private static void addFriend(DuckSocialNetwork app) {
        Long a = Long.parseLong(read("id user 1: "));
        Long b = Long.parseLong(read("id user 2: "));
        boolean ok = app.getFriendshipService().addFriend(a, b);
        System.out.println(ok ? "Prietenie adaugata." : "Eroare la adaugare.");
    }

    private static void removeFriend(DuckSocialNetwork app) {
        Long a = Long.parseLong(read("id user 1: "));
        Long b = Long.parseLong(read("id user 2: "));
        boolean ok = app.getFriendshipService().removeFriend(a, b);
        System.out.println(ok ? "Prietenie stearsa." : "Eroare la stergere.");
    }

    private static void countCommunities(DuckSocialNetwork app) {
        int cnt = app.getFriendshipService().countCommunities();
        System.out.println("Comunitati: " + cnt);
    }

    private static void mostSociableCommunity(DuckSocialNetwork app) {
        List<User> best = app.getFriendshipService().mostSociableCommunity();
        if (best.isEmpty()) {
            System.out.println("Nu exista comunitati.");
            return;
        }
        for (User x : best) System.out.println(" - " + x.getUsername());
    }

    private static void createCard(DuckSocialNetwork app) {
        String nume = read("Nume card: ");
        Card c = app.getCardService().createCard(nume);
        System.out.println("Card creat cu id = " + c.getId());
    }

    private static void listCards(DuckSocialNetwork app) {
        var list = app.getCardService().listCards();
        if (list.isEmpty()) {
            System.out.println("(fara carduri)");
            return;
        }
        for (Card c : list)
            System.out.println(c.getId() + " | " + c.getNumeCard() + " | membri: " + c.getMembri().size());
    }

    private static void addDuckToCard(DuckSocialNetwork app) {
        Long cardId = Long.parseLong(read("id card: "));
        Long duckId = Long.parseLong(read("id duck: "));
        boolean ok = app.getCardService().addDuck(cardId, duckId);
        System.out.println(ok ? "Rata adaugata in card." : "Eroare la adaugare.");
    }

    private static void removeDuckFromCard(DuckSocialNetwork app) {
        Long cardId = Long.parseLong(read("id card: "));
        Long duckId = Long.parseLong(read("id duck: "));
        boolean ok = app.getCardService().removeDuck(cardId, duckId);
        System.out.println(ok ? "Rata eliminata din card." : "Eroare la stergere.");
    }

    private static void deleteCard(DuckSocialNetwork app) {
        Long cardId = Long.parseLong(read("id card de sters: "));
        boolean ok = app.getCardService().deleteCard(cardId);
        System.out.println(ok ? "Card sters." : "Nu s-a putut sterge cardul.");
    }

    private static void setRaceDistances() {
        String line = read("Distante (d > 0): ");
        var ds = new ArrayList<Double>();
        for (String tok : line.trim().split("\\s+")) {
            try {
                double val = Double.parseDouble(tok);
                if (val > 0) ds.add(val);
            } catch (Exception ignored) {}
        }
        if (ds.isEmpty()) {
            System.out.println("Eroare: toate distantele sunt invalide (<= 0).");
            return;
        }
        RACE.setDistante(ds);
        System.out.println("Setate " + ds.size() + " distante.");
    }

    /**
     * Simuleaza cursa si trimite raportul ca UN SINGUR mesaj catre toate ratele participante:
     * from = o persoana aleasa, to = lista de rate participante.
     */
    private static void simulateRace(DuckSocialNetwork app) {
        Event e = pickEventOrNull(app);
        if (e == null) return;

        var subscribedUsers = app.getEventService().listSubscribedUsers(e);
        if (subscribedUsers.isEmpty()) {
            System.out.println("Nu exista utilizatori abonati la acest event.");
            return;
        }

        var ducks = new ArrayList<Duck>();
        for (User u : subscribedUsers) {
            if (u instanceof Duck d) ducks.add(d);
        }
        if (ducks.isEmpty()) {
            System.out.println("La acest event nu este abonata nicio rata.");
            return;
        }

        RACE.selecteazaParticipante(ducks);
        RACE.selecteazaParticipante(ducks);

        int M = RACE.getDistante().size();
        int p = RACE.getParticipante().size();

        if (M == 0) {
            System.out.println("Nu sunt distante setate pentru cursa.");
            return;
        }
        if (p < M) {
            System.out.println("Nu sunt suficiente rate selectate pentru numarul de distante");
            System.out.println("Necesare: " + M + ", selectate: " + p);
            return;
        }

        var raport = RACE.simuleaza();
        System.out.println("--- Simulare cursa pentru event (id=" + e.getId() + ") ---");
        for (String line : raport) System.out.println(line);

        StringBuilder sb = new StringBuilder();
        for (String line : raport) sb.append(line).append("\n");
        String content = sb.toString();

        // alegem sender: doar PERSON
        var allUsers = app.getUserService().findAll();
        var possibleSenders = new ArrayList<User>();
        for (User u : allUsers) {
            if (!(u instanceof Duck)) possibleSenders.add(u);
        }
        if (possibleSenders.isEmpty()) {
            System.out.println("Nu exista niciun utilizator de tip PERSON care sa poata trimite mesajul.");
            return;
        }

        System.out.println("Posibili senderi (doar persoane):");
        for (User u : possibleSenders) {
            System.out.println(" - id=" + u.getId() + " | " + u.getUsername());
        }

        Long senderId;
        try {
            senderId = Long.parseLong(read("Alege id-ul sender-ului: "));
        } catch (NumberFormatException ex) {
            System.out.println("Id invalid (nu este numar).");
            return;
        }

        User sender = null;
        for (User u : possibleSenders) {
            if (u.getId() != null && u.getId().equals(senderId)) {
                sender = u;
                break;
            }
        }
        if (sender == null) {
            System.out.println("Nu exista persoana cu id-ul dat.");
            return;
        }

        // destinatari: toate ratele participante
        var destinatari = new ArrayList<User>();
        for (Duck d : RACE.getParticipante()) destinatari.add(d);

        Message m = new Message(
                null,
                sender,
                destinatari,
                content,
                LocalDateTime.now()
        );

        Message saved = app.getMessageService().send(m);
        if (saved == null) {
            System.out.println("Nu s-a putut trimite mesajul cu raportul cursei.");
        } else {
            System.out.println("Raportul cursei a fost trimis catre toate ratele participante.");
        }
    }

    private static void createEvent(DuckSocialNetwork app) {
        var e = new Event();
        boolean ok = app.getEventService().createEvent(e);
        System.out.println(ok ? "Event creat cu id = " + e.getId() : "Eroare la creare event.");
    }

    private static void listEvents(DuckSocialNetwork app) {
        var events = app.getEventService().findAll();
        if (events.isEmpty()) {
            System.out.println("(fara evenimente)");
            return;
        }
        System.out.println("Evenimente existente:");
        for (Event e : events) System.out.println(" - id=" + e.getId());
    }

    private static Event pickEventOrNull(DuckSocialNetwork app) {
        var events = app.getEventService().findAll();
        if (events.isEmpty()) {
            System.out.println("(fara evenimente)");
            return null;
        }

        System.out.println("Evenimente disponibile:");
        for (Event e : events) System.out.println(" - id=" + e.getId());

        Long id;
        try {
            id = Long.parseLong(read("Id eveniment: "));
        } catch (NumberFormatException ex) {
            System.out.println("Id invalid (nu este numar).");
            return null;
        }

        for (Event e : events) {
            if (e.getId() != null && e.getId().equals(id)) return e;
        }

        System.out.println("Nu exista event cu id = " + id);
        return null;
    }

    private static void deleteEvent(DuckSocialNetwork app) {
        Event e = pickEventOrNull(app);
        if (e == null) return;
        boolean ok = app.getEventService().deleteEvent(e);
        System.out.println(ok ? "Event sters." : "Nu s-a putut sterge eventul.");
    }

    private static void subscribeUserToEvent(DuckSocialNetwork app) {
        Event e = pickEventOrNull(app);
        if (e == null) return;

        Long userId = Long.parseLong(read("Id user de abonat: "));
        boolean ok = app.getEventService().subscribe(e, userId);
        System.out.println(ok ? "User abonat la event." : "Eroare la abonare (user sau event invalid).");
    }

    private static void unsubscribeUserFromEvent(DuckSocialNetwork app) {
        Event e = pickEventOrNull(app);
        if (e == null) return;

        Long userId = Long.parseLong(read("Id user de dezabonat: "));
        boolean ok = app.getEventService().unsubscribe(e, userId);
        System.out.println(ok ? "User dezabonat de la event." : "Eroare la dezabonare.");
    }

    private static void notifyEventSubscribers(DuckSocialNetwork app) {
        Event e = pickEventOrNull(app);
        if (e == null) return;

        String msg = read("Mesaj de notificare: ");
        app.getEventService().notifyAll(e, msg);
        System.out.println("Notificare trimisa abonatilor.");
    }

    /**
     * Trimitere manuala: sender -> un singur receiver,
     * dar conform cerintei, receiver-ul este pus in List.of(receiver)
     */
    private static void sendMessage(DuckSocialNetwork app) {
        Long senderId = Long.parseLong(read("Sender id: "));
        Long receiverId = Long.parseLong(read("Receiver id: "));
        String content = read("Message: ");

        User sender = findUserById(app, senderId);
        User receiver = findUserById(app, receiverId);

        if (sender == null || receiver == null) {
            System.out.println("Sender sau receiver invalid.");
            return;
        }

        Message m = new Message(
                null,
                sender,
                List.of(receiver),
                content,
                LocalDateTime.now()
        );

        Message saved = app.getMessageService().send(m);
        System.out.println(saved != null ? "Message sent." : "Error sending message.");
    }

    private static User findUserById(DuckSocialNetwork app, Long id) {
        for (User u : app.getUserService().findAll()) {
            if (id.equals(u.getId())) return u;
        }
        return null;
    }

    private static String read(String msg) {
        System.out.print(msg);
        return SC.nextLine().trim();
    }

    private static TipDuck parseTipDuck(String s) {
        String t = s == null ? "" : s.trim().toUpperCase();
        return switch (t) {
            case "FLYING" -> TipDuck.FLYING;
            default -> TipDuck.SWIMMING;
        };
    }
}
