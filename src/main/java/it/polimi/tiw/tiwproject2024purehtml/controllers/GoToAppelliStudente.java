package it.polimi.tiw.tiwproject2024purehtml.controllers;

import it.polimi.tiw.tiwproject2024purehtml.beans.Appello;
import it.polimi.tiw.tiwproject2024purehtml.beans.Corso;
import it.polimi.tiw.tiwproject2024purehtml.beans.Utente;
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
import java.util.List;

@WebServlet("/GoToAppelliStudente")
public class GoToAppelliStudente extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection =  null;
    private TemplateEngine templateEngine;

    public GoToAppelliStudente() {
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
        if (session.isNew() || session.getAttribute("utente") == null) {
            String path = getServletContext().getContextPath() + "/login.html";
            response.sendRedirect(path);
            return;
        }

        Utente utente = (Utente) session.getAttribute("utente");
        String nomeCorso = null;
        int idCorso;

        //controllo parametri
        try {
            idCorso = Integer.parseInt(request.getParameter("idCorso"));
        } catch (NumberFormatException | NullPointerException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }
        nomeCorso = StringEscapeUtils.escapeJava(request.getParameter("nomeCorso"));
        if(nomeCorso == null || nomeCorso.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Incorrect or missing param values");
            return;
        }
        /*
        //controllo accessi non autorizzati
        List<Corso> corsi = (List<Corso>) request.getSession().getAttribute("corsi");

        boolean autorizzato = corsi.stream().anyMatch(c -> c.getIdCorso() == idCorso);

        if (!autorizzato) {
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
         */

        AppelloDAO  appelloDAO = new AppelloDAO(connection);
        List<Appello> appelli;

        try {
            appelli = appelloDAO.getAppellibyCorsoPerStudenti(utente.getId(), idCorso);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, errorMessage);
            return;
        }

        if (!appelli.isEmpty()) {
            String path = "AppelliStudente.html";
            IWebExchange webExchange = JakartaServletWebApplication
                    .buildApplication(getServletContext())
                    .buildExchange(request, response);

            WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
            ctx.setVariable("nomeCorso", nomeCorso);
            ctx.setVariable("idCorso", idCorso);
            ctx.setVariable("appelli", appelli);

            templateEngine.process(path, ctx, response.getWriter());
        } else {
            String path = "AppelliStudenteVuoto.html";
            IWebExchange webExchange = JakartaServletWebApplication
                    .buildApplication(getServletContext())
                    .buildExchange(request, response);

            WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
            ctx.setVariable("nomeCorso", nomeCorso);
            ctx.setVariable("idCorso", idCorso);
            ctx.setVariable("appelli", appelli);

            templateEngine.process(path, ctx, response.getWriter());
        }

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
