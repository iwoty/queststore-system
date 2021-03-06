package pl.coderampart.DAO;

import pl.coderampart.model.Artifact;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ArtifactDAO extends AbstractDAO {

    private  Connection connection;

    public ArtifactDAO(Connection connectionToDB) {
        connection = connectionToDB;
    }

    public Artifact getByID(String ID) throws SQLException {
        Artifact artifact;
        String query = "SELECT * FROM artifacts WHERE id = ?;";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, ID);
        ResultSet resultSet = statement.executeQuery();

        artifact = this.createArtifactFromResultSet(resultSet);

        return artifact;
    }

    public List<Artifact> readAll() throws SQLException {
        List<Artifact> artifactList = new ArrayList<>();
        String query = "SELECT * FROM artifacts;";
        PreparedStatement statement = connection.prepareStatement(query);
        ResultSet resultSet = statement.executeQuery();

        while (resultSet.next()) {
            Artifact artifact = this.createArtifactFromResultSet(resultSet);
            artifactList.add(artifact);
        }
        return artifactList;
    }

    public Artifact getByName(String name) throws SQLException {
        Artifact artifact;

        String query = "SELECT * FROM artifacts WHERE name = ?;";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, name);
        ResultSet resultSet = statement.executeQuery();

        return this.createArtifactFromResultSet(resultSet);
    }

    public void create(Artifact artifact) throws SQLException{
        String query = "INSERT INTO artifacts (name, description, type, value, id) VALUES (?, ?, ?, ?, ?);";
        PreparedStatement statement = connection.prepareStatement(query);
        setPreparedStatement(statement, artifact);
        statement.executeUpdate();
    }

    public void update(Artifact artifact) throws SQLException {
        String query = "UPDATE artifacts SET name = ?, description = ?, type = ?, value = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(query);
        setPreparedStatement(statement, artifact);
        statement.executeUpdate();
    }

    public void delete(Artifact artifact) throws SQLException {
        String query = "DELETE FROM artifacts WHERE id = ?;";
        PreparedStatement statement = connection.prepareStatement(query);
        statement.setString(1, artifact.getID());
        statement.executeUpdate();
    }

    private void setPreparedStatement(PreparedStatement statement, Artifact artifact) throws SQLException {
        statement.setString(1, artifact.getName());
        statement.setString(2, artifact.getDescription());
        statement.setString(3, artifact.getType());
        statement.setInt(4, artifact.getValue());
        statement.setString(5, artifact.getID());
    }

    private Artifact createArtifactFromResultSet(ResultSet resultSet) throws SQLException {
        String ID = resultSet.getString("id");
        String name = resultSet.getString("name");
        String description = resultSet.getString("description");
        String type = resultSet.getString("type");
        Integer value = resultSet.getInt("value");

        return new Artifact(ID, name, description, type, value);
    }
}