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
    private HashSet<String> pagesVisited = new HashSet<>();
    private HashMap<String, Integer> osCountMap = new HashMap<>();
    private HashSet<String> nonExistingPages = new HashSet<>();
    private HashMap<String, Integer> browserCountMap = new HashMap<>();
    private int totalEntries = 0;
    // Новые переменные
    private HashSet<String> uniqueUserIPs = new HashSet<>(); // уникальные IP реальных пользователей
    private int realUserEntries = 0; // обращений реальных пользователей (не боты)
    private int errorCount = 0; // количество ошибок (4xx или 5xx)

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
        int code = entry.getResponseCode();
        if (code == 200) {
            pagesVisited.add(entry.getRequestPath());
        }

        if (code == 404) {
            nonExistingPages.add(entry.getRequestPath());
        }

        // Обновляем статистику ОС
        String os = entry.getUserAgent().getOperatingSystem().toString();
        osCountMap.put(os, osCountMap.getOrDefault(os, 0) + 1);

        // Обновляем статистику браузеров
        String browser = entry.getUserAgent().getBrowser().toString();
        browserCountMap.put(browser, browserCountMap.getOrDefault(browser, 0) + 1);

        // Проверяем на бота и добавляем IP в множество уникальных пользователей
        if (entry.isBot()) {
            long currentSeconds = System.currentTimeMillis() / 1000;
            String ip = entry.getIpAddress();
            if (ip != null) {
                uniqueUserIPs.add(ip);
            }
            // Увеличиваем счетчик обращений реальных пользователей только для неботов
            realUserEntries++;
        }

        // Подсчитываем ошибки
        if (code >= 400 && code < 600) {
            errorCount++;
        }
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

    private double getTimePeriodInHours() {
        if (minTime == null || maxTime == null || minTime.equals(maxTime))
            return 0;
        Duration duration = Duration.between(minTime, maxTime);
        return duration.toMinutes() / 60.0;
    }

    // Метод для получения списка всех посещенных (существующих) страниц
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

    // Метод для получения всех несуществующих страниц
    public List<String> getNonExistingPages() {
        return new ArrayList<>(nonExistingPages);
    }

    // Метод для получения статистики браузеров (доля каждого)
    public HashMap<String, Double> getBrowserStatistics() {
        HashMap<String, Double> browserUsage = new HashMap<>();
        if (totalEntries == 0) {
            return browserUsage; // пустой, если логов еще не было
        }
        for (String browser : browserCountMap.keySet()) {
            double ratio = (double) browserCountMap.get(browser) / totalEntries;
            browserUsage.put(browser, ratio);
        }
        return browserUsage;
    }

    // Метод для расчета среднего числа посещений сайта за час по реальным пользователям
    public double getAverageVisitsPerHour() {
        double hours = getTimePeriodInHours();
        if (hours == 0) return 0;
        return (double) realUserEntries / hours;
    }

    // Метод для расчета среднего количества ошибочных запросов в час
    public double getAverageErrorsPerHour() {
        double hours = getTimePeriodInHours();
        if (hours == 0) return 0;
        return (double) errorCount / hours;
    }

    // Метод для расчета средней посещаемости одним пользователем (не ботом)
    public double getAverageVisitsPerUser() {
        double hours = getTimePeriodInHours();
        if (hours == 0 || uniqueUserIPs.isEmpty()) return 0;
        int totalRealVisits = realUserEntries; // число обращений реальных пользователей за весь период
        return (double) totalRealVisits / uniqueUserIPs.size();
    }

}
