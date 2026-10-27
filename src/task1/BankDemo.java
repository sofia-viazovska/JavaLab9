package task1;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Завдання 1: запускає багато (віртуальних) потоків, які одночасно переказують гроші між випадковими рахунками,
 * і перевіряє, що загальна сума грошей у банку не змінилася.
 */
public class BankDemo {

    static final int ACCOUNTS = 200;
    static final int THREADS = 5000;
    static final int MAX_INITIAL_BALANCE = 10_000;
    static final int MAX_TRANSFER = 5_000;

    public static void run() throws InterruptedException {
        Random random = new Random();
        Bank bank = new Bank();
        for (int i = 0; i < ACCOUNTS; i++) {
            bank.openAccount(100 + random.nextInt(MAX_INITIAL_BALANCE - 99));
        }
        List<Account> accounts = bank.getAccounts();

        long before = bank.totalBalance();
        System.out.println("Рахунків: " + ACCOUNTS + ", потоків: " + THREADS);
        System.out.println("Грошей у банку до переказів: " + before);

        CountDownLatch startSignal = new CountDownLatch(1);
        AtomicInteger done = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();
        List<Thread> threads = new ArrayList<>();

        for (int i = 0; i < THREADS; i++) {
            Thread thread = Thread.ofVirtual().unstarted(() -> {
                try {
                    startSignal.await();
                } catch (InterruptedException e) {
                    return;
                }
                ThreadLocalRandom r = ThreadLocalRandom.current();
                int a = r.nextInt(ACCOUNTS);
                int b = r.nextInt(ACCOUNTS - 1);
                if (b >= a) {
                    b++;
                }
                int amount = 1 + r.nextInt(MAX_TRANSFER);
                if (bank.transfer(accounts.get(a), accounts.get(b), amount)) {
                    done.incrementAndGet();
                } else {
                    rejected.incrementAndGet();
                }
            });
            threads.add(thread);
            thread.start();
        }

        startSignal.countDown();
        for (Thread thread : threads) {
            thread.join();
        }

        long after = bank.totalBalance();
        boolean negative = accounts.stream().anyMatch(a -> a.getBalance() < 0);
        System.out.println("Переказів виконано: " + done.get() + ", відхилено (не вистачало грошей): " + rejected.get());
        System.out.println("Грошей у банку після переказів: " + after);
        System.out.println("Суми збігаються: " + (before == after ? "так" : "НІ"));
        System.out.println("Від'ємних балансів немає: " + (negative ? "НІ" : "так"));
    }
}
