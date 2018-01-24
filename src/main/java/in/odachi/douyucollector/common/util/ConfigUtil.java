package in.odachi.douyucollector.common.util;

import in.odachi.douyucollector.common.constant.Constants;
import org.apache.commons.configuration2.Configuration;
import org.apache.commons.configuration2.builder.fluent.Configurations;
import org.apache.commons.configuration2.ex.ConfigurationException;

import java.io.File;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 通过文件读取配置项
 * 实时生效
 */
public class ConfigUtil {

    private static Configuration configuration() {
        try {
            Configurations configs = new Configurations();
            return configs.properties(new File(Constants.CONF_DOUYU_PROPERTIES));
        } catch (ConfigurationException e) {
            throw new RuntimeException(e);
        }
    }

    // Getter
    public static String getMysqlJdbcUrl() {
        return configuration().getString("douyu.mysql.jdbc.url");
    }

    public static String getMysqlUsername() {
        return configuration().getString("douyu.mysql.username");
    }

    public static String getMysqlPassword() {
        return configuration().getString("douyu.mysql.password");
    }

    public static int getFansMinimum() {
        return configuration().getInt("douyu.subscribe.minimum");
    }

    public static String getRedisAddress() {
        return configuration().getString("douyu.redis.address");
    }

    public static String getRedisPassword() {
        return configuration().getString("douyu.redis.password");
    }

    public static int getRedisDb() {
        return configuration().getInt("douyu.redis.database");
    }
    public static Set<Integer> getRoomSet(){
        return new HashSet<Integer>(Arrays.stream(configuration().
                getString("douyu.attention.rooms").
                split(",")).map(Integer::valueOf).collect(Collectors.toSet()));
    }
}
