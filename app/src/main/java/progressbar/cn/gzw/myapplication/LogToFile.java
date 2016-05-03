package progressbar.cn.gzw.myapplication;

import android.os.Environment;
import android.util.Log;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by gzw on 16-5-2.
 */
public class LogToFile {
    public static final String ACTIVITY = "activity";
    public static final String WIDGET = "widget";
    private static final String LOG_FILE_NAME = "logcat.txt";
    private static ExecutorService es;
    private static File file;
    private static int fileSize = 2*1024;

    private static void init() {
        if (es == null) {
            es = Executors.newSingleThreadExecutor();
        }
        String filePath = filePath();
        if (!StringUtil.isEmpty()) {
            file = new File(filePath + "/" + LOG_FILE_NAME);
            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
    public static void setFileSize(int size){
        fileSize = size;
    }

    public static void log(String tag, String atyName) {
        if (!StringUtil.isEmpty(tag, atyName)) {
            init();
            String buffer = tag + "," + atyName+getData()+"\n";
            WriteFileThread wft = new WriteFileThread(buffer);
            wft.setName("logWriteToFile");
            es.execute(wft);
        }

    }

    public static void log(String tag, String atyName, String widget) {
        if (!StringUtil.isEmpty(tag, atyName, widget)) {
            init();
            String buffer = tag + "," + atyName + "," + widget+getData()+"\n";
            WriteFileThread wft = new WriteFileThread(buffer);
            wft.setName("logWriteToFile");
            es.execute(wft);
        }
    }

    private static void toFile(String data) {
        Log.d("hehe",file.length()+"");
        if(file.length() > fileSize){
            dleteContect();
        }
        BufferedWriter out = null;
        try {
            FileWriter fw = new FileWriter(file, true);
            out = new BufferedWriter(fw);
            out.write(data);
            out.flush();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private static void dleteContect() {
        BufferedReader reader = null;
        BufferedWriter writer = null;
        List<String> list = new ArrayList<>();
        try {
            FileReader fr = new FileReader(file);
            FileWriter fw = new FileWriter(file,true);
            reader = new BufferedReader(fr);
            writer = new BufferedWriter(fw);
            int line = 0;
            String string;
            while((string = reader.readLine())!=null){
                line++;
                if(line<100){
                    continue;
                }
                list.add(string);
            }
            FileWriter clear = new FileWriter(file,false);
            clear.write("");
            for(int i=0;i<list.size();i++){
                writer.write(list.get(i));
            }
            writer.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(reader!=null){
                try {
                    reader.close();
                    if(writer!=null)writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static String filePath() {
        boolean sdCardExit = Environment.getExternalStorageState().endsWith(Environment.MEDIA_MOUNTED);
        Log.d("hehe",Environment.getExternalStorageDirectory().getPath());
        if (sdCardExit) {
            Log.d("hehe",Environment.getExternalStorageDirectory().getPath());
            return Environment.getExternalStorageDirectory().getPath();
        } else {
            return null;
        }
    }

    private static class WriteFileThread extends Thread {
        private String data;

        public WriteFileThread(String data) {
            this.data = data;
        }

        @Override
        public void run() {
            toFile(data);
        }
    }
    private static String getData(){
        Date date = new Date(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }
}
