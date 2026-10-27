import task1.BankDemo;
import task2.Pipeline;

public class Main {

    static final int MESSAGES = 100;

    public static void main(String[] args) throws InterruptedException {
        System.out.println("=== Завдання 1. Переказ коштів ===");
        BankDemo.run();

        System.out.println("\n=== Завдання 2. Виробник і споживач ===");
        Pipeline.run(MESSAGES);
    }
}
