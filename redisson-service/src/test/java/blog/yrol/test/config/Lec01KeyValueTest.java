package blog.yrol.test.config;

import org.junit.jupiter.api.Test;
import org.redisson.api.RBucketReactive;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

public class Lec01KeyValueTest extends BaseTest {

    /**
     * Test for setting and retrieving key value
     * **/
    @Test
    public void keyValueAccessTest() {

        // Getting the bucket string value using key. Using StringCodec.INSTANCE to make sure the value is stored as a string instead of encoded by default.
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name", StringCodec.INSTANCE);

        // Setting value Sam to the key value (user:1:name)
        Mono<Void> set = bucket.set("Sam");

        // Getting the value from key value (user:1:name) and print
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(set.concatWith(get))
                .verifyComplete();
    }


    /**
     * Test for expiring KeyValue
     * **/
    @Test
    public void keyValueExpiryTest() {
        // Getting the bucket string value using key. Using StringCodec.INSTANCE to make sure the value is stored as a string instead of encoded by default.
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name", StringCodec.INSTANCE);

        // Setting value Sam to the key value (user:1:name)
        Mono<Void> set = bucket.set("Sam",10, TimeUnit.SECONDS);

        // Getting the value from key value (user:1:name) and print
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(set.concatWith(get))
                .verifyComplete();
    }

    /**
     * Test for expiring KeyValue and then extending it
     * **/
    @Test
    public void keyValueExtendExpiryTest() throws InterruptedException {
        // Getting the bucket string value using key. Using StringCodec.INSTANCE to make sure the value is stored as a string instead of encoded by default.
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name", StringCodec.INSTANCE);

        // Setting value Sam to the key value (user:1:name)
        Mono<Void> set = bucket.set("Sam",10, TimeUnit.SECONDS);

        // Getting the value from key value (user:1:name) and print
        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(set.concatWith(get))
                .verifyComplete();

        // Extend expiry time in 60 seconds after sleeping for 5 seconds
        sleep(5000);
        Mono<Boolean> mono =  bucket.expire(60, TimeUnit.SECONDS);
        StepVerifier.create(mono)
                .expectNext(true)
                .verifyComplete();

        // Access expiration time
        Mono<Void> timeToLive =  bucket.remainTimeToLive()
                .doOnNext(System.out::println)
                .then();

        // With the StepVerifier API, we can define our expectations of published elements in terms of what elements we expect and what happens when our stream completes.
        // https://www.baeldung.com/reactive-streams-step-verifier-test-publisher
        StepVerifier.create(timeToLive)
                .verifyComplete();
    }
}
