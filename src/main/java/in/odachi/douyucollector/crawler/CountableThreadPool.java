package in.odachi.douyucollector.crawler;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class CountableThreadPool {

    private int threadNum;

    private AtomicInteger threadAlive = new AtomicInteger();
    private ExecutorService invoker;

    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public CountableThreadPool(int threadNum) {
        this.threadNum = threadNum;
        this.invoker = Executors.newCachedThreadPool();
    }

    public int getThreadAlive() {
        return threadAlive.get();
    }

    public int getThreadNum() {
        return threadNum;
    }

    public void execute(final Runnable runnable) {
        if (threadAlive.get() >= threadNum) {
            try {
                lock.lock();
                while (threadAlive.get() >= threadNum) {
                    try {
                        condition.await();
                    } catch (InterruptedException ignored) {
                    }
                }
            } finally {
                lock.unlock();
            }
        }
        threadAlive.incrementAndGet();
        invoker.execute(() -> {
            try {
                runnable.run();
            } finally {
                try {
                    lock.lock();
                    threadAlive.decrementAndGet();
                    condition.signal();
                } finally {
                    lock.unlock();
                }
            }
        });
    }

    public boolean isShutdown() {
        return invoker.isShutdown();
    }

    public void shutdown() {
        invoker.shutdown();
    }
}