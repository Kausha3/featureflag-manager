package com.featureflag.service;

import com.featureflag.entity.FeatureFlag;
import com.featureflag.repository.FeatureFlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class FlagCacheService {

    private static final String FLAGS_CACHE_KEY = "featureflags:cache";
    private static final String ALL_FLAGS_KEY = "all_enabled_flags";
    private static final long CACHE_TTL_SECONDS = 60;

    private final RedissonClient redissonClient;
    private final FeatureFlagRepository flagRepository;

    private RMapCache<String, List<FeatureFlag>> flagsCache;

    @PostConstruct
    public void init() {
        flagsCache = redissonClient.getMapCache(FLAGS_CACHE_KEY);
        refreshCache();
    }

    public List<FeatureFlag> getAllEnabledFlagsWithRules() {
        List<FeatureFlag> cached = flagsCache.get(ALL_FLAGS_KEY);

        if (cached != null) {
            return cached;
        }

        List<FeatureFlag> flags = flagRepository.findAllEnabledWithRules();
        flagsCache.put(ALL_FLAGS_KEY, flags, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
        return flags;
    }

    public FeatureFlag getFlagByNameWithRules(String name) {
        List<FeatureFlag> allFlags = getAllEnabledFlagsWithRules();
        return allFlags.stream()
                .filter(f -> f.getName().equals(name))
                .findFirst()
                .orElse(null);
    }

    public void invalidateCache() {
        flagsCache.remove(ALL_FLAGS_KEY);
        log.info("Flag cache invalidated");
    }

    @Scheduled(fixedRate = 30000) // Refresh every 30 seconds
    public void refreshCache() {
        try {
            List<FeatureFlag> flags = flagRepository.findAllEnabledWithRules();
            flagsCache.put(ALL_FLAGS_KEY, flags, CACHE_TTL_SECONDS, TimeUnit.SECONDS);
            log.debug("Flag cache refreshed with {} flags", flags.size());
        } catch (Exception e) {
            log.error("Failed to refresh flag cache: {}", e.getMessage());
        }
    }
}
