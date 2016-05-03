package progressbar.cn.gzw.myapplication;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

/**
 * Created by gzw on 16-5-2.
 */
public class MyThread {
    private HandThread handThread;
    public HandThread getHandThread(){
        handThread = new HandThread();
        return handThread;
    }
    public MyThread(){
        handThread = new HandThread();
    }
    class HandThread extends Thread{
        public Handler handler;
        public HandThread(){
            handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    Log.d("hehe","heihei");
                }
            };
        }

        @Override
        public void run() {
            super.run();
            Looper.prepare();

            Looper.loop();
        }
    }
}
