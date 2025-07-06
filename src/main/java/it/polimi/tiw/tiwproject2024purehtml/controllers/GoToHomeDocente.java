package it.polimi.tiw.tiwproject2024purehtml.controllers;

import it.polimi.tiw.tiwproject2024purehtml.beans.Corso;
import it.polimi.tiw.tiwproject2024purehtml.beans.Docente;
import it.polimi.tiw.tiwproject2024purehtml.dao.CorsoDAO;
import it.polimi.tiw.tiwproject2024purehtml.utility.ConnectionHandler;
import jakarta.servlet.ServletConfig;
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

@WebServlet("/GoToHomeDocente")
public class GoToHomeDocente extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection =  null;
    private TemplateEngine templateEngine;

    public GoToHomeDocente() {
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
        Docente d =  (Docente) session.getAttribute("utente");
        CorsoDAO corsoDAO = new CorsoDAO(connection);
        List<Corso> corsi = null;
        try {
            corsi = corsoDAO.getCorsibyIdDocente(d.getId());
            if (corsi == null) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "Resource not found");
                return;
            }
        } catch (SQLException e) {
            String errorMessage = e.getMessage();
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, errorMessage);
            return;
        }

        request.getSession().setAttribute("corsi", corsi);
        String path = "HomeDocente.html";
        IWebExchange webExchange = JakartaServletWebApplication
                .buildApplication(getServletContext())
                .buildExchange(request, response);

        WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
        ctx.setVariable("corsi", corsi);
        if (corsi.isEmpty()) {
            ctx.setVariable("errorMsg", "Non ci sono corsi");
        }
        templateEngine.process(path, ctx, response.getWriter());
    }
}
