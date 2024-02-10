package blog.yrol.test;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.redisson.api.RBucketReactive;
import org.redisson.api.RTransactionReactive;
import org.redisson.api.TransactionOptions;
import org.redisson.client.codec.LongCodec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;


/**
 * Testing transactional vs non-transactional
 * In transactional, the operation can be cancelled if an error occurred in the pipeline
 * **/
public class Lec14TransactionalTest extends BaseTest {

    private RBucketReactive<Long> user1Balance;
    private RBucketReactive<Long> user2Balance;

    @BeforeAll
    public void accountSetup() {
        user1Balance = this.client.getBucket("user:1:balance", LongCodec.INSTANCE);
        user2Balance = this.client.getBucket("user:2:balance", LongCodec.INSTANCE);

        Mono<Void> mono = user1Balance.set(100L)
                .then(user2Balance.set(0L))
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
    }

    @AfterAll
    public void accountBalanceStatus () {
        Mono<Void> mono = Flux.zip(this.user1Balance.get(), this.user2Balance.get())
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
    }

    /**
     * Non-transaction test - the operation will be continued although there is an error.
     * **/
    @Test
    public void nonTransactionTest() throws InterruptedException {

        this.transfer(user1Balance, user2Balance, 50)
                .thenReturn(0)
                .map(i -> (5/i)) // simulate errors on purpose (attempting to divide by zero)
                .doOnError(System.out::println)
                .subscribe();

        sleep(1000);
    }

    /**
     * Transaction test - operation will be rolled back on error
     * **/
    @Test
    public void transactionTest() throws InterruptedException {
        RTransactionReactive transaction = this.client.createTransaction(TransactionOptions.defaults()); // defining a transaction
        RBucketReactive<Long> user1Balance = transaction.getBucket("user:1:balance", LongCodec.INSTANCE);
        RBucketReactive<Long> user2Balance = transaction.getBucket("user:2:balance", LongCodec.INSTANCE);
        this.transfer(user1Balance, user2Balance, 50)
                .thenReturn(0)
                .map(i -> (5/i)) // simulate errors on purpose (attempting to divide by zero)
                .then(transaction.commit())
                .doOnError(System.out::println)
                .doOnError(ex -> transaction.rollback()) // rolling back the transaction on an error.
                .subscribe();

        sleep(1000);
    }

    private Mono<Void> transfer(RBucketReactive<Long> fromAccount, RBucketReactive<Long> toAccount, int amount) {

        /**
         * Scenario of transferring money from one account to another and return both balances in a tuple using zip. Ex: [50, 50]
         * get() will fetch the values
         * Check if fromAccount balance (t.getT1()) is greater than amount to be transferred.
         * **/
        return Flux.zip(fromAccount.get(), toAccount.get())
                .filter(t -> t.getT1() >= amount)
                .flatMap(t -> fromAccount.set(t.getT1() - amount).thenReturn(t))
                .flatMap(t -> toAccount.set(t.getT2() + amount))
                .then();
    }
}
