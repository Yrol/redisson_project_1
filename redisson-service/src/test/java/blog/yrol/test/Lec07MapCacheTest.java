package blog.yrol.test;

import blog.yrol.test.dto.Student;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMapCacheReactive;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Working with Map caches that allow to expire / remove certain objects from the map
 * **/
public class Lec07MapCacheTest extends BaseTest {

    @Test
    public void mapCacheTest() throws InterruptedException {

        TypedJsonJacksonCodec codec = new TypedJsonJacksonCodec(Integer.class, Student.class);

        // creating the Map
        RMapCacheReactive<Integer, Student> mapCache =  this.client.getMapCache("users:cache", codec);

        Student student1 = new Student("Sam", 10, "London", List.of(20, 30, 40));
        Student student2 = new Student("Paul", 12, "New York", List.of(25, 30, 15));

        // Setting the expiration to 5 and 10 seconds for the student objects
        Mono<Student> s1 = mapCache.put(1, student1, 5, TimeUnit.SECONDS);
        Mono<Student> s2 = mapCache.put(2, student2, 10, TimeUnit.SECONDS);

        StepVerifier.create(s1.then(s2).then())
                .verifyComplete();

        sleep(3000);

        // Access students after 3 seconds
        mapCache.get(1).doOnNext(System.out::println).subscribe(); // will be still available / printed
        mapCache.get(2).doOnNext(System.out::println).subscribe(); // will be still available / printed

        // access students after 6 seconds (after another 3 seconds)
        sleep(3000);
        mapCache.get(1).doOnNext(System.out::println).subscribe(); // won't be available / printed
        mapCache.get(2).doOnNext(System.out::println).subscribe(); //  will be still available / printed

    }
}
