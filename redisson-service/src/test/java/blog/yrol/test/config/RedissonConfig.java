package blog.yrol.test.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.api.RedissonReactiveClient;
import org.redisson.config.Config;

import java.util.Objects;

public class RedissonConfig {

    private RedissonClient redissonClient;


    /**
     * Making connection to teh Redis
     * **/
    public RedissonClient getClient() {
        if(Objects.isNull(this.redissonClient)) {
            Config config = new Config();
            config.useSingleServer()
                    .setAddress("redis://127.0.0.1:6379");
            redissonClient = Redisson.create(config);
        }

        return redissonClient;
    }

    /**
     * Getting connection Redisson reactive client
     * **/
    public RedissonReactiveClient getRedissonReactiveClient() {
        return getClient().reactive();
    }
}
