package com.hixtrip.sample.domain.pay.strategy;

import com.hixtrip.sample.domain.inventory.InventoryConstants;
import com.hixtrip.sample.domain.inventory.InventoryDomainService;
import com.hixtrip.sample.domain.inventory.model.Inventory;
import com.hixtrip.sample.domain.order.OrderDomainService;
import com.hixtrip.sample.domain.order.model.Order;
import com.hixtrip.sample.domain.order.repository.OrderRepository;
import com.hixtrip.sample.domain.pay.PayDomainService;
import com.hixtrip.sample.domain.pay.model.CommandPay;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component(value = "paymentFailureStrategy")
public class PaymentFailureStrategy implements PayCallStrategy {
    @Autowired
    private PayDomainService payDomainService;
    @Autowired
    private OrderDomainService orderDomainService;
    @Autowired
    private InventoryDomainService inventoryDomainService;
    @Autowired
    private OrderRepository orderRepository;

    @Override
    public void execute(String orderId) {
        CommandPay commandPay = CommandPay.builder()
                .orderId(orderId)
                .payStatus(PayStatus.FAIL)
                .build();

        payDomainService.payRecord(commandPay);
        orderDomainService.orderPayFail(commandPay);

        //支付失败，回溯库存操作。
        Order order = orderRepository.getById(orderId);
        if (order == null) {
            throw new RuntimeException("订单不存在");
        }

        Inventory inventory = inventoryDomainService.getInventory(order.getSkuId());
        if (inventory != null) {
            inventory.setSellableQuantity(inventory.getSellableQuantity() + order.getAmount());
            inventory.setWithholdingQuantity(inventory.getWithholdingQuantity() - order.getAmount());
            inventoryDomainService.changeInventory(inventory.getSkuId(), inventory.getSellableQuantity(), inventory.getWithholdingQuantity(), inventory.getOccupiedQuantity());
        }

    }
}
