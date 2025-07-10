package it.polimi.tiw.tiwproject2024purehtml.controllers;


import it.polimi.tiw.tiwproject2024purehtml.beans.Utente;
import it.polimi.tiw.tiwproject2024purehtml.beans.Voto;
import it.polimi.tiw.tiwproject2024purehtml.dao.AppelloDAO;
import it.polimi.tiw.tiwproject2024purehtml.utility.ConnectionHandler;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringEscapeUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/docente/UpdateVoto")
public class UpdateVoto extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection =  null;
    private TemplateEngine templateEngine;

    public UpdateVoto() {
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
        int matricola;
        int idAppello;
        String votoParam;

        //check formato parametri
        try {
            matricola = Integer.parseInt(request.getParameter("matricola"));
            idAppello = Integer.parseInt(request.getParameter("idAppello"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }
        votoParam = StringEscapeUtils.escapeJava(request.getParameter("voto"));
        if(votoParam == null || votoParam.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }
        Voto voto;
        try {
            voto = Voto.fromString(votoParam);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid vote value");
            return;
        }

        AppelloDAO appelloDAO = new AppelloDAO(connection);
        Utente utente = (Utente) session.getAttribute("utente");
        IWebExchange webExchange = JakartaServletWebApplication
                .buildApplication(getServletContext())
                .buildExchange(request, response);

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        try {
            if (appelloDAO.checkAppelloByDocente(utente.getId(), idAppello)) {
                if (appelloDAO.inserisciVoto(idAppello, matricola, voto) == 0) {
                    String path = "ErrorPage.html";
                    ctx.setVariable("error", "MODIFICA NON CONCESSA");
                    ctx.setVariable("description", "Hai tentato di modificare un voto pubblicato, rifiutato o verbalizzato!");
                    templateEngine.process(path, ctx, response.getWriter());
                    session.invalidate();
                    return;
                }
            } else {
                String path = "ErrorPage.html";
                ctx.setVariable("error", "ACCESSO NON AUTORIZZATO");
                ctx.setVariable("description", "Hai tentato di accedere ad una risorsa non tua!");
                templateEngine.process(path, ctx, response.getWriter());
                session.invalidate();
                return;
            }
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "DB Error");
            return;
        }

        String path = getServletContext().getContextPath();
        response.sendRedirect(path + "/docente/GoToModifica?idAppello=" + idAppello + "&matricola=" + matricola);

    }
}
