package org.yearup.data;

import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;

public interface ShoppingCartDao
{
    // add additional method signatures here
    ShoppingCart getByUserId(int userId);
    ShoppingCart addItem(int userId, Product product);
    ShoppingCart updateItem(int userId, int productId, Product product);
    ShoppingCart deleteItem(int userId, int productId);
}
