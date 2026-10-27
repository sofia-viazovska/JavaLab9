package task1;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Банк: зберігає рахунки та вміє безпечно переказувати гроші між ними з багатьох потоків.
 */
public class Bank {

    private final List<Account> accounts = new ArrayList<>();
    private int nextId = 0;

    /** Відкриває новий рахунок із початковою сумою. Викликається до запуску потоків. */
    public Account openAccount(long initialBalance) {
        Account account = new Account(nextId++, initialBalance);
        accounts.add(account);
        return account;
    }

    public List<Account> getAccounts() {
        return Collections.unmodifiableList(accounts);
    }

    /**
     * Переказує amount грошей з рахунку from на рахунок to як одну неподільну операцію.
     * Повертає true, якщо переказ виконано, і false, якщо його відхилено
     * (сума не додатна, рахунки збігаються або на рахунку from не вистачає грошей).
     * <p>
     * Щоб не було deadlock, рахунки завжди блокуються в одному порядку: спочатку той,
     * в кого менший номер, потім той, в кого більший.
     */
    public boolean transfer(Account from, Account to, int amount) {
        if (amount <= 0 || from == to) {
            return false;
        }

        Account first = from.getId() < to.getId() ? from : to;
        Account second = (first == from) ? to : from;

        synchronized (first) {
            synchronized (second) {
                if (from.getBalance() < amount) {
                    return false;
                }
                from.withdraw(amount);
                to.deposit(amount);
                return true;
            }
        }
    }

    /** Сума грошей на всіх рахунках. Точне значення дає лише тоді, коли перекази не виконуються. */
    public long totalBalance() {
        long total = 0;
        for (Account account : accounts) {
            total += account.getBalance();
        }
        return total;
    }
}
