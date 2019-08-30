## 配置

### 一、数据库配置

1. 创建数据库
```
DROP SCHEMA IF EXISTS shop;
CREATE SCHEMA shop;
USE shop;
```

2. 创建表
```
DROP TABLE IF EXISTS `user`;
CREATE TABLE `user` (
  `id` INT UNSIGNED NOT NULL AUTO_INCREMENT COMMENT '主键',
  `username` VARCHAR(10) NOT NULL COMMENT '用户名',
  `age` TINYINT NOT NULL DEFAULT 1 COMMENT '年龄',
  `status` TINYINT NOT NULL DEFAULT 0 COMMENT '用户状态',
  `created_at` DATETIME NOT NULL DEFAULT current_timestamp COMMENT '创建时间',
  `updated_at` DATETIME NOT NULL DEFAULT current_timestamp ON UPDATE current_timestamp COMMENT '更新时间',
  PRIMARY KEY (id)
) ENGINE=InnoDB DEFAULT CHARSET utf8mb4 COMMENT '用户表';
```

### 二、Redis 配置

#### （1）Config

```
@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<Object, Object> redisCacheTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new GenericJackson2JsonRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory);
        template.afterPropertiesSet();
        return template;
    }
}
```

#### （2）属性配置

在 `application.properties` 下
```
spring.redis.host=localhost
spring.redis.password=
# 连接超时时间（毫秒）
spring.redis.timeout=10000
# Redis默认情况下有16个分片，这里配置具体使用的分片，默认是0
spring.redis.database=0
# 连接池最大连接数（使用负值表示没有限制） 默认 8
spring.redis.lettuce.pool.max-active=8
# 连接池最大阻塞等待时间（使用负值表示没有限制） 默认 -1
spring.redis.lettuce.pool.max-wait=-1
# 连接池中的最大空闲连接 默认 8
spring.redis.lettuce.pool.max-idle=8
# 连接池中的最小空闲连接 默认 0
spring.redis.lettuce.pool.min-idle=0
```



## 指令

### `Docker` 指令

1. 启动 `sudo docker run -d --name redis -p 6379:6379 redis --requirepass "redis"`

2. 进入容器 `sudo docker exec -it 6ad8199f75af redis-cli`

### `Redis` 指令

1. 查看所有键 `keys *`

### `MySQL` 指令

1. 启动 `sudo docker run -d mysql`

2. 进入容器 `sudo docker exec -it mysql bash`