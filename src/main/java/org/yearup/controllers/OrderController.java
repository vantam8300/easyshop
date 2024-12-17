package org.yearup.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.yearup.data.OrderDao;
import org.yearup.data.ShoppingCartDao;
import org.yearup.data.UserDao;
import org.yearup.models.Order;
import org.yearup.models.User;

import java.security.Principal;

@RestController
@PreAuthorize("isAuthenticated()")
@RequestMapping("orders")
@CrossOrigin
public class OrderController {
    private UserDao userDao;
    private OrderDao orderDao;
    private ShoppingCartDao shoppingCartDao;


    public OrderController(UserDao userDao, OrderDao orderDao, ShoppingCartDao shoppingCartDao) {
        this.userDao = userDao;
        this.orderDao = orderDao;
        this.shoppingCartDao = shoppingCartDao;
    }

    @PostMapping
    public Order addOrder(Principal principal){
        try
        {
            // get the currently logged in username
            String userName = principal.getName();
            // find database user by userId
            User user = userDao.getByUserName(userName);
            int userId = user.getId();

            Order order =  orderDao.addOrder(userId);

            shoppingCartDao.deleteCart(userId);
            return order;
        }
        catch(Exception e)
        {
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Oops... our bad.");
        }
    }
}
