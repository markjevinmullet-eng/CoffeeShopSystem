package com.scc.dao;

import com.scc.dao.DatabaseConnection;
import java.sql.*;

public class OrderDao {

    public boolean createOrder(int cashierId, int customerId, int productId, int quantity, double subtotal) {
        String insertOrder = "INSERT INTO orders (cashier_id, customer_id, total_amount) VALUES (?, ?, ?)";
        String insertDetail = "INSERT INTO order_details (order_id, product_id, quantity, subtotal) VALUES (?, ?, ?, ?)";
        String updateStock = "UPDATE products SET stock_quantity = stock_quantity - ? WHERE product_id = ?";

        Connection conn = null;
        try {
            conn = DatabaseConnection.connect();
            if (conn == null) return false;

            conn.setAutoCommit(false); // Transaction start

            int orderId;
            try (PreparedStatement stmtOrder = conn.prepareStatement(insertOrder, Statement.RETURN_GENERATED_KEYS)) {
                if (cashierId > 0) {
                    stmtOrder.setInt(1, cashierId);
                } else {
                    stmtOrder.setNull(1, Types.INTEGER);
                }

                if (customerId > 0) {
                    stmtOrder.setInt(2, customerId);
                } else {
                    stmtOrder.setNull(2, Types.INTEGER);
                }

                stmtOrder.setDouble(3, subtotal);
                stmtOrder.executeUpdate();

                try (ResultSet generatedKeys = stmtOrder.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        orderId = generatedKeys.getInt(1);
                    } else {
                        conn.rollback();
                        return false;
                    }
                }
            }

            try (PreparedStatement stmtDetail = conn.prepareStatement(insertDetail)) {
                stmtDetail.setInt(1, orderId);
                stmtDetail.setInt(2, productId);
                stmtDetail.setInt(3, quantity);
                stmtDetail.setDouble(4, subtotal);
                stmtDetail.executeUpdate();
            }

            try (PreparedStatement stmtStock = conn.prepareStatement(updateStock)) {
                stmtStock.setInt(1, quantity);
                stmtStock.setInt(2, productId);
                stmtStock.executeUpdate();
            }

            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            System.out.println("❌ Order transaction failed: " + e.getMessage());
            return false;
        } finally {
            if (conn != null) {
                try { conn.setAutoCommit(true); conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }
}