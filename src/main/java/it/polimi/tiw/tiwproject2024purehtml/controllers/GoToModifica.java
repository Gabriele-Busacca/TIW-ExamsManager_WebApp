package it.polimi.tiw.tiwproject2024purehtml.controllers;

import it.polimi.tiw.tiwproject2024purehtml.beans.DettaglioIscrizioneStudente;
import it.polimi.tiw.tiwproject2024purehtml.beans.Utente;
import it.polimi.tiw.tiwproject2024purehtml.beans.Voto;
import it.polimi.tiw.tiwproject2024purehtml.dao.AppelloDAO;
import it.polimi.tiw.tiwproject2024purehtml.dao.DettaglioIscrizioneStudenteDAO;
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
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/docente/GoToModifica")
public class GoToModifica extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection =  null;
    private TemplateEngine templateEngine;

    public GoToModifica() {
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
        int matricola;
        int idAppello;
        //check formato parametri
        try {
            matricola = Integer.parseInt(request.getParameter("matricola"));
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

        //check se si può modificare il voto
        boolean isModifiable = false;
        try {
            isModifiable = appelloDAO.checkModificaVoto(idAppello, matricola);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, errorMessage);
            return;
        }

        IWebExchange webExchange = JakartaServletWebApplication
                .buildApplication(getServletContext())
                .buildExchange(request, response);

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());

        if(!isModifiable){
            String path = "WarningPage.html";
            ctx.setVariable("error", "Non è possibile modificare il voto.");
            ctx.setVariable("description", "Controlla che la matricola dello studente sia corretta o che lo stato della valutazione sia 'non inserito' o 'inserito'");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }

        DettaglioIscrizioneStudenteDAO disDAO =  new DettaglioIscrizioneStudenteDAO(connection);
        DettaglioIscrizioneStudente studente = new DettaglioIscrizioneStudente();
        try {
            studente = disDAO.getStudenteIscritto(idAppello, matricola);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }

        ctx.setVariable("studente", studente);

        Date data;
        try {
            data = appelloDAO.getDataByIdAppello(idAppello);
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, errorMessage);
            return;
        }
        ctx.setVariable("data", data);
        ctx.setVariable("idAppello", idAppello);
        List<Voto> votiFiltrati = Arrays.stream(it.polimi.tiw.tiwproject2024purehtml.beans.Voto.values())
                .filter(v -> !v.getLabel().isEmpty())  // o .filter(v -> !v.name().isEmpty()) se getLabel() non c'è
                .toList();

        ctx.setVariable("voti", votiFiltrati);


        String path = "ModificaVoto.html";
        templateEngine.process(path, ctx, response.getWriter());

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
