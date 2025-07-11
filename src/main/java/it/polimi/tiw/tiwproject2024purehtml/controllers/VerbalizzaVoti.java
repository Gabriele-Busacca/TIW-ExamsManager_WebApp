package it.polimi.tiw.tiwproject2024purehtml.controllers;

import it.polimi.tiw.tiwproject2024purehtml.beans.DettaglioIscrizioneStudente;
import it.polimi.tiw.tiwproject2024purehtml.beans.Utente;
import it.polimi.tiw.tiwproject2024purehtml.dao.AppelloDAO;
import it.polimi.tiw.tiwproject2024purehtml.dao.VerbaleDAO;
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
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/docente/VerbalizzaVoti")
public class VerbalizzaVoti extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection =  null;
    private TemplateEngine templateEngine;

    public VerbalizzaVoti() {
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
        HttpSession session = request.getSession(false);
        Utente utente = (Utente) session.getAttribute("utente");

        int idVerbale;
        try {
            idVerbale = Integer.parseInt(request.getParameter("idVerbale"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid idAppello");
            return;
        }

        IWebExchange webExchange = JakartaServletWebApplication
                .buildApplication(getServletContext())
                .buildExchange(request, response);

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        VerbaleDAO verbaleDAO = new VerbaleDAO(connection);
        AppelloDAO appelloDAO = new AppelloDAO(connection);

        int idAppello;
        try {
            // Recupera l'idAppello associato al verbale
            idAppello = verbaleDAO.getIdAppelloByIdVerbale(idVerbale);
            if (idAppello == -1) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Verbale non trovato");
                return;
            }

            // Controlla che il docente abbia accesso al verbale (tramite idAppello)
            if (!appelloDAO.checkAppelloByDocente(utente.getId(), idAppello)) {
                ctx.setVariable("error", "ACCESSO NON AUTORIZZATO");
                ctx.setVariable("description", "Hai tentato di accedere ad una risorsa non tua!");
                templateEngine.process("ErrorPage.html", ctx, response.getWriter());
                return;
            }

        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore DB");
            return;
        }

        List<DettaglioIscrizioneStudente> studenti;
        Timestamp timestamp;
        try {
            studenti = verbaleDAO.getStudentiVerbalizzati(idVerbale);
            timestamp = verbaleDAO.getCurrentTimestamp(idVerbale);
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore DB");
            return;
        }

        ZoneId zone = ZoneId.of("Europe/Rome");
        ZonedDateTime zoned = timestamp.toInstant().atZone(zone);

        String data = zoned.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        String ora = zoned.format(DateTimeFormatter.ofPattern("HH:mm"));

        try {
            String nomeCorso = appelloDAO.getNomeCorsoByAppello(idAppello);
            String nomeProf = utente.getNome() + " " + utente.getCognome();

            ctx.setVariable("idVerbale", idVerbale);
            ctx.setVariable("data", data);
            ctx.setVariable("ora", ora);
            ctx.setVariable("idAppello", idAppello);
            ctx.setVariable("studenti", studenti);
            ctx.setVariable("nomeCorso", nomeCorso);
            ctx.setVariable("docente", nomeProf);

            ctx.setVariable("returnPage", "verbali");

            templateEngine.process("Verbale.html", ctx, response.getWriter());
        } catch (SQLException e) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Errore DB");
        }
    }


    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        HttpSession session = request.getSession();

        int idAppello;
        // Verifica parametri
        try {
            idAppello = Integer.parseInt(request.getParameter("idAppello"));
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid idAppello");
            return;
        }

        IWebExchange webExchange = JakartaServletWebApplication
                .buildApplication(getServletContext())
                .buildExchange(request, response);

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        //check accesso a risorsa altrui
        AppelloDAO appelloDAO = new AppelloDAO(connection);
        Utente utente = (Utente) session.getAttribute("utente");

        try {
            if (!appelloDAO.checkAppelloByDocente(utente.getId(), idAppello)) {
                String path = "ErrorPage.html";
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

        VerbaleDAO verbaleDAO = new VerbaleDAO(connection);
        int idVerbale;

        //controllo se ci sono studenti con voto verbalizzabile, ovvero con stato 'pubblicato' o 'rifiutato'
        try {
            if (!appelloDAO.hasVerbalizzabili(idAppello)) {
                String path = "WarningPage.html";
                ctx.setVariable("error", "Non è possibile verbalizzare il voto.");
                ctx.setVariable("description", "Nessun voto ha stato 'PUBBLICATO' o 'RIFIUTATO'");
                templateEngine.process(path, ctx, response.getWriter());
                return;
            }
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }

        //creo verbale
        try {
            idVerbale = verbaleDAO.creaVerbale(idAppello);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }

        try {
            appelloDAO.verbalizzaIscritti(idAppello);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }

        //collego a studenti con tabella ponte
        try {
            verbaleDAO.collegaStudentiAVerbale(idAppello, idVerbale);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }

        List<DettaglioIscrizioneStudente> studenti = new ArrayList<>();
        try {
            studenti = verbaleDAO.getStudentiVerbalizzati(idVerbale);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }

        Timestamp timestamp = null;
        String data;
        String ora;
        try {
            timestamp = verbaleDAO.getCurrentTimestamp(idVerbale);
            ZoneId zone = ZoneId.of("Europe/Rome");
            ZonedDateTime zoned = timestamp.toInstant().atZone(zone);

            data = zoned.format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
            ora = zoned.format(DateTimeFormatter.ofPattern("HH:mm"));

        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }

        String path = "Verbale.html";
        ctx.setVariable("idVerbale", idVerbale);
        ctx.setVariable("data", data);
        ctx.setVariable("ora", ora);
        ctx.setVariable("idAppello", idAppello);
        ctx.setVariable("studenti", studenti);

        try {
            String nomeCorso = appelloDAO.getNomeCorsoByAppello(idAppello);
            String nomeProf = utente.getNome() + " " + utente.getCognome();

            ctx.setVariable("nomeCorso", nomeCorso);
            ctx.setVariable("docente", nomeProf);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }

        ctx.setVariable("returnPage", "iscritti");

        templateEngine.process(path, ctx, response.getWriter());

    }
}
