package blog.yrol.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.RPatternTopicReactive;
import org.redisson.api.RTopicReactive;
import org.redisson.api.listener.PatternMessageListener;
import org.redisson.client.codec.StringCodec;
import reactor.test.StepVerifier;

/**
 *
 * Working with publishers and subscribers.
 * A publisher can have one or more subscribers (one to many)
 * Execution:
 * 1. Run both subscriber1 and subscriber2
 * 2. Run publisher
 * 3. (Optional) Publish message using redis-cli : "publish slack-room1 hi"
 * Outcome:  subscriber1 and subscriber2 both will get the publisher message
 * **/
public class Lec12PubSubTest extends BaseTest {

    /**
     * Subscriber that uses the exact topic name via "getTopic"
     * **/
    @Test
    public void subscriber1() throws InterruptedException {
        RTopicReactive topic = this.client.getTopic("slack-room1", StringCodec.INSTANCE);
        topic.getMessages(String.class)
                .doOnError(System.out::println)
                .doOnNext(System.out::println)
                .subscribe();
        sleep(6000000);
    }

    /**
     * Subscriber that uses the pattern to get the topic name via getPatternTopic
     * **/
    @Test
    public void subscriber2() throws InterruptedException {
        RPatternTopicReactive topic = this.client.getPatternTopic("slack-room*", StringCodec.INSTANCE);
        topic.addListener(String.class, new PatternMessageListener<String>() {
            @Override
            public void onMessage(CharSequence pattern, CharSequence channel, String msg) {
                System.out.println(pattern + ":" + topic + ":" + msg);
            }
        }).subscribe();
        sleep(6000000);
    }

    /**
     * Publisher that send out a message
     * */
    @Test
    public void publisher() {
        RTopicReactive publisher = this.client.getTopic("slack-room1");
        StepVerifier.create(publisher.publish("How are you?").then())
                .verifyComplete();

    }
}
