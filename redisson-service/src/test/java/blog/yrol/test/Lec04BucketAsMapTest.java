package blog.yrol.test;

import org.junit.jupiter.api.Test;
import org.redisson.api.*;
import org.redisson.client.codec.StringCodec;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.HashMap;
import java.util.Map;

/**
 * Testing the multiple buckets where Redis can give them as a single key value map
 * **/
public class Lec04BucketAsMapTest extends BaseTest{

    @Test
    public void bucketAsMap() {

        RBucketReactive<String> bucket;

        RBatchReactive batch = this.client.createBatch();
//        RListReactive<Object> list = batch.getList("namesList");

        Map<String, String> data = new HashMap<>();
        data.put("user:1:name", "Denis");
        data.put("user:2:name", "James");
        data.put("user:3:name", "Andrew");

        for(String key : data.keySet()) {
            batch.getBucket(key).set(data.get(key));
        }

        batch.execute().block();


        Mono<Void> mono = this.client.getBuckets(StringCodec.INSTANCE)
                .get("user:1:name", "user:2:name", "user:3:name")
                .doOnNext(System.out::println)
                .then();

        StepVerifier.create(mono)
                .verifyComplete();
    }
}
