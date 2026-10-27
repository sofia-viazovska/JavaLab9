package task2;

import java.util.ArrayList;
import java.util.List;

/**
 * Завдання 2: конвеєр із двох кільцевих буферів.
 * П'ять потоків генерують рядки в перший буфер, два потоки переписують їх у другий
 * з іншим текстом, а головний потік забирає й друкує задану кількість рядків із другого буфера.
 */
public class Pipeline {

    static final int BUFFER_CAPACITY = 10;
    static final int GENERATORS = 5;
    static final int TRANSLATORS = 2;

    /** Запускає конвеєр, друкує count повідомлень і повертає їх списком. */
    public static List<String> run(int count) throws InterruptedException {
        RingBuffer<String> generated = new RingBuffer<>(BUFFER_CAPACITY);
        RingBuffer<String> translated = new RingBuffer<>(BUFFER_CAPACITY);

        for (int i = 1; i <= GENERATORS; i++) {
            startGenerator(i, generated);
        }
        for (int i = GENERATORS + 1; i <= GENERATORS + TRANSLATORS; i++) {
            startTranslator(i, generated, translated);
        }

        List<String> result = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            String message = translated.take();
            result.add(message);
            System.out.println(i + ". " + message);
        }
        return result;
    }

    private static void startGenerator(int id, RingBuffer<String> out) {
        startDaemon("generator-" + id, () -> {
            try {
                for (int n = 1; ; n++) {
                    out.put("Потік № " + id + " згенерував повідомлення " + n);
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    private static void startTranslator(int id, RingBuffer<String> in, RingBuffer<String> out) {
        startDaemon("translator-" + id, () -> {
            try {
                while (true) {
                    String message = in.take();
                    out.put("Потік № " + id + " переклав повідомлення «" + message + "»");
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });
    }

    /** Створює потік-демон і запускає його: такі потоки не заважають програмі завершитися. */
    private static void startDaemon(String name, Runnable body) {
        Thread thread = new Thread(body, name);
        thread.setDaemon(true);
        thread.start();
    }
}
