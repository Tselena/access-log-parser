import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Scanner;

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
                    System.out.println(Arrays.toString(parts));

                    if (parts.length >= 6) {
                        // Согласно описанию составляющих, User-Agent - последний элемент в массиве
                        String userAgent = parts[parts.length - 1];
//                        System.out.println(userAgent);

                        String userAgentLower = userAgent.toLowerCase();
                        if (userAgentLower.contains("googlebot")) {
                            googleCount++;
                        }
                        if (userAgentLower.contains("yandexbot")) {
                            yandexCount++;
                        }
                    }

                    LogEntry entry = new LogEntry(line);
                    System.out.println(entry);
                    System.out.println("---------");
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

            System.out.println("Average traffic per hour: " + stats.getTrafficRate() + " bytes/hour");

            // Останавливаемся после обработки
            break;

        }
        scanner.close();
    }
}