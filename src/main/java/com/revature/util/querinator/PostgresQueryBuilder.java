package com.revature.util.querinator;

import com.revature.exceptions.AnnotationNotFound;
import com.revature.exceptions.InvalidInput;
import com.revature.util.annotation.*;
import com.revature.util.querinator.annotationhelper.AnnotationGetters;
import com.revature.util.querinator.crud.CreateBasedQueries;
import com.revature.util.querinator.crud.DeleteBasedQueries;
import com.revature.util.querinator.crud.ReadBasedQueries;
import com.revature.util.querinator.crud.UpdateBasedQueries;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created by IntelliJ IDEA.
 * User: James Bialon
 * Date: 5/14/2021
 * Time: 5:33 PM
 * Description: Dynamically build queries to interact with a postgres database
 */
public class PostgresQueryBuilder<T> {

    Connection conn;

    public PostgresQueryBuilder(Connection conn){ this.conn = conn; };

    /**
     *
     * Description: Wraps everything nicely into a switch for multiple query types.
     *
     * @param obj
     * @return query
     * @throws IllegalAccessException
     * @throws InvalidInput
     * @throws AnnotationNotFound
     */

    public boolean insert (T obj) throws IllegalAccessException, AnnotationNotFound, SQLException {
        return sendQuery(obj, "insert");
    }

    public boolean update (T obj) throws IllegalAccessException, AnnotationNotFound, SQLException {
        return sendQuery(obj, "update");
    }

    public boolean delete (T obj) throws IllegalAccessException, AnnotationNotFound, SQLException {
        return sendQuery(obj, "delete");
    }

    public ResultSet selectByPrimaryKey (T obj) throws IllegalAccessException, AnnotationNotFound, SQLException {
        return buildQuery(obj, "select_by_pk");
    }

    public ResultSet loginByUsername (T obj) throws IllegalAccessException, AnnotationNotFound, SQLException {
        return buildQuery(obj, "login_username");
    }

    public ResultSet loginByEmail (T obj) throws IllegalAccessException, AnnotationNotFound, SQLException {
        return buildQuery(obj, "login_email");
    }

    public ResultSet loginByEmailPgCrypt (T obj) throws IllegalAccessException, AnnotationNotFound, SQLException {
        return buildQuery(obj, "pgCrypt_login_email");
    }

    public ResultSet loginByUsernamePgCrypt (T obj) throws IllegalAccessException, AnnotationNotFound, SQLException {
        return buildQuery(obj, "pgCrypt_login_username");
    }

    /**
     *
     * @param obj
     * @param fkInfo [0] = foreign key column name [1] = value
     * @return
     * @throws IllegalAccessException
     * @throws AnnotationNotFound
     * @throws SQLException
     */
    public ResultSet getObjectByForeignKey (T obj, Object[] fkInfo) throws IllegalAccessException, AnnotationNotFound, SQLException {

        ReadBasedQueries selectMaker = new ReadBasedQueries(conn);

        AnnotationGetters annoGetter = new AnnotationGetters();

        // Holds the table name related to our POJO
        String tableName = annoGetter.getTableName(obj);

        return selectMaker.buildSelectAllByFK(tableName, fkInfo);
    }

