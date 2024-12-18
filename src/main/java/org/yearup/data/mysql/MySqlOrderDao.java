package org.yearup.data.mysql;


import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;
import org.yearup.models.OrderLineItem;
import org.yearup.models.Profile;

import javax.mail.internet.MimeMessage;
import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlOrderDao  extends MySqlDaoBase implements OrderDao {

    private final JavaMailSender mailSender;

    public MySqlOrderDao(DataSource dataSource, JavaMailSender mailSender) {
        super(dataSource);
        this.mailSender = mailSender;
    }

    @Override
    public Order getById(int orderId) {
        String sql = "SELECT * FROM orders WHERE order_id = ?";
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {

            statement.setInt(1, orderId);

            ResultSet row = statement.executeQuery();

            if (row.next())
            {
                return mapRowOrder(row);
            }
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public Order addOrder(int userId) {
        String sql = "SELECT * FROM profiles WHERE user_id = ?";
        Profile profile = new Profile();
        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql))
        {
            statement.setInt(1, userId);
            try(ResultSet row = statement.executeQuery();) {
                if (row.next())
                {
                    profile = mapRowProfile(row);
                }
            }
            sql = "INSERT INTO orders (user_id, date, address, city, state, zip) " +
                    " VALUES (?, ?, ?, ?, ?, ?)";

            try(PreparedStatement statement2 = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                statement2.setInt(1, userId);
                statement2.setDate(2, Date.valueOf(LocalDate.now()));
                statement2.setString(3, profile.getAddress());
                statement2.setString(4, profile.getCity());
                statement2.setString(5, profile.getState());
                statement2.setString(6, profile.getZip());

                int rowsAffected = statement2.executeUpdate();

                if (rowsAffected > 0) {
                    // Retrieve the generated keys
                    ResultSet generatedKeys = statement2.getGeneratedKeys();

                    if (generatedKeys.next()) {
                        // Retrieve the auto-incremented ID
                        int orderId = generatedKeys.getInt(1);

                        addOrderLineItem(userId, orderId);
                        // get the newly inserted order
                        Order newOrder = getById(orderId);

                        // Retrieve order details (line items)
                        List<OrderLineItem> orderDetails = getOrderDetails(orderId);

                        sendOrderConfirmationEmail(profile.getEmail(), newOrder, orderDetails);

                        return newOrder;
                    }


                }
            }
        }  catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
        return null;
    }

    @Override
    public void addOrderLineItem(int userId, int orderId) {
        String sql = "SELECT * FROM shopping_cart JOIN products ON shopping_cart.product_id = products.product_id WHERE user_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, userId);

            try(ResultSet row = statement.executeQuery()) {

                while (row.next())
                {
                    sql = "INSERT INTO order_line_items (order_id, product_id, sales_price, quantity) " +
                            " VALUES (?, ?, ?, ?)";

                    try(PreparedStatement statement2 = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                        statement2.setInt(1, orderId);
                        statement2.setInt(2, row.getInt("product_id"));
                        statement2.setBigDecimal(3, row.getBigDecimal("price"));
                        statement2.setInt(4, row.getInt("quantity"));


                        statement2.executeUpdate();
                    }
                }
            }

        }  catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    private List<OrderLineItem> getOrderDetails(int orderId) {
        List<OrderLineItem> orderDetails = new ArrayList<>();
        String sql = "SELECT p.name, oli.quantity, oli.sales_price, oli.order_line_item_id, oli.product_id " +
                "FROM order_line_items oli " +
                "JOIN products p ON oli.product_id = p.product_id " +
                "WHERE oli.order_id = ?";

        try (Connection connection = getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, orderId);
            try (ResultSet row = statement.executeQuery()) {
                while (row.next()) {
                    int orderLineItemId = row.getInt("order_line_item_id");
                    int productId = row.getInt("product_id");
                    String productName = row.getString("name");
                    int quantity = row.getInt("quantity");
                    BigDecimal price = row.getBigDecimal("sales_price");
                    OrderLineItem orderLineItem = new OrderLineItem(orderLineItemId, orderId, productId, productName, price, quantity);
                    orderDetails.add(orderLineItem);
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch order details.", e);
        }

        return orderDetails;
    }

    private BigDecimal calculateTotalCost(List<OrderLineItem> orderDetails) {
        BigDecimal total = BigDecimal.ZERO;
        for (OrderLineItem item : orderDetails) {
            total = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
        }
        return total;
    }

    private void sendOrderConfirmationEmail(String toEmail, Order order, List<OrderLineItem> orderDetail) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(toEmail);
            helper.setSubject("Order Confirmation - Order #" + order.getOrderId());
            helper.setText(buildEmailContent(order, orderDetail), true);

            mailSender.send(message);
            System.out.println("Email sent successfully to " + toEmail);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to send email.", e);
        }
    }

    private String buildEmailContent(Order order, List<OrderLineItem> orderDetail) {
        StringBuilder content = new StringBuilder();

        content.append("<h1>Thank you for your order!</h1>");

        content.append("<p>Your order ID is: <strong>").append(order.getOrderId()).append("</strong></p>");

        content.append("<p>Order Date: ").append(order.getDate()).append("</p>");

        content.append("<p>Shipping Address:</p>");
        content.append("<p>").append(order.getAddress()).append(", ").append(order.getCity())
                .append(", ").append(order.getState()).append(" ").append(order.getZip()).append("</p>");

        content.append("<h2>Order Details:</h2>");
        content.append("<ul>");
        for (OrderLineItem item : orderDetail) {
            content.append("<li>").append(item).append("</li>");
        }
        content.append("</ul>");

        content.append("<h2>Total: $").append(calculateTotalCost(orderDetail)).append(" </h2>");

        content.append("<p><strong>We appreciate your business!</strong></p>");
        return content.toString();
    }

    protected static Profile mapRowProfile(ResultSet row) throws SQLException
    {
        int userId = row.getInt("user_id");
        String firstName = row.getString("first_name");
        String lastName = row.getString("last_name");
        String phone = row.getString("phone");
        String email = row.getString("email");
        String address = row.getString("address");
        String city = row.getString("city");
        String state = row.getString("state");
        String zip = row.getString("zip");

        return new Profile(userId, firstName, lastName, phone, email, address, city, state, zip);
    }
    protected static Order mapRowOrder(ResultSet row) throws SQLException
    {
        int orderId = row.getInt("order_id");
        int userId = row.getInt("user_id");
        LocalDate date = row.getDate("date").toLocalDate();
        String address = row.getString("address");
        String city = row.getString("city");
        String state = row.getString("state");
        String zip = row.getString("zip");
        BigDecimal shippingAmount = row.getBigDecimal("shipping_amount");

        return new Order(orderId, userId, date, address, city, state, zip, shippingAmount);
    }
}


