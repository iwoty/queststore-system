package pl.coderampart.controller.admin;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import pl.coderampart.DAO.GroupDAO;
import pl.coderampart.DAO.MentorDAO;
import pl.coderampart.controller.helpers.AccessValidator;
import pl.coderampart.controller.helpers.MailSender;
import pl.coderampart.controller.helpers.PasswordHasher;
import pl.coderampart.controller.helpers.FlashNoteHelper;
import pl.coderampart.controller.helpers.HelperController;
import pl.coderampart.model.Group;
import pl.coderampart.model.Mentor;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Map;

public class CreateMentorController extends AccessValidator implements HttpHandler {

    private Connection connection;
    private MentorDAO mentorDAO;
    private HelperController helper;
    private FlashNoteHelper flashNoteHelper;
    private PasswordHasher hasher;
    private GroupDAO groupDAO;
    private MailSender mailSender;

    public CreateMentorController(Connection connection) {
        this.connection = connection;
        this.mentorDAO = new MentorDAO(connection);
        this.groupDAO = new GroupDAO(connection);
        this.helper = new HelperController(connection);
        this.flashNoteHelper = new FlashNoteHelper();
        this.hasher = new PasswordHasher();
        this.mailSender = new MailSender();
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        validateAccess( "Admin", httpExchange, connection);
        String method = httpExchange.getRequestMethod();
        String response = "";

        if (method.equals("GET")) {
            response += helper.renderHeader( httpExchange, connection );
            response += helper.render( "admin/adminMenu" );
            response += helper.renderWithDropdownGroups("admin/createMentor");
            response += helper.render( "footer" );

            helper.sendResponse( response, httpExchange );
        }

        if (method.equals("POST")) {
            Map<String, String> inputs = helper.getInputsMap(httpExchange);

            createMentor(inputs, httpExchange);
            helper.redirectTo( "/mentor/create", httpExchange );
        }
    }

    private void createMentor(Map<String, String> inputs, HttpExchange httpExchange) {
        String firstName = inputs.get("first-name");
        String lastName = inputs.get("last-name");
        String dateOfBirth = inputs.get("date-of-birth");
        String email = inputs.get("email");
        String groupName = inputs.get("group");
        LocalDate dateOfBirthObject = LocalDate.parse(dateOfBirth);

        try {
            String generatedPassword = helper.generateRandomPassword();
            String hashedPassword = hasher.generateStrongPasswordHash( generatedPassword );
            Group group = groupDAO.getByName( groupName );

            Mentor newMentor = new Mentor( firstName, lastName, dateOfBirthObject, email, hashedPassword );
            newMentor.setGroup( group );
            mentorDAO.create( newMentor );

            String initialMessage = mailSender.prepareMessage( firstName, generatedPassword );
            mailSender.send( email, initialMessage );

            String mentorFullName = firstName + " " + lastName;
            String flashNote = flashNoteHelper.createCreationFlashNote( "Mentor", mentorFullName );
            flashNoteHelper.addSuccessFlashNoteToCookie(flashNote, httpExchange);
        } catch (NoSuchAlgorithmException | SQLException | InvalidKeySpecException e) {
            flashNoteHelper.addFailureFlashNoteToCookie(httpExchange);
            e.printStackTrace();
        }
    }
}