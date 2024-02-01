package blog.yrol.test.config;

import org.junit.jupiter.api.Test;
import org.redisson.api.DeletedObjectListener;
import org.redisson.api.ExpiredObjectListener;
import org.redisson.api.RBucketReactive;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.concurrent.TimeUnit;

/**
 * Test for evaluating the addListener expire and delete events
 * Make sure to enable notifications events in Redis server before running the test by running the command:  config set notify-keyspace-events AKE
 * **/
public class Lec05EventListenerTest extends BaseTest {

    @Test
    public void expiredEventTest() throws InterruptedException {
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name", StringCodec.INSTANCE);

        // Making the TTL for 3 seconds
        Mono<Void> set = bucket.set("Sam",3, TimeUnit.SECONDS);

        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        // Event lister that listens for the expired keys. Will return only the key not the value
        Mono<Void> event = bucket.addListener(new ExpiredObjectListener() {
            @Override
            public void onExpired(String s) {
                System.out.println("Expired : " + s);
            }
        }).then();

        // sleeping for 5 seconds
        sleep(5000);

        // Evaluating the events in order (the addListener (expiration) event will occur at last)
        StepVerifier.create(set.concatWith(get).concatWith(event))
                .verifyComplete();
    }

    /**
     * Delete events addListener
     * Make sure to execute "del user:1:name" in Redis to execute the results
     * */
    @Test
    public void deletedEventTest() throws InterruptedException {
        RBucketReactive<String> bucket = this.client.getBucket("user:1:name", StringCodec.INSTANCE);

        // Making the TTL for 3 seconds
        Mono<Void> set = bucket.set("Sam");

        Mono<Void> get = bucket.get()
                .doOnNext(System.out::println)
                .then();

        // Event lister that listens for the deleted keys. Will return only the key not the value
        Mono<Void> event = bucket.addListener(new DeletedObjectListener() {
            @Override
            public void onDeleted(String s) {
                System.out.println("Deleted : " + s);
            }
        }).then();


        // Extend expiry time in 60 seconds after sleeping for 5 seconds
        sleep(60000);


        // Evaluating the events in order (the addListener (delete) event will occur at last)
        StepVerifier.create(set.concatWith(get).concatWith(event))
                .verifyComplete();
    }
}
