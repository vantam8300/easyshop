package org.yearup.data;

import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

public interface ShoppingCartDao
{
    // add additional method signatures here
    ShoppingCart getByUserId(int userId);
    ShoppingCart addItem(int userId, int product);
    void updateItem(int userId, int productId, ShoppingCartItem product);
    ShoppingCart deleteCart(int userId);
}
