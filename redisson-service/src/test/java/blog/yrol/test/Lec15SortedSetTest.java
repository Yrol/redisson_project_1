package blog.yrol.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.RScoredSortedSetReactive;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.function.Function;

/**
 * Working with sorted lists
 * **/
public class Lec15SortedSetTest extends BaseTest {

    @Test
    public void sortedSet() throws InterruptedException {
        RScoredSortedSetReactive<String> sortedSet = this.client.getScoredSortedSet("student:score", StringCodec.INSTANCE);

        Mono<Void> mono = sortedSet.addScore("sam", 12.25)// addScore() will create a new entry or if the value already exist it'll add
                .then(sortedSet.add(23.35, "mike")) // add() will always create a new value (overwrite if already exist)
                .then(sortedSet.addScore("jake", 7))
                .then();

        StepVerifier.create(mono)
                .verifyComplete();

        // Get values between 0 - 1 index inclusive (will return ascending order by value)
        sortedSet.entryRange(0, 1)
                .flatMapIterable(Function.identity()) // returning a flux instead of a collection
                .map(se -> se.getScore() + " : " + se.getValue())
                .doOnNext(System.out::println)
                        .subscribe();

        sleep(1000);
    }
}
