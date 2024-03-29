package blog.yrol.test;

import blog.yrol.test.config.RedissonConfig;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.redisson.api.RedissonReactiveClient;

/**
 * The base test class that's being used by other test classes
 * **/
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class BaseTest {

    private final RedissonConfig redissonConfig = new RedissonConfig();
    protected RedissonReactiveClient client;

    @BeforeAll
    public void setClient() {
        this.client = this.redissonConfig.getRedissonReactiveClient();
    }

    @AfterAll
    public void shutdown() {
        this.client.shutdown();
    }

    protected void sleep(long millis) throws InterruptedException {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
