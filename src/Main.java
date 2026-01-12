import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int file_count = 0;
        Scanner scanner = new Scanner(System.in);

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
            int maxLength = Integer.MIN_VALUE;
            int minLength = Integer.MAX_VALUE;

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
                    // Обновляем длину самой длинной и короткой строки
                    if (length > maxLength) {
                        maxLength = length;
                    }
                    if (length < minLength) {
                        minLength = length;
                    }
                }
                reader.close();
            } catch (LineTooLongException e) {
                e.printStackTrace();
                break; // Завершение программы при превышении лимита
            } catch (Exception ex) {
                ex.printStackTrace();
                break; // Завершение программы при других ошибках
            }

            // Итоговые выводы
            System.out.println("Общее количество строк в файле: " + totalLines);
            System.out.println("Длина самой длинной строки: " + maxLength);
            System.out.println("Длина самой короткой строки: " + minLength);

            // Останавливаемся после обработки
            break;

        }
        scanner.close();
    }
}