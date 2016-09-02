package com.quan.administrator.myapp1.service;

import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.quan.administrator.myapp1.Interface.Ikey;

/**
 * Created by Administrator on 2016/9/1 0001 上午 8:13.
 */
public class MessengerService extends Service {
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return messenger.getBinder();
    }

    public class  MessengerHandle extends Handler{

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case Ikey.MEG_FROM_CLIENT:
                    Bundle data=msg.getData();
                    String message=data.getString("msg");
                    Log.d("-------msg","receive from client to message="+message);
//                  Toast.makeText(MessengerService.this,"receive from client to " + "message="+message,Toast.LENGTH_LONG).show();

                   //回复客户端
                    Messenger client = msg.replyTo;
//                    Log.d("----client", msg.replyTo +""+"   msg="+msg);
                    Message message2=Message.obtain(null,Ikey.MEG_FROM_SERVICE);
                    Bundle bundle=new Bundle();
                    bundle.putString("service","嗯嗯，我收到了");
                    message2.setData(bundle);
                    try {
                        client.send(message2);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
            }
            super.handleMessage(msg);
        }
    }

    Messenger messenger=new Messenger(new MessengerHandle());
}
