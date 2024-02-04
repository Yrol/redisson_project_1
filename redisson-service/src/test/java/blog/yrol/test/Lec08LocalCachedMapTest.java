package blog.yrol.test;

import blog.yrol.test.config.RedissonConfig;
import blog.yrol.test.dto.Student;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RedissonClient;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.util.List;

/**
 * Testing for the local cache (app cache)
 * Steps to simulate local cache via test cases
 * 1. Run appServer1 test case - this will print the student details on sleep mode
 * 2. Run appServer2 test case - this will change student 1 record and will result in printing the updated data in appServer1
 * 3. Bring down the redis service (docker) - the app will keep printing the last updated data - this will prove the data is being invoked from local and not from Redis server
 * **/
public class Lec08LocalCachedMapTest extends BaseTest {

    private RLocalCachedMap<Integer, Student> studentsMap;


    /**
     * Configuration for the local cache
     * **/
    @BeforeAll
    public void setupClient() {
        RedissonConfig config = new RedissonConfig();
        RedissonClient redissonClient = config.getClient();

        /**
         * Configuration for local cache. Ex: TTL for local cache & etc
         * LocalCachedMapOptions.SyncStrategy.UPDATE - make sure the servers will be updated as soon as the same local cache of another server is updated
         * LocalCachedMapOptions.ReconnectionStrategy.NONE -
         * **/
        LocalCachedMapOptions<Integer, Student> localCachedMapOptions =  LocalCachedMapOptions.<Integer, Student>defaults()
                        .syncStrategy(LocalCachedMapOptions.SyncStrategy.UPDATE)
                                .reconnectionStrategy(LocalCachedMapOptions.ReconnectionStrategy.NONE);

        // Creating a local cached map (a hashmap called "students")
        studentsMap = redissonClient.getLocalCachedMap(
                "students",
                new TypedJsonJacksonCodec(Integer.class, Student.class),
                localCachedMapOptions
        );
    }

    @Test
    public void appServer1() throws InterruptedException {

        // creating students and adding them to the local cache
        Student student1 = new Student("Sam", 10, "London", List.of(20, 30, 40));
        Student student2 = new Student("Paul", 12, "New York", List.of(25, 30, 15));
        this.studentsMap.put(1, student1);
        this.studentsMap.put(2, student2);

        // Print student 1 after every one second
        Flux.interval(Duration.ofSeconds(1))
                .doOnNext(i -> System.out.println(i + " ==> " + studentsMap.get(1)))
                .subscribe();

        sleep(6000000);
    }


    /**
     * Test case for simulating the update (run after executing the test case - appServer1 )
     * **/
    @Test
    public void appServer2() throws InterruptedException {

        // creating students and adding them to the local cache
        Student student1 = new Student("Sam updated", 13, "Sydney", List.of(20, 30, 40));
        this.studentsMap.put(1, student1);

    }
}
