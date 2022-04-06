package com.me.db.api;

import com.me.db.DbApplication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.junit.Assert;

import java.util.concurrent.ThreadLocalRandom;

/**
 * wrk 加 10 个线程 50 个并发连接做压测
 * wrk -t 10 -c 50 -d 10s http://localhost:8080/mysql --latency
 * wrk -t 10 -c 50 -d 10s http://localhost:8080/redis --latency
 * wrk -t 10 -c 50 -d 10s http://localhost:8080/mysql2 --latency
 * wrk -t 10 -c 50 -d 10s http://localhost:8080/redis2 --latency
 * Redis 数据都保存在内存中，所以读写单一 Key 的性能非常高。
 * 但Redis 薄弱的地方是，不擅长做 Key 的搜索。对 MySQL，
 * 我们可以使用 LIKE 操作前匹配走 B+ 树索引实现快速搜索；
 * 但对 Redis，我们使用 Keys 命令对 Key 的搜索，其实相当于在 MySQL 里做全表扫描。
 */
@RestController
public class MysqlAndRedisController {
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @GetMapping("redis")
    public void redis() {
        Assert.assertEquals(stringRedisTemplate.opsForValue().get("item" + (ThreadLocalRandom.current().nextInt(DbApplication.ROWS) + 1)), DbApplication.PAYLOAD);
    }

    @GetMapping("redis2")
    public void redis2() {
        Assert.assertEquals(1111, stringRedisTemplate.keys("item71*").size());
    }

    @GetMapping("mysql")
    public void mysql() {
        Assert.assertEquals(jdbcTemplate.queryForObject("SELECT data FROM `r` WHERE name=?", new Object[]{("item" + (ThreadLocalRandom.current().nextInt(DbApplication.ROWS) + 1))}, String.class), DbApplication.PAYLOAD);
    }

    @GetMapping("mysql2")
    public void mysql2() {
        Assert.assertEquals(1111, jdbcTemplate.queryForList("SELECT name FROM `r` WHERE name LIKE 'item71%'", String.class).size());
    }
}
