package task1;

/**
 * Банківський рахунок. Усі методи synchronized, тобто працюють під замком самого об'єкта рахунку.
 * Баланс ніколи не може стати від'ємним: withdraw відмовляє, якщо грошей не вистачає.
 */
public class Account {

    private final int id;
    private long balance;

    public Account(int id, long balance) {
        this.id = id;
        this.balance = balance;
    }

    /** Унікальний номер рахунку. За ним Bank визначає порядок блокування. */
    public int getId() {
        return id;
    }

    public synchronized long getBalance() {
        return balance;
    }

    public synchronized void deposit(long amount) {
        balance += amount;
    }

    public synchronized void withdraw(long amount) {
        if (amount > balance) {
            throw new IllegalStateException("На рахунку " + id + " недостатньо грошей");
        }
        balance -= amount;
    }
}
