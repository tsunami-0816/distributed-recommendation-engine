package com.example.recommend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisAutoConfiguration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 分布式智能推荐引擎启动类。
 * <p>
 * 本次实现模块：M09 推荐场景编排。
 * 排除 Redis 自动配置（未安装 Redis 时使用内存缓存兜底，保证服务可独立启动）。
 *
 * @author recommend-team
 * @since 1.0.0
 */
@SpringBootApplication(exclude = {RedisAutoConfiguration.class})
@EnableScheduling
// 修复Bug-008：启用 @Async 异步方法支持，曝光埋点异步执行不阻塞推荐响应
@EnableAsync
public class RecommendationEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(RecommendationEngineApplication.class, args);
    }
}
