import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.*;

public class Statistics {
    private int totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;
    private HashSet<String> pagesVisited = new HashSet<>();
    private HashMap<String, Integer> osCountMap = new HashMap<>();
    private HashSet<String> nonExistingPages = new HashSet<>();
    private HashMap<String, Integer> browserCountMap = new HashMap<>();
    private HashSet<String> uniqueUserIPs = new HashSet<>(); // уникальные IP реальных пользователей
    private int totalEntries = 0;
    private int realUserEntries = 0; // обращений реальных пользователей (не боты)
    private int errorCount = 0; // количество ошибок (4xx или 5xx)
    // Новые переменные
    private List<LogEntry> allEntries = new ArrayList<>(); // Хранить все лог-записи
    private Map<Long, Integer> visitsPerSecond = new HashMap<>(); // Посещение по секундам

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
    }

    public void addEntry(LogEntry entry) {
        // записываем лог
        allEntries.add(entry);

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

        // Проверяем на бота, учитываем посещение по времени и добавляем IP в множество уникальных пользователей
        if (!entry.isBot()) {
            long epochSecond = entry.getDateTime().toEpochSecond(ZoneOffset.UTC);
            // Обновляем количество посещений в текущую секунду
            visitsPerSecond.put(epochSecond, visitsPerSecond.getOrDefault(epochSecond, 0) + 1);

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

    // Метод для расчёта пиковой посещаемости сайта (в секунду)
    public int getPeakVisitsPerSecond() {
        Map<Long, Integer> visitsPerSecond = new HashMap<>();

        for (LogEntry entry : allEntries) {
            long timestamp = entry.getDateTime().getSecond();
            long secondKey = timestamp;

            visitsPerSecond.put(secondKey, visitsPerSecond.getOrDefault(timestamp, 0) + 1);
        }

        int maxCount = 0;
        for (int count : visitsPerSecond.values()) {
            if (count > maxCount) {
                maxCount = count;
            }
        }

        printVisitsPerSecond(visitsPerSecond);
        return maxCount;
    }

    // Вспомогательный метод для вывода содержимого Map<Long, Integer> visitsPerSecond
    public void printVisitsPerSecond(Map<Long, Integer> visitsPerSecond) {
        for (Map.Entry<Long, Integer> entry : visitsPerSecond.entrySet()) {
            System.out.println("Секунда: " + entry.getKey() + ", посещений: " + entry.getValue());
        }
    }

    // Метод, возвращающий список сайтов, со страниц которых есть ссылки на текущий сайт
    public Set<String> getRefererDomains() {
        Set<String> domains = new HashSet<>();
        for (LogEntry entry : allEntries) {
            String referer = entry.getReferer();
            if (referer != null && !referer.isEmpty()) {
                try {
                    // Декодируем реферер
                    String decodedRefferer = URLDecoder.decode(referer, StandardCharsets.UTF_8);

                    // Проверяем наличие протокола
                    if (!decodedRefferer.matches("^[a-zA-Z]+://.*")) {
                        decodedRefferer = "http://" + decodedRefferer;
                    }
                    URL url = new URL(decodedRefferer);
                    String host = url.getHost().trim();
                    if (!host.isEmpty()) {
                        domains.add(host);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return domains;
    }

    // Метод для расчёта максимальной посещаемости одним пользователем
    public int getMaxVisitsPerUser() {
        Map<String, Integer> userVisitCounts = new HashMap<>();
        for (LogEntry entry : allEntries) {
            if (!entry.isBot()) {
                String ip = entry.getIpAddress();
                if (ip != null) {
                    userVisitCounts.put(ip, userVisitCounts.getOrDefault(ip, 0) + 1);
                }
            }
        }
        int maxVisits = 0;
        for (int count : userVisitCounts.values()) {
            if (count > maxVisits) {
                maxVisits = count;
            }
        }
        return maxVisits;
    }

}
