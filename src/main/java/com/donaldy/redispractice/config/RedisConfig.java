package com.donaldy.redispractice.config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    /*@Bean
    public RedisTemplate<Object, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        template.afterPropertiesSet();
        return template;
    }*/

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useClusterServers()
                .addNodeAddress("redis://172.18.1.23:7003")
                .addNodeAddress("redis://172.18.1.23:7004")
                .addNodeAddress("redis://172.18.1.24:7003")
                .addNodeAddress("redis://172.18.1.24:7004")
                .addNodeAddress("redis://172.18.1.26:7003")
                .addNodeAddress("redis://172.18.1.26:7004")
                .setPassword("7Ego=3eyE");

        return Redisson.create(config);
    }

}