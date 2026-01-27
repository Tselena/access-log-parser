import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

public class LogEntry {
    private final String ipAddress;
    private final LocalDateTime dateTime;
    private final HttpMethod method;
    private final String requestPath;
    private final int responseCode;
    private final int responseSize;
    private final String referer;
    private final String userAgentString;
    private final UserAgent userAgent;


    //Getters
    public String getIpAddress() {
        return ipAddress;
    }
    public LocalDateTime getDateTime() {
        return dateTime;
    }
    public HttpMethod getMethod() {
        return method;
    }
    public String getRequestPath() {
        return requestPath;
    }
    public int getResponseCode() {
        return responseCode;
    }
    public int getResponseSize() {
        return responseSize;
    }
    public String getReferer() {
        return referer;
    }
    public String getUserAgentString() {
        return userAgentString;
    }
    public UserAgent getUserAgent() {
        return userAgent;
    }


    // Конструктор, принимающий строку лог-файла
    public LogEntry(String logLine) {
        // Разделение строки по кавычкам для выделения request и user-agent
        String[] parts = logLine.split("\"");
        if (parts.length < 5) {
            throw new IllegalArgumentException("Некорректная строка лог-файла");
        }

        // Парсим IP-адрес и дату/время + остальное
        String prePart = parts[0].trim(); // до первой кавычки
        String[] preTokens = prePart.split(" ");
        this.ipAddress = preTokens[0];

        // Парсим дату/время
        String dateStr = preTokens[preTokens.length -1];
        int dateStartIdx = logLine.indexOf('[') + 1;
        int dateEndIdx = logLine.indexOf(']');
        String dateTimeStr = logLine.substring(dateStartIdx, dateEndIdx);
        // Формат: 25/Sep/2022:06:25:50 +0300
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MMM/yyyy:HH:mm:ss Z");
        this.dateTime = LocalDateTime.parse(dateTimeStr, formatter);

        // Парсим request (часть внутри кавычек)
        String requestPart = parts[1]; // "METHOD /path HTTP/1.1"
        String[] requestElements = requestPart.trim().split(" ");
        String methodStr = requestElements[0];
        this.method = parseHttpMethod(methodStr);
        this.requestPath = requestElements[1];

        // Парсим ответ и размер
        String[] afterRequest = parts[2].trim().split(" ");
        int responseCode = -1;
        int responseSize = -1;
        try {
            responseCode = Integer.parseInt(afterRequest[0]);
        } catch (NumberFormatException e) { }
        try {
            responseSize = Integer.parseInt(afterRequest[1]);
        } catch (NumberFormatException e) { }
        this.responseCode = responseCode;
        this.responseSize = responseSize;

        // referer
        this.referer = (parts.length > 2) ? parts[3] : "-";

        // userAgent строка
        this.userAgentString = parts[parts.length - 1];

        // Создаём объект UserAgent
        this.userAgent = new UserAgent(this.userAgentString);
    }

    private HttpMethod parseHttpMethod(String methodStr) {
        try {
            return HttpMethod.valueOf(methodStr);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public boolean isBot() {
        if (userAgentString == null) {
            return false;
        }
        String userAgentLower = userAgentString.toLowerCase();

        // Список признаков ботов
        String[] botIndicators = {
                "bot", "googlebot", "yandexbot"
        };

        for (String indicator : botIndicators) {
            if (userAgentLower.contains(indicator)) {
                return true;
            }
        }
        return false;
    }


    @Override
    public String toString() {
        return "IP: " + ipAddress +
                "\nDateTime: " + dateTime +
                "\nMethod: " + method +
                "\nRequest Path: " + requestPath +
                "\nResponse Code: " + responseCode +
                "\nResponse Size: " + responseSize +
                "\nReferer: " + referer +
                "\nUser-Agent (browser): " + userAgent.getBrowser() +
                "\nUser-Agent (os type): " + userAgent.getOperatingSystem();
    }
}
