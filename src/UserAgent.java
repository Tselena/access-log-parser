public class UserAgent {
    private final OperatingSystem os;
    private final Browser browser;

    public UserAgent(String userAgentStr) {
        this.os = parseOperatingSystem(userAgentStr);
        this.browser = parseBrowser(userAgentStr);
    }

    private OperatingSystem parseOperatingSystem(String userAgent) {
        String userAgentLower = userAgent.toLowerCase();
//        System.out.println("user-agent" + userAgentLower);
        if (userAgentLower.contains("windows")) return OperatingSystem.WINDOWS;
        if (userAgentLower.contains("macintosh") || userAgentLower.contains("mac os")) return OperatingSystem.MACOS;
        if (userAgentLower.contains("linux")) return OperatingSystem.LINUX;
        return OperatingSystem.UNKNOWN;
    }

    private Browser parseBrowser(String userAgent) {
        String userAgentLower = userAgent.toLowerCase();
        if (userAgentLower.contains("edge")) return Browser.EDGE;
        if (userAgentLower.contains("firefox")) return Browser.FIREFOX;
        if (userAgentLower.contains("chrome")) return Browser.CHROME;
        if (userAgentLower.contains("opera")) return Browser.OPERA;
        return Browser.OTHER;
    }

    // Геттеры
    public OperatingSystem getOperatingSystem() {
        return os;
    }
    public Browser getBrowser() {
        return browser;
    }
}
