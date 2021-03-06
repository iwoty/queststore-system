package pl.coderampart.controller.mentor;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import pl.coderampart.DAO.TeamDAO;
import pl.coderampart.controller.helpers.AccessValidator;
import pl.coderampart.controller.helpers.FlashNoteHelper;
import pl.coderampart.controller.helpers.HelperController;
import pl.coderampart.model.Group;
import pl.coderampart.model.Team;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class EditTeamController extends AccessValidator implements HttpHandler {

    private Connection connection;
    private TeamDAO teamDAO;
    private HelperController helper;
    private FlashNoteHelper flashNoteHelper;


    public EditTeamController(Connection connection) {
        this.connection = connection;
        this.teamDAO = new TeamDAO(connection);
        this.helper = new HelperController(connection);
        this.flashNoteHelper = new FlashNoteHelper();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        validateAccess( "Mentor", httpExchange, connection);
        String method = httpExchange.getRequestMethod();
        List<Team> allTeams = helper.readTeamsFromDB();
        String teamID = helper.getIdFromURI( httpExchange );
        Team team = helper.getTeamById( teamID );

        if(method.equals("GET")) {
            String response = "";
            response += helper.renderHeader(httpExchange, connection);
            response += helper.render("mentor/mentorMenu");
            response += renderProperBodyResponse(teamID, allTeams);
            response += helper.render("footer");

            helper.sendResponse( response, httpExchange );
        }

        if(method.equals("POST")) {
            Map inputs = helper.getInputsMap(httpExchange);
            editTeam(inputs, team, httpExchange);
            helper.redirectTo( "/team/edit", httpExchange );
        }
    }

    private String renderProperBodyResponse(String teamID, List<Team> allTeams) {
        Integer idLength = 36;
        if(teamID.length() == idLength) {
            Team teamToEdit = helper.getTeamById(teamID);
            return renderEditTeam(teamToEdit, allTeams);
        } else {
            return renderTeamEmptyForm(allTeams);
        }
    }

    private String renderEditTeam(Team team, List<Team> allTeams) {
        List<Group> allGroups = helper.readGroupsFromDB();
        String templatePath = "templates/mentor/editTeam.twig";
        JtwigTemplate template = JtwigTemplate.classpathTemplate( templatePath );
        JtwigModel model = JtwigModel.newModel();

        model.with("allTeams", allTeams);
        model.with("teamName", team.getName());
        model.with("groupName", team.getGroup().getName());
        model.with("allGroups", allGroups);

        return template.render(model);
    }

    private String renderTeamEmptyForm(List<Team> allTeams) {
        String templatePath = "templates/mentor/editTeam.twig";
        JtwigTemplate template = JtwigTemplate.classpathTemplate( templatePath );
        JtwigModel model = JtwigModel.newModel();

        model.with("allTeams", allTeams);

        return template.render(model);
    }

    private void editTeam(Map<String, String> inputs, Team team, HttpExchange httpExchange) {
        String name = inputs.get("team-name");

        try {
            team.setName(name);
            teamDAO.update( team );

            String flashNote = flashNoteHelper.createEditionFlashNote( "Team", name );
            flashNoteHelper.addSuccessFlashNoteToCookie(flashNote, httpExchange);
        } catch (SQLException e) {
            flashNoteHelper.addFailureFlashNoteToCookie(httpExchange);
            e.printStackTrace();
        }
    }
}