# EASY SHOP

This project provides an e-commerce platform with an API backend. The system allows users to manage products, categories, shopping carts, and orders while supporting user authentication and role-based access.

## Features
- **Authentication**: User registration, login, and role-based access control.
- **Category Management**: Create, read, update, and delete product categories.
- **Product Management**: Full CRUD functionality with filtering options.
- **Shopping Cart**: Add, update, clear cart and remove products.
- **Order Processing**: Place orders from the shopping cart.
- **User Profile**: Manage user profiles.

---

## API Endpoints

### **Authentication**
- **Register**:  
  `POST http://localhost:8080/register`  
  **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "password",
    "confirmPassword": "password",
    "role": "ADMIN"
  }
  ```
- **Login**:  
  `POST http://localhost:8080/login`  
  **Request Body**:
  ```json
  {
    "username": "admin",
    "password": "password"
  }
  ```

### **Categories**
- **GET**: Retrieve all categories  
  `GET http://localhost:8080/categories`
- **GET by ID**: Retrieve a specific category  
  `GET http://localhost:8080/categories/{id}`
- **POST**: Create a new category  
  `POST http://localhost:8080/categories`
- **PUT**: Update a category  
  `PUT http://localhost:8080/categories/{id}`
- **DELETE**: Delete a category  
  `DELETE http://localhost:8080/categories/{id}`

---

### **Products**
- **GET**: Retrieve all products or filter by criteria  
  `GET http://localhost:8080/products`  
  **Query Parameters**:
    - `cat` (int): Filter by category ID
    - `minPrice` (decimal): Minimum price
    - `maxPrice` (decimal): Maximum price
    - `color` (string): Filter by color
- **GET by ID**: Retrieve a specific product  
  `GET http://localhost:8080/products/{id}`
- **POST**: Create a new product  
  `POST http://localhost:8080/products`
- **PUT**: Update a product  
  `PUT http://localhost:8080/products/{id}`
- **DELETE**: Delete a product  
  `DELETE http://localhost:8080/products/{id}`

---

### **Shopping Cart**
- **GET**: View cart items  
  `GET http://localhost:8080/cart`
- **POST**: Add a product to the cart  
  `POST http://localhost:8080/cart/products/{productId}`
- **PUT**: Update product quantity in the cart  
  `PUT http://localhost:8080/cart/products/{productId}`  
  **Request Body**:
  ```json
  {
    "quantity": 3
  }
  ```
- **DELETE**: Clear cart  
  `DELETE http://localhost:8080/cart`
- **DELETE**: Delete item in cart  
    `DELETE http://localhost:8080/cart/products/{productId}`

---

### **Orders**
- **POST**: Place an order  
  `POST http://localhost:8080/orders`

---

### **Profile**
- **GET**: Retrieve user profile  
  `GET http://localhost:8080/profile`
- **PUT**: Update user profile  
  `PUT http://localhost:8080/profile`

---

## Setup

1. Clone the repository.
2. Create and initialize the database using `create_database.sql`.
3. Configure application properties.
4. Run the application on `http://localhost:8080`.


  
