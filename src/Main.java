import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        System.out.println("Введите первое число:");
        int number1 = new Scanner(System.in).nextInt();
        System.out.println("Введите второе число:");
        int number2 = new Scanner(System.in).nextInt();

        int sum = number1 + number2;
        int subtraction = number1 - number2;
        int multiplication = number1 * number2;
        double division = (double) number1 / number2;

        System.out.println("Сумма: " + sum);
        System.out.println("Разность: " + subtraction);
        System.out.println("Произведение: " + multiplication);
        System.out.println("Частное: " + division);
    }
}