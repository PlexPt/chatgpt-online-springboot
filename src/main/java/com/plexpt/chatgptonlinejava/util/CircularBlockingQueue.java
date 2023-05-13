package com.plexpt.chatgptonlinejava.util;

import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by giant039 on 2017/3/17.
 * 阻塞环形队列 高并发
 *
 * @param <E>
 */
public class CircularBlockingQueue<E> extends CircularQueue<E> {
    /**
     * 对添加，删除，指针移动操作加锁
     */
    protected final ReentrantLock putLock = new ReentrantLock();

    private QueueListener listener;

    public CircularBlockingQueue() {
        super();
    }

    public CircularBlockingQueue(QueueListener listener) {
        super();
        this.listener = listener;
    }

    public void setListener(QueueListener listener) {
        this.listener = listener;
    }

    @Override
    public boolean add(E e) {
        final ReentrantLock putLock = this.putLock;
        try {
            putLock.lockInterruptibly();
            super.add(e);

            if (listener != null) {
                listener.afterAdd(e);
            }

            return true;
        } catch (InterruptedException exp) {
            exp.printStackTrace();
            return false;
        } finally {
            putLock.unlock();
        }

    }

    @Override
    public E next() {
        final ReentrantLock putLock = this.putLock;
        try {
            putLock.lockInterruptibly();
            return super.next();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            putLock.unlock();
        }

    }

    @Override
    public E prev() {
        final ReentrantLock putLock = this.putLock;
        try {
            putLock.lockInterruptibly();
            return super.prev();
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        } finally {
            putLock.unlock();
        }
    }

    @Override
    public boolean remove(E e) {
        final ReentrantLock putLock = this.putLock;
        try {
            putLock.lockInterruptibly();

            if (listener != null) {
                listener.afterAdd(e);
            }

            return super.remove(e);
        } catch (InterruptedException exp) {
            exp.printStackTrace();
            return false;
        } finally {
            putLock.unlock();
        }
    }


    /**
     * 监听器监听插入，删除，等操作之后需要实现的功能
     */
    interface QueueListener<E> {
        void afterAdd(E e);

        void afterRemove(E e);
    }

}
