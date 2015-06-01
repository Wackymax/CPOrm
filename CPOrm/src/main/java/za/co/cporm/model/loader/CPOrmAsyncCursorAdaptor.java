package za.co.cporm.model.loader;

import android.content.Context;
import android.database.Cursor;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import za.co.cporm.model.util.CPOrmCursor;
import za.co.cporm.util.CPOrmLog;

import java.lang.ref.SoftReference;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hennie.brink on 2015-05-30.
 */
public abstract class CPOrmAsyncCursorAdaptor<T, K> extends CPOrmCursorAdaptor<T, K> {

    private final Lock lock = new ReentrantLock();
    private final Condition condition = lock.newCondition();
    private final Queue<T> loaderQueue = new LinkedList<T>();
    private final LoaderThreadHandler threadHandler = new LoaderThreadHandler(this);
    private LoaderThread loaderThread;

    public CPOrmAsyncCursorAdaptor(Context context, int layoutId) {

        super(context, layoutId);
    }

    public CPOrmAsyncCursorAdaptor(Context context, Cursor c, int layoutId) {

        super(context, c, layoutId);
    }

    public CPOrmAsyncCursorAdaptor(Context context, Cursor c, int layoutId, int flags) {

        super(context, c, layoutId, flags);
    }

    @Override
    public void changeCursor(Cursor cursor) {

        dispose();
        super.changeCursor(cursor);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        startLoaderThreadIfStopped();
        lock.lock();
        T inflate = ((CPOrmCursor<T>) getCursor()).inflate();
        if(!loaderQueue.contains(inflate))
            loaderQueue.offer(inflate);
        condition.signal();
        lock.unlock();
        super.bindView(view, context, cursor);
    }

    private void dispose(){

        lock.lock();
        if(loaderThread != null) {
            loaderThread.isCancelled = true;
            loaderThread = null;
        }
        condition.signal();
        lock.unlock();
    }

    private void startLoaderThreadIfStopped() {

        if(loaderThread == null) {
            loaderThread = new LoaderThread();
            loaderThread.start();
        }
    }

    public abstract boolean loadAsyncInformation(T information);

    private class LoaderThread extends Thread {

        volatile boolean isCancelled = false;
        CPOrmCursor<T> cursor = (CPOrmCursor<T>) getCursor();

        @Override
        public void run() {

            setName(CPOrmAsyncCursorAdaptor.class.getSimpleName() + "_Loader");
            try {
                while (isStillValid()) {

                    lock.lock();
                    while (loaderQueue.peek() == null && isStillValid()) {
                        condition.await();
                    }

                    if (!isStillValid()) {
                        lock.unlock();
                        break;
                    }

                    T item = loaderQueue.poll();
                    lock.unlock();
                    if(loadAsyncInformation(item))
                        threadHandler.sendEmptyMessage(0);
                }
            } catch (InterruptedException e) {
                CPOrmLog.e("Failed while waiting for items", e);
            }
        }

        private boolean isStillValid() {

            return cursor != null && !cursor.isClosed() && !isCancelled;
        }
    }

    private static class LoaderThreadHandler extends Handler {

        private final SoftReference<CPOrmCursorAdaptor> adaptorReference;

        LoaderThreadHandler(CPOrmCursorAdaptor adaptor) {

            this.adaptorReference = new SoftReference<CPOrmCursorAdaptor>(adaptor);
        }

        @Override
        public void handleMessage(Message msg) {

            super.handleMessage(msg);

            CPOrmCursorAdaptor adaptor = adaptorReference.get();
            if(adaptor != null) adaptor.notifyDataSetChanged();
        }
    }
}
