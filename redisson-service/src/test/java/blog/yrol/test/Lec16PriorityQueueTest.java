package blog.yrol.test;

import blog.yrol.test.assignment.Category;
import blog.yrol.test.assignment.PriorityQueue;
import blog.yrol.test.assignment.UserOrder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.RScoredSortedSetReactive;
import org.redisson.codec.TypedJsonJacksonCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import java.time.Duration;

/**
 * Testing the priority queue.
 * The priority will be given to the categories based on the order they're defined in the Category.java, hence 1.PRIME, 2.STD and 3.GUEST will be given priority respectively.
 * The items added to each category will have their own score defined via getScore()
 * Triggering test cases:
 * 1. Run consumer
 * 2. Option 1: Running producerRegular() - will automatically
 * 3. Option 2: Running producerUnlimited() - will need to stop manually
 * Consumer process priority: PRIME, STD, GUEST
 * */
public class Lec16PriorityQueueTest extends BaseTest {

    private PriorityQueue priorityQueue;

    @BeforeAll
    public void setupQueue() {
        RScoredSortedSetReactive<UserOrder> sortedSet = this.client.getScoredSortedSet("user:order:queue", new TypedJsonJacksonCodec(UserOrder.class));
        this.priorityQueue = new PriorityQueue(sortedSet);
    }

    /**
     * Producer with 5 orders
     * **/
    @Test
    public void producerRegular() throws InterruptedException {

        UserOrder userOrder1 = new UserOrder(1, Category.GUEST);
        UserOrder userOrder2 = new UserOrder(2, Category.STD);
        UserOrder userOrder3 = new UserOrder(3, Category.PRIME);
        UserOrder userOrder4 = new UserOrder(4, Category.STD);
        UserOrder userOrder5 = new UserOrder(5, Category.GUEST);
        Mono<Void> mono =  Flux.just(userOrder1, userOrder2, userOrder3, userOrder4, userOrder5)
                .flatMap(this.priorityQueue::addUser)
                .then();

        StepVerifier.create(mono)
                .verifyComplete();

        sleep(60000);
    }

    /**
     * Producer runs in loop and adding orders (the iterator is increased by 5 each time)
     * **/
    @Test
    public void producerUnlimited() throws InterruptedException {

        Flux.interval(Duration.ofSeconds(1))
                .map(i -> (i.intValue() * 5))
                .doOnNext(i -> {
                    UserOrder userOrder1 = new UserOrder(i + 1, Category.GUEST);
                    UserOrder userOrder2 = new UserOrder(i + 2, Category.STD);
                    UserOrder userOrder3 = new UserOrder(i + 3, Category.PRIME);
                    UserOrder userOrder4 = new UserOrder(i + 4, Category.STD);
                    UserOrder userOrder5 = new UserOrder(i + 5, Category.GUEST);
                    Mono<Void> mono =  Flux.just(userOrder1, userOrder2, userOrder3, userOrder4, userOrder5)
                            .flatMap(this.priorityQueue::addUser)
                            .then();

                    StepVerifier.create(mono)
                            .verifyComplete();
                }).subscribe();

        sleep(60000);
    }

    @Test
    public void consumer() throws InterruptedException {
        this.priorityQueue.getItems()
                .delayElements(Duration.ofMillis(500))
                .doOnNext(System.out::println)
                .subscribe();

        sleep(6000000);
    }

}
