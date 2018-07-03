package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.*;
import redis.clients.util.Hashing;
import redis.clients.util.Sharded;

import java.util.ArrayList;
import java.util.List;

/**
 * @author : pengyao
 * @Date: 2018/7/3 11: 16
 */
public class RedisShardedPool {
    //Sharded jedis连接池
    private static ShardedJedisPool pool;
    //最大连接数
    private static Integer maxTotal = Integer.parseInt(PropertiesUtil.getProperty("redis.max.total","20"));
    //在jedispool中最大的idle状态（空闲状态）的jedis实例个数
    private static Integer maxIdle =Integer.parseInt(PropertiesUtil.getProperty("redis.max.idle","10"));
    //在jedispool中最小的idle状态（空闲状态）的jedis实例个数
    private static Integer minIdle =Integer.parseInt(PropertiesUtil.getProperty("redis.min.idle","2"));
    //在borrow一个jedis实例时，是否需要进行验证操作，如果赋值为true,表明的得到的jedis实例是可用的
    private static boolean testOnBorrow =Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.borrow","true"));
    //在borrow一个jedis实例时，是否需要进行验证操作，如果赋值为true,表明的放回jedispool的jedis实例是可用的
    private static boolean testOnReturn =Boolean.parseBoolean(PropertiesUtil.getProperty("redis.test.return","true"));
    private static String redis1IP = PropertiesUtil.getProperty("redis1.ip");
    private static Integer redis1Port =Integer.parseInt(PropertiesUtil.getProperty("redis1.port"));
    private static String redis2IP = PropertiesUtil.getProperty("redis2.ip");
    private static Integer redis2Port =Integer.parseInt(PropertiesUtil.getProperty("redis2.port"));

    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        //连接耗尽时是否阻塞，false会抛出异常，true会阻塞直到超时，默认为true
        config.setBlockWhenExhausted(true);
        JedisShardInfo info1 = new JedisShardInfo(redis1IP,redis1Port,1000*2);//默认超时时间就是2秒
        JedisShardInfo info2 = new JedisShardInfo(redis2IP,redis2Port,1000*2);

        List<JedisShardInfo> jedisShardInfoList = new ArrayList<JedisShardInfo>(2);
        jedisShardInfoList.add(info1);
        jedisShardInfoList.add(info2);

        pool = new ShardedJedisPool(config,jedisShardInfoList, Hashing.MURMUR_HASH, Sharded.DEFAULT_KEY_TAG_PATTERN);

    }

    static{
        initPool();
    }

    //提供方法返回jedis
    public  static ShardedJedis getJedis(){
        return pool.getResource();
    }
    //返回损坏的jedis
    public static void returnBrokenResource(ShardedJedis jedis){
        pool.returnBrokenResource(jedis);
    }
    //返回正常的jedis
    public static void returnResource(ShardedJedis jedis){
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        //默认select 0
        ShardedJedis jedis = pool.getResource();

        for (int i = 0; i < 10; i++){
            jedis.set("key"+i,"vakue"+i);
        }
        returnResource(jedis);
        //pool.destroy();//临时调用，销毁所有连接
        System.out.println("program is over!!");
    }


}
