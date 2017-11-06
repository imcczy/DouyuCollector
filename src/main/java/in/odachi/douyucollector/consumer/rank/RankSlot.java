package in.odachi.douyucollector.consumer.rank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 滑动窗口，每1分钟一格
 */
class RankSlot {

    private final List<Map<Integer, Double>> slots;

    // 当前索引下标
    private int index = 0;

    /**
     * 初始化必须提供大小
     * 每1分钟一格，如果统计最近5分钟，则size=5，以此类推
     */
    RankSlot(int size) {
        this.slots = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            this.slots.add(new HashMap<>());
        }
    }

    /**
     * 将数据放入Slot
     */
    void putIntoSlots(Map<Integer, Double> data) {
        slots.set(index, data);
        if (++index >= slots.size()) {
            index = 0;
        }
    }

    /**
     * 查询的结果集
     */
    Map<Integer, Double> querySlotSum() {
        return slots.stream()
                .flatMap(m -> m.entrySet().stream())
                .collect(Collectors.groupingBy(Map.Entry::getKey, Collectors.summingDouble(Map.Entry::getValue)));
    }
}
