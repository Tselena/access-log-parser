import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

public class Main {
    public static void main(String[] args) {
        int file_count = 0;
        Scanner scanner = new Scanner(System.in);

        Statistics stats = new Statistics();

        while (true) {
            System.out.println("Укажите путь к файлу");
            String path = scanner.nextLine(); //запрашиваем путь к файлу

            File file = new File(path);
            boolean fileExists = file.exists(); //проверка, существует ли указанный файл
            boolean isDirectory = file.isDirectory(); //проверка, что путь ведет к файлу

            if (!fileExists || isDirectory) {
                System.out.println("Указанный файл не существует или указанный путь является путём к папке, а не к файлу");
                continue;
            }

            if (fileExists && !isDirectory) {
                System.out.println("Путь указан верно");
                file_count++;
                System.out.println("Это файл номер № " + file_count);
            }

            // Объявляем переменные для подсчета
            int totalLines = 0;
            int yandexCount = 0;
            int googleCount = 0;

            // Построчно читаем указанный файл
            try {
                FileReader fileReader = new FileReader(path);
                BufferedReader reader =
                        new BufferedReader(fileReader);
                String line;
                while ((line = reader.readLine()) != null) {
                    int length = line.length();

                    // Проверка длины строки
                    if (length > 1024) {
                        throw new LineTooLongException("Строка номер " + (totalLines + 1) + " длиной " + length + " символов превышает 1024 символа");
                    }

                    totalLines++;

                    String[] parts = line.split("\"");
//                    System.out.println(Arrays.toString(parts));

                    if (parts.length >= 6) {
                        // Согласно описанию составляющих, User-Agent - последний элемент в массиве
                        String userAgent = parts[parts.length - 1];
                        // System.out.println(userAgent);

                        String userAgentLower = userAgent.toLowerCase();
                        if (userAgentLower.contains("googlebot")) {
                            googleCount++;
                        }
                        if (userAgentLower.contains("yandexbot")) {
                            yandexCount++;
                        }
                    }

                    LogEntry entry = new LogEntry(line);
//                    System.out.println(entry);
//                    System.out.println("---------");
//                    System.out.println("ipAddress " + entry.getIpAddress());
//                    System.out.println("dateTime " + entry.getDateTime());
//                    System.out.println("method " + entry.getMethod());
//                    System.out.println("requestPath " + entry.getRequestPath());
//                    System.out.println("responseCode " + entry.getResponseCode());
//                    System.out.println("responseSize " + entry.getResponseSize());
//                    System.out.println("referer " + entry.getReferer());
                    stats.addEntry(entry);
                }
                reader.close();
            } catch (LineTooLongException e) {
                e.printStackTrace();
                break; // Завершение программы при превышении лимита
            } catch (Exception ex) {
                ex.printStackTrace();
                break; // Завершение программы при других ошибках
            }

            // вывод статистики
            System.out.println("\n=== Статистика ===");
            System.out.println("Общее количество строк в файле: " + totalLines);
            System.out.println();

            double yandexShare = totalLines > 0 ? (double) yandexCount / totalLines : 0;
            double googleShare = totalLines > 0 ? (double) googleCount / totalLines : 0;
            System.out.println("Доля YandexBot: " + Math.round(yandexShare * 100 * 100.00) / 100.00 + "%");
            System.out.println("Доля Googlebot: " + Math.round(googleShare * 100 * 100.00) / 100.00 + "%");
            System.out.println();

            System.out.println("Средний объём трафика сайта за час: " + stats.getTrafficRate() + " байт/час" + "\n");

            // Получение списка всех посещенных (существующих) страниц
            List<String> existingPages = stats.getAllVisitedPages();
//            System.out.println("Все посещенные страницы: " + existingPages + "\n");
//
            List<String> nonExistingPages = stats.getNonExistingPages();
//            System.out.println("Все несуществующие страницы: " + nonExistingPages + "\n");

            // Получение статистики ОС
            HashMap<String, Double> osStats = stats.getOSUsageStatistics();
            System.out.println("Доля операционных систем:");
            System.out.println(osStats + "\n");

            // Получение статистики браузеров
            HashMap<String, Double> browsersStats = stats.getBrowserStatistics();
            System.out.println("Доля браузеров:");
            System.out.println(browsersStats + "\n");

            // Получение статистики по посещаемости
            System.out.println("Среднее число посещений в час (реальные пользователи): " + stats.getAverageVisitsPerHour());
            System.out.println("Среднее количество ошибочных запросов в час: " + stats.getAverageErrorsPerHour());
            System.out.println("Средняя посещаемость одним пользователем (не ботом): " + stats.getAverageVisitsPerUser() + "\n");

            System.out.println("Максимальная посещаемость за одну секунду (не ботом): " + stats.getPeakVisitsPerSecond());
            System.out.println("Список доменов сайтов, со страниц которых есть ссылки: " + stats.getRefererDomains());
            System.out.println("Максимальное число визитов одним пользователем: " + stats.getMaxVisitsPerUser());

            // Останавливаемся после обработки
            break;

        }
        scanner.close();
    }
}