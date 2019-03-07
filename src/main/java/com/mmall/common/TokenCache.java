package com.mmall.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * @Description: 用于将忘记密码的token存放本地缓存
 * @author: wuleshen
 */
public class TokenCache {

    public static final String TOKEN_PREFIX = "token_";

    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);

    private static LoadingCache<String,String> localCache = CacheBuilder.newBuilder()
            .initialCapacity(1000)      //缓存的初始化容量
            .maximumSize(10000)         //缓存的最大容量，当超过这个值时，用LRU算法移除缓存项
            .expireAfterAccess(12, TimeUnit.HOURS)      //缓存的有效期，设置为12小时
            .build(new CacheLoader<String, String>() {
                //默认的数据加载实现，当调用get取值时，如果key没有对应的值，就调用该方法进行加载
                @Override
                public String load(String key) throws Exception {
                        return "null";      //为了避免异常（key.equal(token)），返回字符串null
                }
            });

    public static void setKey(String key, String value) {
        localCache.put(key,value);
    }

    public static String getKey(String key) {
        String value = null;
        try {
            value = localCache.get(key);
            if("null".equals(value)) {
                return null;
            }
            return value;
        } catch (Exception e) {
            logger.error("localcache get error", e);
        }
        return null;
    }
}
