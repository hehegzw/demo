package progressbar.cn.gzw.myapplication;

/**
 * Created by gzw on 16-5-2.
 */
public class FileData {
    public String filePath;
    public int repeatCount;
    public long time;

    public FileData(String filePath, int repeatCount, long time) {
        this.filePath = filePath;
        this.repeatCount = repeatCount;
        this.time = time;
    }
}
