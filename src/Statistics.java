import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Statistics {
    private int totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;

    // Новые переменные для дополнения
    private HashSet<String> pagesVisited = new HashSet<>();
    private HashMap<String, Integer> osCountMap = new HashMap<>();
    private int totalEntries = 0;

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
    }

    public void addEntry(LogEntry entry) {
        // увеличиваем количество добавленных записей
        totalEntries++;

        // увеличиваем общий трафик
        this.totalTraffic += entry.getResponseSize();

        // обновляем minTime и maxTime
        LocalDateTime entryTime = entry.getDateTime();

        if (minTime == null || entryTime.isBefore(minTime)) {
            minTime = entryTime;
        }
        if (maxTime == null || entryTime.isAfter(maxTime)) {
            maxTime = entryTime;
        }

        // Проверяем код ответа
        if (entry.getResponseCode() == 200) {
            // добавляем адрес страницы в множество
            pagesVisited.add(entry.getRequestPath());
        }

        // Обновляем статистику ОС
        String os = entry.getUserAgent().getOperatingSystem().toString(); // предполагается, что есть такой метод
        osCountMap.put(os, osCountMap.getOrDefault(os, 0) + 1);
    }

    public double getTrafficRate() {
        if (minTime == null || maxTime == null || minTime.equals(maxTime)) {
            return 0.0;
        }
        Duration duration = Duration.between(minTime, maxTime);
        double hours = duration.toMinutes() / 60.0;
        if (hours == 0) return totalTraffic; // если всего один час или менее
        return totalTraffic / hours;
    }

    // Метод для получения списка всех посещенных страниц
    public List<String> getAllVisitedPages() {
        return new ArrayList<>(pagesVisited);
    }

    // Метод для получения статистики операционных систем с долями
    public HashMap<String, Double> getOSUsageStatistics() {
        HashMap<String, Double> osUsage = new HashMap<>();
        if (totalEntries == 0) {
            return osUsage; // пустой, если логов еще не добавлялось
        }
        for (String os : osCountMap.keySet()) {
            double ratio = (double) osCountMap.get(os) / totalEntries;
            osUsage.put(os, ratio);
        }
        return osUsage;
    }

}
