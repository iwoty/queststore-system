package pl.coderampart.DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import pl.coderampart.model.*;
import pl.coderampart.services.User;

public class AdminDAO extends AbstractDAO implements User<Admin> {


    private Connection connection;

    public AdminDAO() {

        try {

            connection = super.getInstance();
        }catch (Exception e) {
            System.err.println( e.getClass().getName() + ": " + e.getMessage() );
        }
    }

    public Admin getLogged(String email, String password) throws SQLException {
        Admin admin = null;

        connection = this.connectToDataBase();
        String query = "SELECT * FROM admins WHERE email = ? AND password = ?;";

        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, email);
        statement.setString(2, password);
        ResultSet resultSet = statement.executeQuery();

        admin = this.createAdminFromResultSet(resultSet);
        connection.close();

        return admin;
    }

    public ArrayList<Admin> readAll() throws SQLException{
        ArrayList<Admin> adminList = new ArrayList<>();

        Connection connection = this.connectToDataBase();
        String query = "SELECT * FROM admins;";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Admin admin = this.createAdminFromResultSet(resultSet);
            adminList.add(admin);
        }
        connection.close();

        return adminList;
    }

    public void create(Admin admin) throws SQLException {

        Connection connection = this.connectToDataBase();
        String query = "INSERT INTO admins VALUES (?, ?, ?, ?, ?, ?);";
        PreparedStatement statement = connection.prepareStatement(query);
        PreparedStatement setStatement = setPreparedStatement(statement, admin);
        statement.executeUpdate();

        connection.close();
    }

    public void update(Admin admin) throws SQLException{

        Connection connection = this.connectToDataBase();
        String query = "UPDATE admins SET id = ?, first_name = ?, " +
                       "last_name = ?, email = ?, password = ?, " +
                       "date_of_birth = ?;";

        PreparedStatement statement = connection.prepareStatement(query);
        PreparedStatement setStatement = setPreparedStatement(statement, admin);
        setStatement.executeUpdate();

        connection.close();

    }

    public void delete(Admin admin) throws SQLException{

        Connection connection = this.connectToDataBase();
        String query = "DELETE FROM admins WHERE id = ?;";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, admin.getID());
        statement.executeUpdate();

        connection.close();
    }

    private PreparedStatement setPreparedStatement(PreparedStatement statement, Admin admin) throws SQLException {
        statement.setString(1, admin.getID());
        statement.setString(2, admin.getFirstName());
        statement.setString(3, admin.getLastName());
        statement.setString(4, admin.getEmail());
        statement.setString(5, admin.getPassword());
        statement.setString(6, admin.getDateOfBirth().toString());

        return statement;
    }

    private Admin createAdminFromResultSet(ResultSet resultSet) throws SQLException {
        String ID = resultSet.getString("id");
        String firstName = resultSet.getString("first_name");
        String lastName = resultSet.getString("last_name");
        String email = resultSet.getString("email");
        String password = resultSet.getString("password");
        String dateOfBirth = resultSet.getString("date_of_birth");
        LocalDate dateOfBirthObject = LocalDate.parse(dateOfBirth);

        return new Admin(ID, firstName, lastName, dateOfBirthObject,email, password);
    }
}