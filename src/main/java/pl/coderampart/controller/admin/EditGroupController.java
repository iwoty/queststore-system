package pl.coderampart.controller.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import org.jtwig.JtwigModel;
import org.jtwig.JtwigTemplate;
import pl.coderampart.DAO.GroupDAO;
import pl.coderampart.controller.helpers.AccessValidator;
import pl.coderampart.controller.helpers.FlashNoteHelper;
import pl.coderampart.controller.helpers.HelperController;
import pl.coderampart.model.Group;

import java.io.*;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

public class EditGroupController extends AccessValidator implements HttpHandler{

    private Connection connection;
    private GroupDAO groupDAO;
    private HelperController helper;
    private FlashNoteHelper flashNoteHelper;

    public EditGroupController(Connection connection){
        this.connection = connection;
        this.groupDAO = new GroupDAO(connection);
        this.helper = new HelperController(connection);
        this.flashNoteHelper = new FlashNoteHelper();

    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        validateAccess( "Admin", httpExchange, connection);
        String method = httpExchange.getRequestMethod();
        List<Group> allGroups = helper.readGroupsFromDB();
        String groupID = helper.getIdFromURI( httpExchange );
        Group group = helper.getGroupById( groupID );

        if(method.equals("GET")) {
            String response = "";
            response += helper.renderHeader(httpExchange, connection);
            response += helper.render("admin/adminMenu");
            response += renderProperBodyResponse(groupID, allGroups);
            response += helper.render("footer");

            helper.sendResponse( response, httpExchange );
        }

        if(method.equals("POST")) {
            Map inputs = helper.getInputsMap(httpExchange);
            editGroup(inputs, group, httpExchange);
            helper.redirectTo( "/group/edit", httpExchange );
        }

    }

    private String renderProperBodyResponse(String groupID, List<Group> allGroups) {
        Integer idLength = 36;
        if (groupID.length() == idLength) {
            Group levelToEdit = helper.getGroupById( groupID );
            return renderEditGroup(levelToEdit, allGroups);
        } else {
            return renderGroupEmptyForm(allGroups);
        }
    }

    private String renderEditGroup(Group group, List<Group> allGroups) {
        String templatePath = "templates/admin/editGroup.twig";
        JtwigTemplate template = JtwigTemplate.classpathTemplate( templatePath );
        JtwigModel model = JtwigModel.newModel();

        model.with("allGroups", allGroups);
        model.with("groupName", group.getName());

        return template.render(model);
    }

    private String renderGroupEmptyForm(List<Group> allGroups) {
        String templatePath = "templates/admin/editGroup.twig";
        JtwigTemplate template = JtwigTemplate.classpathTemplate( templatePath );
        JtwigModel model = JtwigModel.newModel();

        model.with("allGroups", allGroups);

        return template.render(model);
    }

    private void editGroup(Map<String, String> inputs, Group group, HttpExchange httpExchange) {
        String name = inputs.get("group-name");

        try {
            group.setName( name );
            groupDAO.update( group );

            String flashNote = flashNoteHelper.createEditionFlashNote( "Group", name );
            flashNoteHelper.addSuccessFlashNoteToCookie(flashNote, httpExchange);
        } catch (SQLException e) {
            flashNoteHelper.addFailureFlashNoteToCookie(httpExchange);
            e.printStackTrace();
        }
    }
}
