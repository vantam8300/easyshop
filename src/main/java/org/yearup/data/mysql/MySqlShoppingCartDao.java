package org.yearup.data.mysql;

import org.springframework.stereotype.Component;
import org.yearup.data.ShoppingCartDao;
import org.yearup.models.Product;
import org.yearup.models.ShoppingCart;
import org.yearup.models.ShoppingCartItem;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Component
public class MySqlShoppingCartDao extends MySqlDaoBase implements ShoppingCartDao {
    public MySqlShoppingCartDao(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    public ShoppingCart getByUserId(int userId) {
        ShoppingCart shoppingCart = new ShoppingCart();

        String sql = "SELECT * FROM shopping_cart " +
                "JOIN products ON shopping_cart.product_id = products.product_id " +
                "WHERE user_id = ? ";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);

            ResultSet row = statement.executeQuery();

            while (row.next())
            {
                Product product = mapRow(row);
                ShoppingCartItem item = new ShoppingCartItem();
                item.setProduct(product);
                item.setQuantity(row.getInt("quantity"));
                shoppingCart.add(item);
            }
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        return shoppingCart;
    }
    @Override
    public ShoppingCart addItem(int userId, int productId) {

        ShoppingCart shoppingCart = getByUserId(userId);

        if (shoppingCart.contains(productId)) {
            String sql = "UPDATE shopping_cart" +
                    " SET quantity = ? " +
                    " WHERE product_id = ? and user_id = ?;";

            try (Connection connection = getConnection())
            {
                PreparedStatement statement = connection.prepareStatement(sql);
                statement.setInt(1, shoppingCart.get(productId).getQuantity() + 1);
                statement.setInt(2, productId);
                statement.setInt(3, userId);

                statement.executeUpdate();
            }
            catch (SQLException e)
            {
                e.printStackTrace();
                throw new RuntimeException(e);
            }

        } else {
            String sql = "INSERT INTO shopping_cart(user_id, product_id, quantity) " +
                    " VALUES (?, ?, ?);";

            try (Connection connection = getConnection())
            {
                PreparedStatement statement = connection.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS);

                statement.setInt(1, userId);
                statement.setInt(2, productId);
                statement.setInt(3, 1);

                int rowsAffected = statement.executeUpdate();

                if (rowsAffected > 0) {

                    return getByUserId(userId);
                }
            }
            catch (SQLException e)
            {
                throw new RuntimeException(e);
            }
        }
        return null;
    }

    @Override
    public void updateItem(int userId, int productId, ShoppingCartItem shoppingCartItem) {
        String sql = "UPDATE shopping_cart" +
                " SET quantity = ? " +
                " WHERE product_id = ? and user_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, shoppingCartItem.getQuantity());
            statement.setInt(2, productId);
            statement.setInt(3, userId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteCart(int userId) {
        String sql = "DELETE FROM shopping_cart " +
                " WHERE user_id = ?;";

        try (Connection connection = getConnection())
        {
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setInt(1, userId);

            statement.executeUpdate();
        }
        catch (SQLException e)
        {
            throw new RuntimeException(e);
        }
    }

    protected static Product mapRow(ResultSet row) throws SQLException
    {
        int productId = row.getInt("product_id");
        String name = row.getString("name");
        BigDecimal price = row.getBigDecimal("price");
        int categoryId = row.getInt("category_id");
        String description = row.getString("description");
        String color = row.getString("color");
        int stock = row.getInt("stock");
        boolean isFeatured = row.getBoolean("featured");
        String imageUrl = row.getString("image_url");

        return new Product(productId, name, price, categoryId, description, color, stock, isFeatured, imageUrl);
    }
}
