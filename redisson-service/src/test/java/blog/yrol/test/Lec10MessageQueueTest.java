package blog.yrol.test;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBlockingDequeReactive;
import org.redisson.client.codec.LongCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.Duration;

/**
 * Demo for using Redis as a message queue.
 * Producer and Consumer concept
 * Execution Steps
 * 1. Execute consumer1
 * 2. Execute producer
 * 3. Execute consumer2
 * Outcome: consumer1 & consumer2 will start printing the values from messageQueue added by the producer
 */
public class Lec10MessageQueueTest extends BaseTest {

    private  RBlockingDequeReactive<Long> messageQueue;

    @BeforeAll
    public void setupQueue() {
        messageQueue = this.client.getBlockingDeque("message-queue", LongCodec.INSTANCE);
    }

    @Test
    public void consumer1() throws InterruptedException {
        messageQueue.takeElements()
                .doOnNext(i -> System.out.println("Consumer 1: " + i))
                .doOnError(System.out::println)
                .subscribe();

        sleep(6000000);
    }

    @Test
    public void consumer2() throws InterruptedException {
        messageQueue.takeElements()
                .doOnNext(i -> System.out.println("Consumer 2: " + i))
                .doOnError(System.out::println)
                .subscribe();

        sleep(6000000);
    }


    /***
     * Simulating Producer of adding items every 500ms
     * **/
    @Test
    public void producer() {
        Mono<Void> producer =  Flux.range(1,100000)
                .delayElements(Duration.ofMillis(500))
                .doOnNext(i -> System.out.println("producer adding:" + i))
                .flatMap(i ->this.messageQueue.add(Long.valueOf(i)))
                .then();

        StepVerifier.create(producer)
                .verifyComplete();
    }
}
