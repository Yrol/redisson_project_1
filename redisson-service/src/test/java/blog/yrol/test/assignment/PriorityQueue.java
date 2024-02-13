package blog.yrol.test.assignment;

import org.redisson.api.RScoredSortedSetReactive;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PriorityQueue {

    RScoredSortedSetReactive<UserOrder> queue;

    public PriorityQueue(RScoredSortedSetReactive<UserOrder> queue) {
        this.queue = queue;
    }

    public Mono<Void> addUser(UserOrder userOrder){
        return this.queue.add(
//                userOrder.getCategory().ordinal(),
                getScore(userOrder.getCategory()),
                userOrder
        ).then();
    }

    /**
     * Fetching the orders from the list
     * Fetch one item at a time - in case of failure this will prevent loosing all items
     * **/
    public Flux<UserOrder> getItems() {
        return this.queue.takeFirstElements()
                .limitRate(1);
    }

    /**
     * Creating a unique double value to which provides a unique score to each item added to the queue
     * This will ensure the FIFO of the list for each category
     * **/
    private double getScore(Category category) {
        return category.ordinal() + Double.parseDouble("0." + System.nanoTime());
    }
}
