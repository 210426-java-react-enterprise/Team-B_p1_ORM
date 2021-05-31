package com.revature.util.crud;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayDeque;

/**
 * Created by IntelliJ IDEA.
 * User: Jbialon
 * Date: 5/15/2021
 * Time: 5:00 PM
 * Description: {Insert Description}
 */
public class UpdateBasedQueries {

    Connection conn;

    public UpdateBasedQueries(Connection conn){ this.conn = conn; }

    /**
     *
     * Description: Updates a record by primary key
     *
     * @param tableName
     * @param pkInfo
     * @param queryColumns
     * @param queryValues
     * @return
     * @throws SQLException
     */
    public boolean buildUpdateQueryString(String tableName, Object[] pkInfo, ArrayDeque<String> queryColumns, ArrayDeque<Object> queryValues) throws SQLException {

        int paramCounter = 1;

        // Return value
        String query = "update " + tableName + " set ";

        Object lastColumnValue = queryColumns.peekLast();

        // While we still have column data in our deque...
        while (!queryColumns.isEmpty()) {

            if (!queryColumns.peek().equals(lastColumnValue)) {

                query = query + queryColumns.poll() + " = ?, ";

            } else {

                query = query + queryColumns.poll() + " = ? ";

            }

        }

        query = query + "where " + pkInfo[0] + " = ?;";

        PreparedStatement pstmt = conn.prepareStatement(query);

        for (Object value : queryValues) {

            pstmt.setObject(paramCounter, queryValues.poll());

            paramCounter++;

        }

        pstmt.setObject(paramCounter, pkInfo[1]);

        System.out.println(pstmt);

        int updatedRows = pstmt.executeUpdate();

        if (updatedRows != 0) {
            return true;
        }

        //if (pstmt != null) pstmt.close();
        //if (conn != null) conn.close();

        return false;

    }

}
