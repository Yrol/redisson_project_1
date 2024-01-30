package blog.yrol.test.config;

import blog.yrol.test.dto.Student;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucketReactive;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.Arrays;

public class Lec02KeyValueObjectTest extends BaseTest {


    /**
     * Test for serialising and storing and object
     * new TypedJsonJacksonCodec - will convert object to JSON when storing as well as hide class dto info
     * **/
    @Test
    public void keyValueObjectTest() {

        Student student = new Student("Sam", 23, "Atlanta", Arrays.asList(50, 60, 80));
        RBucketReactive<Student> bucket = this.client.getBucket("student:1", new TypedJsonJacksonCodec(Student.class));
        Mono<Void> set = bucket.set(student);
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        // Compare the objects
        StepVerifier.create(set.concatWith(get))
                .verifyComplete();
    }
}
