package it.polimi.tiw.tiwproject2024purehtml.controllers;

import it.polimi.tiw.tiwproject2024purehtml.beans.DettaglioIscrizioneStudente;
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
import java.util.List;
import java.util.Optional;

@WebServlet("/docente/GoToIscrittiAppello")
public class GoToIscrittiAppello extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection =  null;
    private TemplateEngine templateEngine;

    public GoToIscrittiAppello() {
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

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();
        int idAppello;
        try {
            idAppello = Integer.parseInt(request.getParameter("idAppello"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }
        String sortBy = Optional.ofNullable(request.getParameter("sortBy")).orElse("cognome");
        String sortOrder = Optional.ofNullable(request.getParameter("sortOrder")).orElse("asc");
        String nextSortOrder = "asc".equals(sortOrder) ? "desc" : "asc";

        // Whitelist per la sicurezza
        List<String> allowedFields = List.of("matricola", "cognome", "nome", "email", "corso_laurea", "voto", "stato");
        if (!allowedFields.contains(sortBy)) sortBy = "cognome";
        if (!List.of("asc", "desc").contains(sortOrder)) sortOrder = "asc";

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

        IscrizioneAppelloDAO dao  = new IscrizioneAppelloDAO(connection);
        List<DettaglioIscrizioneStudente> iscritti;
        try {
            iscritti = dao.getIscrittiByAppelloSorted(idAppello, sortBy, sortOrder);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }

        boolean pubblicabile = false;
        try {
            pubblicabile = dao.checkVotiInseriti(idAppello);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }

        boolean verbalizzabile =  false;
        try {
            verbalizzabile = dao.checkVerbalizzabile(idAppello);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }

        String path = "IscrittiAppello.html";
        IWebExchange webExchange = JakartaServletWebApplication
                .buildApplication(getServletContext())
                .buildExchange(request, response);

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        String successMessage = (String) session.getAttribute("success");
        if (successMessage != null) {
            ctx.setVariable("success", successMessage);
            session.removeAttribute("success");
        }
        ctx.setVariable("iscritti", iscritti);
        ctx.setVariable("idAppello", idAppello);
        ctx.setVariable("sortBy", sortBy);
        ctx.setVariable("sortOrder", sortOrder);
        ctx.setVariable("nextSortOrder", nextSortOrder);
        ctx.setVariable("pubblicabile", pubblicabile);
        ctx.setVariable("verbalizzabile", verbalizzabile);

        templateEngine.process(path, ctx, response.getWriter());

    }
}
