package com.quan.administrator.myapp1.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.RemoteCallbackList;
import android.os.RemoteException;
import android.os.SystemClock;
import android.support.annotation.Nullable;
import android.util.Log;

import com.quan.administrator.myapp1.aidl.Book;
import com.quan.administrator.myapp1.aidl.BookManager;
import com.quan.administrator.myapp1.aidl.IOnNewBookArrivedListener;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Administrator on 2016/9/1 0001 下午 3:56.
 * 这是一个ipc的demo
 */
public class BookManagerService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return myBinder;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        copyOnWriteArrayList.add(new Book(1,"数据结构"));
        copyOnWriteArrayList.add(new Book(2,"高数"));
        new Thread(new ServiceWorker()).start();
    }

    /**
     * 采用CopyOnWriteArrayList,这个CopyOnWriteArrayList支持并发读写，
     * 在aidl方法是在服务端的Binder线程池中执行的，因此多个客户端同时连接的时候，
     * 会存在多个线程同时访问的情使用情形，所以我们要在aidl方法中处理线程同步，
     * 而我们直接使用CopyOnWriteArrayList来进行自动的线程同步。注意的一点是他不是继承ArrayList
     */
    private CopyOnWriteArrayList<Book> copyOnWriteArrayList=new CopyOnWriteArrayList<Book>();
//    private CopyOnWriteArrayList<IOnNewBookArrivedListener> bookListenerList=new
//            CopyOnWriteArrayList<IOnNewBookArrivedListener>();
    //用RemoteCallbackList对象来代替CopyOnWriteArrayList，系统专门提供用于删除跨进程listener接口的
    private RemoteCallbackList<IOnNewBookArrivedListener> bookListenerList=new
        RemoteCallbackList<IOnNewBookArrivedListener>();
    private AtomicBoolean isServiceDestoryed=new AtomicBoolean(false);

    private Binder myBinder= new BookManager.Stub() {



        @Override
        public List<Book> getBookList() throws RemoteException {
            //模拟耗时
            SystemClock.sleep(5000);
            return copyOnWriteArrayList;
        }

        @Override
        public void addBook(Book book) throws RemoteException {
            copyOnWriteArrayList.add(book);
        }

        @Override
        public void registerBookArrivedListener(IOnNewBookArrivedListener listener) throws RemoteException {
           /* if(!bookListenerList.contains(listener)){
                bookListenerList.add(listener);
                Log.d("quan","add listener{"+listener+"} success!");
            }else{
                Log.d("quan","already exist");
            }
            Log.d("quan","bookListenerList.size()="+bookListenerList.size());*/
            bookListenerList.register(listener);
            Log.d("quan","add listener{"+listener+"} success!");
        }

        @Override
        public void unRegisterBookArrivedListener(IOnNewBookArrivedListener listener) throws RemoteException {
            /*if(bookListenerList.contains(listener)){
                bookListenerList.remove(listener);
                Log.d("quan","unRegister listener{"+listener+"} success!");
            }else{
                Log.d("quan","no found listener!");
            }
//            Log.d("quan","bookListenerList.size()="+bookListenerList.size());*/
            bookListenerList.unregister(listener);
            Log.d("quan","unRegister listener{"+listener+"} success!");
        }
    };

    private void onNewBookArrived(Book book) throws RemoteException {
        copyOnWriteArrayList.add(book);

       final int N=bookListenerList.beginBroadcast();

        Log.d("quan","copyOnWriteArrayList.size()="+copyOnWriteArrayList.size());

        /**
         * 遍列bookListenerList时不要用List的方式遍列，RemoteCallbackList不是List,要用
         * bookListenerList.beginBroadcast()和bookListenerList.finishBroadcast();配对一起使用
         * 哪怕是仅仅获取bookListenerList中的元素
         */
        for(int i=0;i<N   /*bookListenerList.size()*/;i++){
//            IOnNewBookArrivedListener listener=bookListenerList.get(i);
            IOnNewBookArrivedListener listener=bookListenerList.getBroadcastItem(i);
//            Log.d("quan","listener=="+listener);
//            listener.onNewBookArrived(book);

            if(listener!=null){
                listener.onNewBookArrived(book);
            }
        }
        bookListenerList.finishBroadcast();
    }

    class ServiceWorker implements Runnable{
        @Override
        public void run() {
            while (!isServiceDestoryed.get()){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int bookId=copyOnWriteArrayList.size()+1;
                Book book=new Book(bookId,"new book is #"+bookId);
                try {
                    onNewBookArrived(book);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onDestroy() {
        isServiceDestoryed.set(true);
        super.onDestroy();
    }
}
