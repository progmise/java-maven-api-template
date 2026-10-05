package io.github.progmise.api.infrastructure.config;

import io.github.progmise.api.domain.FeatureToggle;
import io.github.progmise.commons.infrastructure.Cache;
import io.github.progmise.commons.infrastructure.FeatureToggleHelper;
import io.github.progmise.commons.infrastructure.NoOpCache;
import io.github.progmise.commons.infrastructure.RCache;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.redisson.config.SingleServerConfig;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

// Cache wiring: RedissonClient only when cache.enabled=true (otherwise the
// app would fail to boot without Redis). Cache falls back to NoOpCache —
// lookups fail-open. The CACHE_ON toggle wraps RCache so cache reads can be
// disabled at runtime without redeploying.
@Configuration(proxyBeanMethods = false)
public class CacheConfig {

    @Bean(destroyMethod = "shutdown")
    @ConditionalOnProperty(name = "cache.enabled", havingValue = "true")
    public RedissonClient redissonClient(
        @Value("${spring.data.redis.host:localhost}") String host,
        @Value("${spring.data.redis.port:6379}") int port,
        @Value("${spring.data.redis.password:}") String password,
        @Value("${spring.data.redis.ssl.enabled:false}") boolean ssl,
        @Value("${spring.data.redis.timeout:5000}") int timeout
    ) {
        Config config = new Config();
        SingleServerConfig single = config.useSingleServer()
            .setAddress((ssl ? "rediss://" : "redis://") + host + ":" + port)
            .setTimeout(timeout);
        if (!password.isBlank()) {
            single.setPassword(password);
        }
        return Redisson.create(config);
    }

    @Bean
    public Cache cache(
        ObjectProvider<RedissonClient> redisson,
        ObjectProvider<FeatureToggleHelper> toggles,
        @Value("${cache.name:cache}") String name,
        @Value("${cache.ttl:300}") long ttl
    ) {
        RedissonClient client = redisson.getIfAvailable();
        if (client == null) {
            return new NoOpCache();
        }
        return new RCache(client, name, ttl, TimeUnit.SECONDS, () -> {
            FeatureToggleHelper helper = toggles.getIfAvailable();
            return helper != null && helper.isActive(FeatureToggle.CACHE_ON);
        });
    }
}
