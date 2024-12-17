package org.yearup.data;

import org.yearup.models.Order;

public interface OrderDao {
    Order getById(int orderId);
    Order addOrder(int userId);
    void addOrderLineItem(int userId, int orderId);
}
