package service;

import model.Duck;
import model.RaceEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.*;

public class RaceEventService {

    private final ExecutorService pool = Executors.newFixedThreadPool(4);

    private final Set<Long> subscribedDuckIds = ConcurrentHashMap.newKeySet();

    public CompletableFuture<List<String>> runRaceAsync(List<Duck> selectedDucks,
                                                        List<Double> distances) {
        return CompletableFuture.supplyAsync(() -> {
            if (selectedDucks == null || selectedDucks.isEmpty())
                throw new RuntimeException("No ducks selected");
            if (distances == null || distances.isEmpty())
                throw new RuntimeException("No distances");

            if (selectedDucks.size() != distances.size())
                throw new RuntimeException("Trebuie EXACT atâtea rațe câte distanțe. " +
                        "Selectate=" + selectedDucks.size() + ", distanțe=" + distances.size());

            RaceEvent race = new RaceEvent();
            race.setDistante(distances);
            race.clearParticipante();
            race.selecteazaParticipante(new ArrayList<>(selectedDucks));

            return race.simuleaza();
        }, pool);
    }

    public boolean subscribe(Duck d) {
        if (d == null || d.getId() == null) return false;
        return subscribedDuckIds.add(d.getId());
    }

    public boolean unsubscribe(Duck d) {
        if (d == null || d.getId() == null) return false;
        return subscribedDuckIds.remove(d.getId());
    }

    public boolean isSubscribed(Duck d) {
        if (d == null || d.getId() == null) return false;
        return subscribedDuckIds.contains(d.getId());
    }

    public List<Long> subscribedIds() {
        return new ArrayList<>(subscribedDuckIds);
    }

    public void shutdown() {
        pool.shutdownNow();
    }
}
