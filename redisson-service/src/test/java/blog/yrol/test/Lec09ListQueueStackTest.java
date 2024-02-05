package blog.yrol.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.RDequeReactive;
import org.redisson.api.RListReactive;
import org.redisson.api.RQueueReactive;
import org.redisson.client.codec.LongCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Testing lists, queues and deque (double ended queues)
 * **/
public class Lec09ListQueueStackTest extends BaseTest {

    @Test
    public void listTest01() {

        // Removing the list if already exist
        StepVerifier.create(this.client.getList("number-instance-list").delete().then())
                .verifyComplete();

        RListReactive<Long> list = this.client.getList("number-instance-list", LongCodec.INSTANCE);

        // Creating a list of type Long (creating publisher) and add on by one to Redis
        Mono<Void> listAdd  = Flux.range(1, 10)
                .map(Long::valueOf)
                .flatMap(list::add)
                .then();

        StepVerifier.create(listAdd)
                        .verifyComplete();

        StepVerifier.create(list.size())
                .expectNext(10)
                .verifyComplete();
    }

    @Test
    public void listTest02() {
        RListReactive<Long> list = this.client.getList("number-instance-streams", LongCodec.INSTANCE);

        // Creating a stream to add list items at once (as opposed to adding one by one above)
        List<Long> longList = LongStream.rangeClosed(1, 10)
                .boxed()
                .collect(Collectors.toList());

        // Adding the stream list to the RListReactive
        StepVerifier.create(list.addAll(longList).then())
                .verifyComplete();

        StepVerifier.create(list.size())
                .expectNext(10)
                .verifyComplete();
    }


    /**
     * Working with queues
     * **/
    @Test
    public void queueTest() {

        // Remove queue if already exist
        StepVerifier.create(this.client.getQueue("number-instance-queue").delete().then())
                .verifyComplete();

        RQueueReactive<Long> queue = this.client.getQueue("number-instance-queue", LongCodec.INSTANCE);

        Mono<Void> queueAdd = Flux.range(1, 10)
                .map(Long::valueOf)
                .flatMap(queue::add)
                .then();

        StepVerifier.create(queueAdd)
                .verifyComplete();

        // Removing items from the queue from beginning and repeat that for 3 times. Ex:
        Mono<Void> queuePoll =  queue.poll().repeat(3)
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(queuePoll)
                .verifyComplete();

        StepVerifier.create(queue.size())
                .expectNext(6)
                .verifyComplete();
    }

    /**
     * Working with Deque (double ended queues)
     * Dequeues allow poll and pollLast (pop items from beginning as well as from the end)
     * */
    @Test
    public void dequeueTest() {

        // Remove Deque first if already exist
        StepVerifier.create(this.client.getDeque("number-instance-deque").delete().then())
                .verifyComplete();

        RDequeReactive<Long> deque = this.client.getDeque("number-instance-deque", LongCodec.INSTANCE);

        Mono<Void> queueAdd = Flux.range(1, 10)
                .map(Long::valueOf)
                .flatMap(deque::add)
                .then();

        StepVerifier.create(queueAdd)
                        .verifyComplete();

        Mono<Void> stackPoll =  deque.pollLast()
                .repeat(3)
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(stackPoll)
                .verifyComplete();

        StepVerifier.create(deque.size())
                .expectNext(6)
                .verifyComplete();

    }
}
