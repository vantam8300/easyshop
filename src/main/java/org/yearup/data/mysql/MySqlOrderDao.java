package org.yearup.data.mysql;


import org.springframework.stereotype.Component;
import org.yearup.data.OrderDao;
import org.yearup.models.Order;
import org.yearup.models.Profile;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.*;
import java.time.LocalDate;

@Component
public class MySqlOrderDao  extends MySqlDaoBase implements OrderDao {
    public MySqlOrderDao(DataSource dataSource) {
        super(dataSource);
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
                        return getById(orderId);
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

            try(ResultSet row = statement.executeQuery();) {

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


