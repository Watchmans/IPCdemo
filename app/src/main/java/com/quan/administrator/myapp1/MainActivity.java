package com.quan.administrator.myapp1;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.quan.administrator.myapp1.Interface.Ikey;
import com.quan.administrator.myapp1.aidl.Book;

import com.quan.administrator.myapp1.aidl.BookManager;
import com.quan.administrator.myapp1.aidl.IOnNewBookArrivedListener;
import com.quan.administrator.myapp1.service.BookManagerService;
import com.quan.administrator.myapp1.service.MessengerService;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    @Bind(R.id.btn_send)
    Button btnSend;
    @Bind(R.id.btn_Messenager)
    Button btnMessenager;
    @Bind(R.id.btn_aidl)
    Button btnAidl;
    @Bind(R.id.tv_aidl)
    TextView tvAidl;
    private Messenger messenger;
    private MyServiceConnection myServiceConnection;
    private AidlServiceConnection aidlServiceConnection;
    private BookManager bookManager1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initData();
        initListenter();

    }

    private void initListenter() {
        btnSend.setOnClickListener(this);
        btnMessenager.setOnClickListener(this);
        btnAidl.setOnClickListener(this);
    }

    private void sendMessagerOnService() {
        myServiceConnection = new MyServiceConnection();
        Intent intent = new Intent(this, MessengerService.class);
        bindService(intent, myServiceConnection, Context.BIND_AUTO_CREATE);
    }

    class MyServiceConnection implements ServiceConnection {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            messenger = new Messenger(service);
            Message message = Message.obtain(null, Ikey.MEG_FROM_CLIENT);
            Bundle bundle = new Bundle();
            bundle.putString("msg", "hello service,I'm client");
            message.setData(bundle);
            //一定要注意这句，否则在服务端中msg.replyTo返回null的，会报空指针异常
            message.replyTo = replyMessenger;
            try {
                messenger.send(message);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        private class MessengerHandle extends Handler {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case Ikey.MEG_FROM_SERVICE:
                        Bundle bundle = msg.getData();
                        String result = bundle.getString("service");
                        Log.d("---msg:", "msg come from service result=" + result);
                }
                super.handleMessage(msg);
            }
        }

        Messenger replyMessenger = new Messenger(new MessengerHandle());


        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    private void sendDate() {
        int sum = 0;
        for (int i = 0; i < 1000; i++) {
            sum += i;
        }

        Bundle bundle = new Bundle();
        bundle.putInt("sum", sum);
        Intent intent = new Intent(MainActivity.this, SecondActivity.class);
        intent.putExtra("var", bundle);
        startActivity(intent);
    }

    private void initData() {
        SharedPreferences sp = getApplicationContext().getSharedPreferences("app1", MODE_PRIVATE);
        sp.edit().putString("app1Data", "这是一个应用MyApp1").commit();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_send:
                sendDate();
            case R.id.btn_Messenager:
                Toast.makeText(this, "66666", Toast.LENGTH_LONG).show();
                sendMessagerOnService();
            case R.id.btn_aidl:
                callAidl();
        }
    }

    private void callAidl() {
        aidlServiceConnection = new AidlServiceConnection();
        Intent intent = new Intent(this, BookManagerService.class);
        bindService(intent, aidlServiceConnection, Context.BIND_AUTO_CREATE);
    }


    /*********************************************************************************************************/

    class AidlServiceConnection implements ServiceConnection {

        /**
         * 服务中onServiceConnected和onServiceDisconnected都是运行在UI线程中的
         *
         * @param name
         * @param service
         */
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            bookManager1 = BookManager.Stub.asInterface(service);

            //如果调用服务端的方法时，该方法是耗时的，最好将它运行在非UI线程中
            //假如getBookList是一个耗时的方法
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                       final List<Book> list = bookManager1.getBookList();
                        Log.d("----", list.getClass().getCanonicalName());
                        Log.d("----", list.toString());
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                tvAidl.setText(list.toString());

                            }
                        });

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                }
            }).start();

            // **********************注册监听***************************
            try {
                bookManager1.registerBookArrivedListener(iOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }

/*
            try {
                List<Book> list = bookManager1.getBookList();
                Log.d("----", list.getClass().getCanonicalName());
                Log.d("----", list.toString());
                tvAidl.setText(list.toString());

                *//***********************注册监听****************************//*
                bookManager1.registerBookArrivedListener(iOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }*/

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }


    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case Ikey.MEG_NEW_BOOK_ARRIVED:
                    Log.d("quan", "receive new book :" + msg.obj);
                    break;
            }
            super.handleMessage(msg);
        }
    };

    IOnNewBookArrivedListener iOnNewBookArrivedListener = new IOnNewBookArrivedListener.Stub() {
        @Override
        public void onNewBookArrived(Book newBook) throws RemoteException {
            mHandler.obtainMessage(Ikey.MEG_NEW_BOOK_ARRIVED).getTarget();
        }

    };

    @Override
    protected void onDestroy() {
        if (bookManager1 != null && bookManager1.asBinder().isBinderAlive()) {
            try {
                bookManager1.unRegisterBookArrivedListener(iOnNewBookArrivedListener);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
            Log.d("quan", "unregiest listener:" + iOnNewBookArrivedListener);
        }
        if (myServiceConnection != null) {
            unbindService(myServiceConnection);
        }
        if (aidlServiceConnection != null) {
            unbindService(aidlServiceConnection);
        }
        super.onDestroy();
    }
}
