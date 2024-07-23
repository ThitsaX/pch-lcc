package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis;

import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.HubToPayerOutput;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.redisson.api.RMapCache;
import org.redisson.api.RTopic;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class RedisPayerService {

    private static final Logger LOG = LoggerFactory.getLogger(RedisPayerService.class);

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisConfiguration.Settings settings;

    private static final int SUBSCRIPTION_TIMEOUT = 10000;

    // private static final int DATA_LIFETIME = 3; // minutes

    public HubToPayerOutput subscribe(String topicToListen) throws InterruptedException {

        RMapCache<String, HubToPayerOutput> transferIdMap = redissonClient.getMapCache("firstCallMap");

        if (transferIdMap != null && !transferIdMap.isEmpty() && transferIdMap.containsKey(topicToListen)) {

            HubToPayerOutput output = transferIdMap.get(topicToListen);

            if (output != null) {

                return transferIdMap.get(topicToListen);
            }
        }

        GetPartyOutputWrapper outputWrapper = new GetPartyOutputWrapper();
        RTopic topic = redissonClient.getTopic(topicToListen);
        CountDownLatch latch = new CountDownLatch(1);

        int listenerId = topic.addListener(HubToPayerOutput.class, (charSequence, output) -> {
            outputWrapper.setOutput(output);
            latch.countDown();
        });


        latch.await(10, TimeUnit.SECONDS);

        topic.removeListener(listenerId);


        return outputWrapper.getOutput();
    }

    public HubToPayerOutput getDataWithTransferId(String transferId) {

        RMapCache<String, HubToPayerOutput> transferIdMap = redissonClient.getMapCache("hubToPayerOutputMap");

        if (transferIdMap == null || transferIdMap.isEmpty()) { return null; }

        return transferIdMap.get(transferId);
    }

    public void putToMap(String transferId, HubToPayerOutput hubToPayerOutput) {

        RMapCache<String, HubToPayerOutput> transferIdMap = redissonClient.getMapCache("hubToPayerOutputMap");

        if (transferIdMap != null) {

            transferIdMap.put(transferId,
                              hubToPayerOutput,
                              Integer.parseInt(settings.getCacheLifetime()),
                              TimeUnit.MINUTES);
        }
    }

    public void removeFromMap(String transferId) {

        RMapCache<String, HubToPayerOutput> transferIdMap = redissonClient.getMapCache("hubToPayerOutputMap");

        if (transferIdMap != null && !transferIdMap.isEmpty()) {

            transferIdMap.remove(transferId);
        }
    }

    public void removeTopicFromMap(String topicId) {

        RMapCache<String, HubToPayerOutput> topicIdMap = redissonClient.getMapCache("firstCallMap");

        if (topicIdMap != null && !topicIdMap.isEmpty()) {

            topicIdMap.remove(topicId);
        }
    }

    public void publishData(String topicToPublish, HubToPayerOutput getPartyOutput) {

        RTopic topic = redissonClient.getTopic(topicToPublish);
        topic.publish(getPartyOutput);

    }

    public void publishFirstCallData(String topicToPublish, HubToPayerOutput getPartyOutput) {

        RMapCache<String, HubToPayerOutput> firstCallMapMap = redissonClient.getMapCache("firstCallMap");

        if (!firstCallMapMap.containsKey(topicToPublish)) {

            RTopic topic = redissonClient.getTopic(topicToPublish);
            topic.publish(getPartyOutput);

            firstCallMapMap.put(topicToPublish,
                                getPartyOutput,
                                Integer.parseInt(settings.getCacheLifetime()),
                                TimeUnit.MINUTES);

        }

    }

    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    private static class GetPartyOutputWrapper {

        private HubToPayerOutput output;

    }

}
