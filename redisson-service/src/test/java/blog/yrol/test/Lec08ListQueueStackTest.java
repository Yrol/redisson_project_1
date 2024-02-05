package blog.yrol.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.RListReactive;
import org.redisson.client.codec.LongCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * Testing lists using Publishers and streams
 * If the lists are already exist, make sure the Redis DB is flushed "flushed"
 * **/
public class Lec08ListQueueStackTest extends BaseTest {

    @Test
    public void listTest01() {

        RListReactive<Long> list = this.client.getList("number-instance", LongCodec.INSTANCE);

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
}
