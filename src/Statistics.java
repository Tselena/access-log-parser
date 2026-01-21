import java.time.Duration;
import java.time.LocalDateTime;

public class Statistics {
    private int totalTraffic;
    private LocalDateTime minTime;
    private LocalDateTime maxTime;

    public Statistics() {
        this.totalTraffic = 0;
        this.minTime = null;
        this.maxTime = null;
    }

    public void addEntry(LogEntry entry) {
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
}
