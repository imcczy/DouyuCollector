package in.odachi.douyucollector;


import java.util.HashSet;
import java.util.Set;

/**
 * Created by imcczy on 2018/1/29.
 */
public enum ListenedRoom {
    instance;
    private static Set<Integer> listened = new HashSet<>();
    public void addRoom(Integer roomid){
        listened.add(roomid);
    }
    public void delRoom(Integer roomid){
        listened.remove(roomid);
    }
    public boolean contians(Integer roomid){
        return listened.contains(roomid);

    }

    @Override
    public String toString() {
        return listened.toString();
    }
}
