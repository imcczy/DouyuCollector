package in.odachi.douyucollector.common.util;

/**
 * 相关工具类
 */
public class NumberUtil {

    public static Integer parseInt(Object o) {
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o instanceof Double) {
            return ((Double) o).intValue();
        }
        try {
            return Integer.parseInt((String) o);
        } catch (RuntimeException e) {
            return Integer.MAX_VALUE;
        }
    }
}