    public boolean sendQuery(T obj, String queryType) throws IllegalAccessException, InvalidInput, AnnotationNotFound, SQLException {

        // TODO: Maybe turn this into an ENUM?
        // Set of valid queryType entries
        //Set<String> validQueryTypes = Stream.of("insert", "update", "delete")
        //                                    .collect(Collectors.toCollection(HashSet::new));

        // Ensures a good entry for query type
        //if (!validQueryTypes.contains(queryType)) { throw new InvalidInput("Bad query type value!"); }

        // Ensures the pojo is supposed to be persisted to a table
        if (!obj.getClass().isAnnotationPresent(Entity.class)) { throw new AnnotationNotFound("Entity annotation not found!!"); }

        AnnotationGetters annoGetter = new AnnotationGetters();

        // Holds the table name related to our POJO
        String tableName = annoGetter.getTableName(obj);

        // Get the queries column names
        ArrayDeque<String> queryColumns = annoGetter.getColumnNames(obj);

        // Get the queries values
        ArrayDeque<Object> queryValues = annoGetter.getValues(obj);

        // Primary key info [0] will be the pk column name [1] will be the key itself
        Object[] pkInfo = annoGetter.getPrimaryKey(obj);

        // Query Builders
        CreateBasedQueries createGenerator;
        UpdateBasedQueries updateGenerator;
        DeleteBasedQueries deleteGenerator;

        // The return value
        boolean query = false;

        switch (queryType) {

            case "insert":

                createGenerator = new CreateBasedQueries(conn);

                // We have our dynamic/generic query :)
                query = createGenerator.buildInsertQueryString(tableName, queryColumns, queryValues);
                break;

            case "update":

                updateGenerator = new UpdateBasedQueries(conn);

                query = updateGenerator.buildUpdateQueryString(tableName, pkInfo, queryColumns, queryValues);
                break;

            case "delete":

                deleteGenerator = new DeleteBasedQueries(conn);

                query = deleteGenerator.buildDeleteByPK(tableName, pkInfo);

                break;

        }

        return query;

    }

    public ResultSet buildQuery(T obj, String queryType) throws IllegalAccessException, InvalidInput, AnnotationNotFound, SQLException {

        // TODO: Maybe turn this into an ENUM?
        // Set of valid queryType entries
        //Set<String> validQueryTypes = Stream.of("select_by_pk", "login_username", "login_email", "pgCrypt_login_email", "pgCrypt_login_username")
        //        .collect(Collectors.toCollection(HashSet::new));

        // Ensures a good entry for query type
        //if (!validQueryTypes.contains(queryType)) { throw new InvalidInput("Bad query type value!"); }

        // Ensures the pojo is supposed to be persisted to a table
        if (!obj.getClass().isAnnotationPresent(Entity.class)) { throw new AnnotationNotFound("Entity annotation not found!!"); }

        AnnotationGetters annoGetter = new AnnotationGetters();

        // Holds the table name related to our POJO
        String tableName = annoGetter.getTableName(obj);

        // Primary key info [0] will be the pk column name [1] will be the key itself
        Object[] pkInfo = annoGetter.getPrimaryKey(obj);

        /*
            Will be needed for login functions within switch
            Email or Username column will be [0][0] and their values will be [0][1]
            Password column will be [1][0] and the value will be [1][1]
         */
        Object[][] loginInfo;

        // Query Builders
        ReadBasedQueries readGenerator;

        // The return value
        ResultSet rs = null;

        switch (queryType) {

            case "select_by_pk":

                readGenerator = new ReadBasedQueries(conn);

                rs = readGenerator.buildSelectAllByPK(tableName, pkInfo);

                break;


            case "login_username":

                readGenerator = new ReadBasedQueries(conn);

                loginInfo = annoGetter.getLoginInfoByUsername(obj);

                rs = readGenerator.buildLogin(tableName, loginInfo);

                break;

            case "login_email":

                readGenerator = new ReadBasedQueries(conn);

                loginInfo = annoGetter.getLoginInfoByEmail(obj);

                rs = readGenerator.buildLogin(tableName, loginInfo);

                break;

            case "pgCrypt_login_email":

                readGenerator = new ReadBasedQueries(conn);

                loginInfo = annoGetter.getLoginInfoByEmail(obj);

                rs = readGenerator.buildGetDecryptedPgEncryptedPass(tableName, loginInfo);

                break;

            case "pgCrypt_login_username":

                readGenerator = new ReadBasedQueries(conn);

                loginInfo = annoGetter.getLoginInfoByUsername(obj);

                rs = readGenerator.buildGetDecryptedPgEncryptedPass(tableName, loginInfo);

                break;
        }

        return rs;

    }

}
