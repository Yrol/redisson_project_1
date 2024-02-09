package blog.yrol.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.BatchOptions;
import org.redisson.api.RBatchReactive;
import org.redisson.api.RListReactive;
import org.redisson.api.RSetReactive;
import org.redisson.client.codec.LongCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


/**
 * Working with batch requests
 * Batches can be used for reducing the network calls. Ex: collect items/data for 5 seconds and then save into Redis.
 * Compare the execution time of both batchTest() and withoutBatchTest()
 * **/
public class Lec13BatchTest extends BaseTest {

    /**
     * Test case using batches
     * This will ake only one call to Redis
     * **/
    @Test
    public void batchTest() {
        RBatchReactive batch = this.client.createBatch(BatchOptions.defaults());
        RListReactive<Long> list = batch.getList("numbers-list-batch", LongCodec.INSTANCE);
        RSetReactive<Long> set =  batch.getSet("numbers-set-batch", LongCodec.INSTANCE);

        // Adding the to list and set, but not saving to Redis (not subscribed)
        for (long i = 0; i < 200000; i++) {
            list.add(i);
            set.add(i);
        }

        // Using execute() save the above set and list at once to Redis.
        StepVerifier.create(batch.execute().then())
                .verifyComplete();
    }


    /**
     * Test case without using batches.
     * This will make 400000 calls to the redis (for adding 200000 data to list and set each)
     * **/
    @Test
    public void withoutBatchTest() {
        RListReactive<Long> list = this.client.getList("numbers-list", LongCodec.INSTANCE);
        RSetReactive<Long> set = this.client.getSet("numbers-set", LongCodec.INSTANCE);

        // Adding to the list and saving to Redis one by one as opposed to the above test
        Mono<Void> mono = Flux.range(1, 200000)
                .map(Long::valueOf)
                .flatMap(i -> list.add(i).then(set.add(i)))
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
    }
}
