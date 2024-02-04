package blog.yrol.test;

import blog.yrol.test.dto.Student;
import org.junit.jupiter.api.Test;
import org.redisson.api.RMapReactive;
import org.redisson.client.codec.StringCodec;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.Map;

/**
 * Working with Maps
 * **/
public class Lec06MapTest extends BaseTest {

    /**
     * Test for using a Redis map
     * **/
    @Test
    public void mapTest01() {
        RMapReactive<String, String> map = this.client.getMap("user:1", StringCodec.INSTANCE);
        Mono<String> name =  map.put("name", "Sam");
        Mono<String> age =  map.put("age", "23");
        Mono<String> city =  map.put("city", "New York");

        StepVerifier.create(name.concatWith(age).concatWith(city).then())
                .verifyComplete(); // then() will change it to Mono of void instead of return value
    }

    /**
     * Test for using a Java map with Redis Map
     * **/
    @Test
    public void mapTest02() {
        Map<String, String> javaMap =  Map.of(
                "name", "jake",
                "age", "30",
                "City", "London"
        );

        RMapReactive<String, String> map = this.client.getMap("user:2", StringCodec.INSTANCE);

        StepVerifier.create(map.putAll(javaMap).then())
                .verifyComplete(); // then() will change it to Mono of void instead of return value
    }

    /**
     * Test for using a Java map with different object types
     * **/
    @Test
    public void mapTest03() {

        TypedJsonJacksonCodec codec = new TypedJsonJacksonCodec(Integer.class, Student.class);

        RMapReactive<Integer, Student> map = this.client.getMap("users", codec);

        Student student1 = new Student("Sam", 10, "London", List.of(20, 30, 40));
        Student student2 = new Student("Paul", 12, "New York", List.of(25, 30, 15));

        Mono<Student> mono1 =  map.put(1, student1);
        Mono<Student> mono2 = map.put(2, student2);

        StepVerifier.create(mono1.concatWith(mono2).then())
                .verifyComplete();
    }
}
