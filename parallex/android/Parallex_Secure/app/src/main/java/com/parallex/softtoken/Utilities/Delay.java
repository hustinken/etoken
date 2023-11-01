package com.parallex.softtoken.Utilities;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;

public class Delay {

    private boolean loop;
    public Delay(IDelay delay, final int ms){
        loop = false;
        IDelayListener = delay;
        final Handler handler = new Handler(Looper.getMainLooper(), new Handler.Callback() {
            @Override
            public boolean handleMessage(Message message) {
                IDelayListener.onDelayFinished();
                return true;
            }
        });

        new Thread(new Runnable() {
            @Override
            public void run() {
                do{
                    SystemClock.sleep(ms);
                    handler.sendEmptyMessage(0);
                }while (loop);
            }
        }).start();
    }

    public void loop(){
        loop = true;
    }

    public void stopLoop(){
        loop = false;
    }

    IDelay IDelayListener;
    public interface IDelay{
        void onDelayFinished();
    }
}
