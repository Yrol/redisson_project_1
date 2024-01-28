package blog.yrol.test.config;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucketReactive;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class Lec01KeyValueTest extends BaseTest {

    @Test
    public void keyValueAccessTest() {

        // Getting the bucket string value using key
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name");

        // Setting value Sam to the key value (user:1:name)
        Mono<Void> set = bucket.set("Sam");

        // Getting the value from key value (user:1:name) and print
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(set.concatWith(get))
                .verifyComplete();
    }
}
