package progressbar.cn.gzw.myapplication;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import progressbar.cn.gzw.myapplication.net.HttpUtil;

/**
 * Created by gzw on 16-5-2.
 */
public class Track {
    private static Track track;
    public static int WRITE_FILE = 1;
    public static int READ_FILE = 2;
    private  String mRootPath;
    private  File mManifestFile;
    private  String mPrefix;
    private  long mInterval;
    private  File currentFile;
    private  long duration = 604800000;
    private  int index = -1;
    private HandThread handThread;
    private List<FileData> files;

    public static Track getInstance(){
        if(track == null) track = new Track();
        return track;
    }
    private Track(){
    }
    public void startCheck(){
        Message msg = handThread.handler.obtainMessage();
        msg.what = READ_FILE;
        handThread.handler.sendMessageDelayed(msg,5*1000);
    }
    public void init(Config config){
        handThread = new HandThread();
        handThread.start();
        mPrefix = config.mPrefix;
        mInterval = config.mInterval;
        mRootPath = getmRootPath(config.context)+"/track/";
        File file = new File(mRootPath);
        if(!file.exists()) file.mkdir();
        mManifestFile = new File(mRootPath+"manifest.txt");
        if(!mManifestFile.exists()) try {
            mManifestFile.createNewFile();
            String currentFileName = mRootPath+mPrefix+System.currentTimeMillis()+".txt";
            currentFile = new File(currentFileName);
            if(!currentFile.exists()) currentFile.createNewFile();
            String buffer = currentFileName+","+0+","+System.currentTimeMillis()+"\n";
            writeToFile(buffer,mManifestFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public  void record(String tag,String arg0){
        String buffer = getJson(tag,arg0,null).toString()+"\n";
        writeToFile(buffer,currentFile);
    }
    public  void record(String tag,String arg0,String arg1){
        String buffer = getJson(tag,arg0,arg1).toString()+"\n";
        writeToFile(buffer,currentFile);
    }

    private  void writeToFile(String Content,File file){
        BufferedWriter out = null;
        try {
            if(file == null || !file.exists()) {
                file = new File(createFile().filePath);
                Log.d("detest","chuangjianwenjian");
            }
            FileWriter fw = new FileWriter(file, true);
            out = new BufferedWriter(fw);
            out.write(Content);
            out.flush();
            if(!file.getName().contains("manifest")&&isBig(file)){
                uploadToServer("signl");
            }
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
    //get all track file
    private  List<FileData> getAllFile(){
        List<FileData> files = new ArrayList<>();
        BufferedReader reader;
        try {
            FileReader fileReader = new FileReader(mManifestFile);
            reader = new BufferedReader(fileReader);
            String string;
            while((string = reader.readLine())!=null){
                String[] datas = string.split(",");
                files.add(new FileData(datas[0],Integer.valueOf(datas[1]),Long.valueOf(datas[2])));
            }
            return files;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private  void uploadToServer(String type){
        HttpUtil httpUtil = HttpUtil.getInstence();
        int count = files.size()-1;
        if(!type.equals("signl")){
            count = 0;
        }
        for(int i=files.size()-1;i>=count;i--){
            index = i;
            File tempFile = new File(files.get(i).filePath);
            if(tempFile.length()<=0)continue;
            if(!isCanUpload(files.get(i))){
                if(deleteFile(files.get(i).filePath)){
                    files.remove(i);
                    continue;
                }
            }else{
                final File file = new File(files.get(i).filePath);
                Map<String,File> fileMap = new HashMap<>();
                fileMap.put("file",file);
                httpUtil.uploadFile("http://192.168.0.120:8080/UploadFile/UploadFile", null, fileMap, new HttpUtil.Success() {
                    @Override
                    public void success(String response) {
                        if(deleteFile(files.get(index).filePath)){
                            files.remove(index);
                        }
                        if(index == 0){
                            resetManifest();
                        }
                    }
                }, new HttpUtil.Failure() {
                    @Override
                    public void failure(String error) {
                        files.get(index).repeatCount++;
                        if(index == 0) resetManifest();
                    }
                });
            }
        }
    }

    private  boolean isCanUpload(FileData fileData){
        long time = System.currentTimeMillis();
        if((time-fileData.time)>duration || fileData.repeatCount > 10){
            return false;
        }
        return true;
    }
    private  boolean deleteFile(String filePath){
        File file = new File(filePath);
        if(file.exists()) return file.delete();
        return false;
    }
    private  void resetManifest(){
        mManifestFile.delete();
        mManifestFile = new File(mRootPath+"manifest.txt");
        if(!mManifestFile.exists()){
            try {
                mManifestFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        for(int i=files.size()-1;i>=0;i--){
            String buffer = files.get(i).filePath+","+files.get(i).repeatCount+","+files.get(i).time+"\n";
            sendToHandler(buffer,2);
        }
    }
    private  JSONObject getJson(String tag,String arg0,String arg1){
        JSONObject json = new JSONObject();
        try {
            json.put("type",tag);
            json.put("activity",arg0);
            if(arg1!=null)
                json.put("weiget",arg1);
            return json;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }
    private String getmRootPath(Context context){
        if((Environment.MEDIA_MOUNTED.endsWith(Environment.getExternalStorageState()))||
                !Environment.isExternalStorageRemovable()&&null!=context.getExternalFilesDir(null)){
            return context.getExternalFilesDir(null).getPath();
        }else{
            return context.getFilesDir().getPath();
        }
    }
    private boolean isBig(File file){
        if(file.length() > 10*1024){
            return true;
        }
        return false;
    }
    private FileData createFile(){
        try{
            String currentFileName = mRootPath+mPrefix+System.currentTimeMillis()+".txt";
            currentFile = new File(currentFileName);
            if(!currentFile.exists()) currentFile.createNewFile();
            FileData fileData = new FileData(currentFileName,0,System.currentTimeMillis());
            mManifestFile = new File(mRootPath+"manifest.txt");
            if(!mManifestFile.exists()){
                mManifestFile.createNewFile();
            }
            String buffer = currentFileName+","+0+","+System.currentTimeMillis()+"\n";
            writeToFile(buffer,mManifestFile);
            return fileData;
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }
    class HandThread extends Thread{
        public Handler handler;
        public HandThread(){
            handler = new Handler(){
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if(msg.what == WRITE_FILE){
                        int type = msg.arg1;
                        File file;
                        if(type == 1){
                            file = currentFile;
                        }else{
                            file = mManifestFile;
                        }
                        String buffer = (String) msg.obj;
                        writeToFile(buffer,file);
                    }else if(msg.what == READ_FILE){
                        if(!isNetworkAvailable())return;
                        files = getAllFile();
                        uploadToServer("all");
                        Message msgs = handThread.handler.obtainMessage();
                        msgs.what = READ_FILE;
                        //Log.d("track","check");
                        handThread.handler.sendMessageDelayed(msgs,1000*300);
                    }
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
    public static boolean isNetworkAvailable(){
        Context context = MyApplication.getAppContext();
        // 获取手机所有连接管理对象（包括对wi-fi,net等连接的管理）
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager == null){
            return false;
        }else{
            // 获取NetworkInfo对象
            NetworkInfo[] networkInfo = connectivityManager.getAllNetworkInfo();

            if (networkInfo != null && networkInfo.length > 0){
                for (int i = 0; i < networkInfo.length; i++){
                    // 判断当前网络状态是否为连接状态
                    if (networkInfo[i].getState() == NetworkInfo.State.CONNECTED){
                        return true;
                    }
                }
            }
        }
        return false;
    }
    private void sendToHandler(String buffer,int type){
        Message msg = handThread.handler.obtainMessage();
        msg.what = WRITE_FILE;
        msg.arg1 = type;
        msg.obj = buffer;
        handThread.handler.sendMessage(msg);
    }
}
