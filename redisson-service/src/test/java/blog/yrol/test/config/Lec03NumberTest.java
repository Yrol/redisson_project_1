package blog.yrol.test.config;

import org.junit.jupiter.api.Test;
import org.redisson.api.RAtomicLongReactive;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * Working with integers.
 * Integers use RAtomicLongReactive instead of RBucketReactive (unlike in strings and objects)
 * **/
public class Lec03NumberTest extends BaseTest {

    @Test
    public void keyValueIncreaseTest() {
        RAtomicLongReactive atomicLong = this.client.getAtomicLong("user:1:visit");

        // Increment integer starting from 1 and up to 30 with a delay of 1 second (will take about 30 seconds to complete)
        Mono<Void> mono =  Flux.range(1,30)
                .delayElements(Duration.ofSeconds(1))
                .flatMap(i -> atomicLong.incrementAndGet())
                .then();

        StepVerifier.create(mono)
                .verifyComplete();

    }
}
