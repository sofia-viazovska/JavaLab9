package task1;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class BankTest {

    @Test
    public void simpleTransferMovesMoney() {
        Bank bank = new Bank();
        Account a = bank.openAccount(100);
        Account b = bank.openAccount(50);
        assertTrue(bank.transfer(a, b, 30));
        assertEquals(70, a.getBalance());
        assertEquals(80, b.getBalance());
    }

    @Test
    public void transferIsRejectedWhenNotEnoughMoney() {
        Bank bank = new Bank();
        Account a = bank.openAccount(10);
        Account b = bank.openAccount(0);
        assertFalse(bank.transfer(a, b, 11));
        assertEquals(10, a.getBalance());
        assertEquals(0, b.getBalance());
        assertTrue(bank.transfer(a, b, 10));
        assertEquals(0, a.getBalance());
    }

    @Test
    public void invalidTransfersAreRejected() {
        Bank bank = new Bank();
        Account a = bank.openAccount(100);
        Account b = bank.openAccount(100);
        assertFalse(bank.transfer(a, a, 10));
        assertFalse(bank.transfer(a, b, 0));
        assertFalse(bank.transfer(a, b, -5));
        assertEquals(200, bank.totalBalance());
    }

    /** Класичний випадок deadlock: два потоки переказують у протилежних напрямках між тими самими рахунками. */
    @Test(timeout = 20_000)
    public void oppositeTransfersDoNotDeadlock() throws InterruptedException {
        Bank bank = new Bank();
        Account a = bank.openAccount(1_000_000);
        Account b = bank.openAccount(1_000_000);
        Thread t1 = new Thread(() -> {
            for (int i = 0; i < 200_000; i++) {
                bank.transfer(a, b, 1);
            }
        });
        Thread t2 = new Thread(() -> {
            for (int i = 0; i < 200_000; i++) {
                bank.transfer(b, a, 1);
            }
        });
        t1.start();
        t2.start();
        t1.join();
        t2.join();
        assertEquals(2_000_000, bank.totalBalance());
    }

    @Test(timeout = 60_000)
    public void totalIsPreservedUnderHeavyConcurrency() throws InterruptedException {
        Random random = new Random(5);
        Bank bank = new Bank();
        for (int i = 0; i < 50; i++) {
            bank.openAccount(random.nextInt(1000));
        }
        List<Account> accounts = bank.getAccounts();
        long before = bank.totalBalance();

        List<Thread> threads = new ArrayList<>();
        for (int t = 0; t < 2000; t++) {
            long seed = t;
            Thread thread = Thread.ofVirtual().unstarted(() -> {
                Random r = new Random(seed);
                for (int i = 0; i < 50; i++) {
                    int x = r.nextInt(accounts.size());
                    int y = r.nextInt(accounts.size());
                    bank.transfer(accounts.get(x), accounts.get(y), 1 + r.nextInt(500));
                }
            });
            threads.add(thread);
            thread.start();
        }
        for (Thread thread : threads) {
            thread.join();
        }

        assertEquals(before, bank.totalBalance());
        for (Account account : accounts) {
            assertTrue(account.getBalance() >= 0);
        }
    }
}
