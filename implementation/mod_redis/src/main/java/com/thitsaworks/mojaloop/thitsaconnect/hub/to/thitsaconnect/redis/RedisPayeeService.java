package com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis;

import com.thitsaworks.mojaloop.thitsaconnect.hub.to.thitsaconnect.redis.data.HubToPayeeOutput;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class RedisPayeeService {

    private static final Logger LOG = LoggerFactory.getLogger(RedisPayeeService.class);

    @Autowired
    private RedissonClient redissonClient;

    @Autowired
    private RedisConfiguration.Settings settings;

   // private final int DATA_LIFETIME = Integer.parseInt(settings.getCacheLifetime()); // minutes

    public HubToPayeeOutput getDataWithTransferId(String transferId) {

        RMapCache<String, HubToPayeeOutput> transferIdMap = redissonClient.getMapCache("hubToPayeeOutputMap");

        if (transferIdMap == null || transferIdMap.isEmpty()) {return null;}

        return transferIdMap.get(transferId);
    }

    public void removeFromMap(String transferId) {

        RMapCache<String, HubToPayeeOutput> transferIdMap = redissonClient.getMapCache("hubToPayeeOutputMap");

        if (transferIdMap != null && !transferIdMap.isEmpty()) {

            transferIdMap.remove(transferId);
        }
    }

    public void putToRedisMap(HubToPayeeOutput getPartyOutput) {

        RMapCache<String, HubToPayeeOutput> transferIdMap = redissonClient.getMapCache("hubToPayeeOutputMap");
        transferIdMap.put(getPartyOutput.getTransferId(), getPartyOutput,  Integer.parseInt(settings.getCacheLifetime()), TimeUnit.MINUTES);

    }

}
