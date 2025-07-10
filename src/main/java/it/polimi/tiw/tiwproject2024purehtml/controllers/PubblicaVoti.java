package it.polimi.tiw.tiwproject2024purehtml.controllers;

import it.polimi.tiw.tiwproject2024purehtml.beans.Utente;
import it.polimi.tiw.tiwproject2024purehtml.dao.AppelloDAO;
import it.polimi.tiw.tiwproject2024purehtml.dao.IscrizioneAppelloDAO;
import it.polimi.tiw.tiwproject2024purehtml.utility.ConnectionHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/docente/PubblicaVoti")
public class PubblicaVoti extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection =  null;
    private TemplateEngine templateEngine;

    public PubblicaVoti() {
        super();
        // TODO Auto-generated constructor stub
    }

    public void init() throws ServletException {
        connection = ConnectionHandler.getConnection(getServletContext());
        JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(getServletContext());
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/WEB-INF/");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false); // utile in fase di sviluppo

        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        int idAppello;
        try {
            idAppello = Integer.parseInt(request.getParameter("idAppello"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }

        //check accesso a risorsa altrui
        AppelloDAO appelloDAO = new AppelloDAO(connection);
        Utente utente = (Utente) session.getAttribute("utente");

        try {
            if (!appelloDAO.checkAppelloByDocente(utente.getId(), idAppello)) {
                String path = "ErrorPage.html";
                IWebExchange webExchange = JakartaServletWebApplication
                        .buildApplication(getServletContext())
                        .buildExchange(request, response);

                WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
                ctx.setVariable("error", "ACCESSO NON AUTORIZZATO");
                ctx.setVariable("description", "Hai tentato di accedere ad una risorsa non tua!");
                templateEngine.process(path, ctx, response.getWriter());
                session.invalidate();
                return;
            }
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect param values");
            return;
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DB Error");
            return;
        }

        IWebExchange webExchange = JakartaServletWebApplication
                .buildApplication(getServletContext())
                .buildExchange(request, response);

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        IscrizioneAppelloDAO iaDAO = new IscrizioneAppelloDAO(connection);
        int righeModificate = 0;
        try {
            if (iaDAO.checkVotiInseriti(idAppello)) {
                righeModificate = iaDAO.pubblicaVoti(idAppello);
            } else {
                String path = "WarningPage.html";
                ctx.setVariable("error", "Non è possibile pubblicare il voto.");
                ctx.setVariable("description", "Nessun voto ha stato 'INSERITO'");
                templateEngine.process(path, ctx, response.getWriter());
                return;
            }
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }

        if (righeModificate > 0) {
            session.setAttribute("success", righeModificate + " voti pubblicati con successo");
        } else {
            session.setAttribute("success", "Nessun voto da pubblicare");
        }

        request.getSession().setAttribute("success", righeModificate + " voti pubblicati con successo");

        response.sendRedirect(request.getContextPath() + "/docente/GoToIscrittiAppello?idAppello=" + idAppello);

    }
}
