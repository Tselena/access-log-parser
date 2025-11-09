import java.io.File;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        int file_count = 0;
        while(true) {
            System.out.println("Укажите путь к файлу");
            String path = new Scanner(System.in).nextLine(); //запрашиваем путь к файлу

            File file = new File(path);
            boolean fileExists = file.exists(); //проверка, существует ли указнный файл
            boolean isDirectory = file.isDirectory(); //проверка, что путь ведет к файлу

            if (!fileExists || isDirectory) {
                System.out.println("Указанный файл не существует или указанный путь является путём к папке, а не к файлу");
                continue;
            }

            if (fileExists && !isDirectory) {
                System.out.println("Путь указан верно");
                file_count++;
                System.out.println("“Это файл номер № " + file_count);
            }
        }
    }
}