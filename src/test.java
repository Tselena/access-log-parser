import java.io.File;
import java.util.Scanner;

public class test {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int validFileCount = 0; // Счётчик верно указанных файлов

        while (true) {
            System.out.print("Введите путь к файлу: ");
            String path = scanner.nextLine();

            File file = new File(path);
            boolean fileExists = file.exists();
            boolean isFile = file.isFile();

            if (!fileExists || !isFile) {
                System.out.println("Файл не существует или указанный путь является путём к папке.");
                continue;
            } else {
                validFileCount++;
                System.out.println("Путь указан верно");
                System.out.println("Это файл номер " + validFileCount);
            }
        }
    }
}

