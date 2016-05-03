package progressbar.cn.gzw.myapplication;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        MyThread hand = new MyThread();
        MyThread.HandThread handler = hand.getHandThread();
        handler.start();
        handler.handler.postDelayed(null,1000*3);
    }
}
