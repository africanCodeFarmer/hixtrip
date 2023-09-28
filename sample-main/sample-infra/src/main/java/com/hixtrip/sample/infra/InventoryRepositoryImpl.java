package com.hixtrip.sample.infra;

import com.hixtrip.sample.domain.inventory.InventoryConstants;
import com.hixtrip.sample.domain.inventory.model.Inventory;
import com.hixtrip.sample.domain.inventory.repository.InventoryRepository;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * infra层是domain定义的接口具体的实现
 */
@Component
public class InventoryRepositoryImpl implements InventoryRepository {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private RedissonClient redissonClient;

    private static final String SKU_INVENTORY_KEY = "sku_inventory";

    @Override
    public Inventory getInventory(String skuId) {
        Object v = redisTemplate.opsForHash().get(SKU_INVENTORY_KEY, skuId);

        return v == null ? null : (Inventory) v;
    }

    @Override
    public Boolean changeInventory(Inventory inventory) {
        RLock lock = redissonClient.getLock(InventoryConstants.LOCK_PREFIX + inventory.getSkuId());
        boolean hasLock = false;
        try {
            hasLock = lock.tryLock(10, TimeUnit.SECONDS);
            if (!hasLock) {
                throw new RuntimeException("当前访问量过大，请稍后重试");
            }
            return redisTemplate.opsForHash().putIfAbsent(SKU_INVENTORY_KEY, inventory.getSkuId(), inventory);

        } catch (InterruptedException e) {
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        } finally {
            if (hasLock) {
                lock.unlock();
            }
        }
    }
}
