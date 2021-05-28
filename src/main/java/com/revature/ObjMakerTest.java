package com.revature;

import com.revature.models.Person;
import com.revature.util.factory.ConnectionFactory;
import com.revature.util.querinator.GenericObjectMaker;
import com.revature.util.querinator.PostgresQueryBuilder;

import java.sql.SQLException;

/**
 * Created by IntelliJ IDEA.
 * User: Jbialon
 * Date: 5/25/2021
 * Time: 5:00 PM
 * Description: {Insert Description}
 */
public class ObjMakerTest {

    public static void main(String[] args) throws SQLException, IllegalAccessException, InstantiationException {

        final PostgresQueryBuilder qBuild = new PostgresQueryBuilder(ConnectionFactory.getInstance().getConnection());
        final GenericObjectMaker oBuild = new GenericObjectMaker();

        Person test = new Person();

        test.setId(6);

        test = (Person) oBuild.buildObject(Person.class, qBuild.selectByPrimaryKey(test));

        System.out.println(test.toString());
        System.out.println();

        test = new Person();

        test.setEmail("test.person@test.org");
        test.setPassword("password");

        test = (Person) oBuild.buildObject(Person.class, qBuild.loginByEmail(test));

        System.out.println(test.toString());
        System.out.println();

        test = new Person();

        test.setUsername("test_person");
        test.setPassword("password");

        test = (Person) oBuild.buildObject(Person.class, qBuild.loginByUsername(test));

        System.out.println(test.toString());

    }

}
