package com.mmall.common;

import com.mmall.util.PropertiesUtil;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author : pengyao
 * @Date: 2018/6/30 20: 04
 */
public class RedisPool {
    //jedis连接池
    private static JedisPool pool;
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
    private static String redisIP = PropertiesUtil.getProperty("redis.ip");
    private static Integer redisPort =Integer.parseInt(PropertiesUtil.getProperty("redis.port"));

    private static void initPool(){
        JedisPoolConfig config = new JedisPoolConfig();
        config.setMaxIdle(maxTotal);
        config.setMaxIdle(maxIdle);
        config.setMinIdle(minIdle);
        config.setTestOnBorrow(testOnBorrow);
        config.setTestOnReturn(testOnReturn);
        //连接耗尽时是否阻塞，false会抛出异常，true会阻塞直到超时，默认为true
        config.setBlockWhenExhausted(true);
        pool = new JedisPool(config,redisIP,redisPort,1000*2);
    }

    static{
        initPool();
    }

    //提供方法返回jedis
    public  static Jedis getJedis(){
        return pool.getResource();
    }
    //返回损坏的jedis
    public static void returnBrokenResource(Jedis jedis){
        pool.returnBrokenResource(jedis);
    }
    //返回正常的jedis
    public static void returnResource(Jedis jedis){
        pool.returnResource(jedis);
    }

    public static void main(String[] args) {
        //默认select 0
        Jedis jedis = pool.getResource();
        jedis.set("pengyaoKey","pengyaoValue");
        returnResource(jedis);
        pool.destroy();//临时调用，销毁所有连接
        System.out.println("program is over!!");
    }



}

