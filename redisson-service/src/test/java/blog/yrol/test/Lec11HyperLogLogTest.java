package blog.yrol.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.RHyperLogLogReactive;
import org.redisson.client.codec.LongCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.LongStream;

/**
 * HyperLogLog (HLL) is a probabilistic data structure that estimates the number of UNIQUE elements (cardinality) of a set.
 * In short, counting unique items of list require a lot of memory, however in HLL will use 12k bytes in the worst case.
 * HLL accuracy - provides a standard error of 0.81%.
 * Use cases: How many unique visits has this page had on this day?
 * Documentation: https://redis.io/docs/data-types/probabilistic/hyperloglogs/#:~:text=of%20a%20set.-,HyperLogLog%20is%20a%20probabilistic%20data%20structure%20that%20estimates%20the%20cardinality,accuracy%20for%20efficient%20space%20utilization.
 * **/
public class Lec11HyperLogLogTest extends BaseTest {

    @Test
    public void count() {

        RHyperLogLogReactive<Long> counter = this.client.getHyperLogLog("user-visits", LongCodec.INSTANCE);


        // creating lists with some overlapping values.
        List<Long> list1 =  LongStream.rangeClosed(1,25000)
                .boxed()
                .collect(Collectors.toList());

        List<Long> list2 =  LongStream.rangeClosed(25001,50000)
                .boxed()
                .collect(Collectors.toList());

        List<Long> list3 =  LongStream.rangeClosed(1,70000)
                .boxed()
                .collect(Collectors.toList());

        List<Long> list4 =  LongStream.rangeClosed(50000,100000)
                .boxed()
                .collect(Collectors.toList());

        // Adding all the list values to the RHyperLogLogReactive
        Mono<Void> mono =  Flux.just(list1, list2, list3, list4)
                        .flatMap(counter::addAll)
                                .then();

        StepVerifier.create(mono)
                .verifyComplete();

        // Printing how many unique items are in the list
        counter.count()
                .doOnNext(System.out::println)
                .subscribe();
    }
}
