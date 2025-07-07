package it.polimi.tiw.tiwproject2024purehtml.controllers;

import it.polimi.tiw.tiwproject2024purehtml.beans.Utente;
import it.polimi.tiw.tiwproject2024purehtml.dao.UtenteDAO;
import it.polimi.tiw.tiwproject2024purehtml.utility.ConnectionHandler;
import it.polimi.tiw.tiwproject2024purehtml.utility.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.WebApplicationTemplateResolver;
import org.thymeleaf.web.IWebExchange;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;


import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

@WebServlet("/CheckLogin")
public class CheckLogin extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private Connection connection =  null;
    private TemplateEngine templateEngine;

    public CheckLogin() {
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

    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String email = request.getParameter("email");
        String password = request.getParameter("password");

        if (email == null || email.isEmpty() || password == null || password.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Missing parameters");
            return;
        }

        String hashedPassword = PasswordUtil.hashPasswordSHA1(password);
        UtenteDAO uDAO = new UtenteDAO(connection);
        Utente utente;

        try {
            utente = uDAO.checkCredentials(email, hashedPassword);
        } catch (SQLException e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_BAD_GATEWAY, "Failure in database credential checking");
            return;
        }

        if (utente == null) {
            IWebExchange webExchange = JakartaServletWebApplication
                    .buildApplication(getServletContext())
                    .buildExchange(request, response);

            WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
            ctx.setVariable("errorMsg", "Incorrect email or password");

            templateEngine.process("login.html", ctx, response.getWriter());
            return;
        }

        // Autenticazione riuscita
        String target = "";
        request.getSession().setAttribute("utente", utente);
        if (utente.getRuolo().equals("studente")) {
            target = "/GoToHomeStudente";
        }
        else if (utente.getRuolo().equals("docente")) {
            target = "/GoToHomeDocente";
        } else {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Undefined user");
        }
        response.sendRedirect(getServletContext().getContextPath() + target);
    }

    public void destroy() {
        try {
            ConnectionHandler.closeConnection(connection);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
