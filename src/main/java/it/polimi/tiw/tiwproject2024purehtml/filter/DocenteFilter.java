package it.polimi.tiw.tiwproject2024purehtml.filter;

import it.polimi.tiw.tiwproject2024purehtml.beans.Utente;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
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

@WebFilter("/docente/*")
public class DocenteFilter implements Filter {
    private TemplateEngine templateEngine;

    public void init(FilterConfig filterConfig) throws ServletException {
        ServletContext servletContext = filterConfig.getServletContext();
        JakartaServletWebApplication webApplication = JakartaServletWebApplication.buildApplication(servletContext);
        WebApplicationTemplateResolver templateResolver = new WebApplicationTemplateResolver(webApplication);
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setPrefix("/WEB-INF/");
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(false); // utile in fase di sviluppo

        this.templateEngine = new TemplateEngine();
        this.templateEngine.setTemplateResolver(templateResolver);
    }

    public DocenteFilter() {
        // TODO Auto-generated constructor stub
    }

    public void destroy() {
        // TODO Auto-generated method stub
    }

    public void doFilter(ServletRequest request, ServletResponse response, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse res = (HttpServletResponse) response;

        HttpSession s = req.getSession();
        Utente utente = (Utente) s.getAttribute("utente");

        if (utente == null) {
            res.sendRedirect(req.getContextPath() + "/login.html");
            return;
        }

        if (!utente.getRuolo().equals("docente")) {
            String path = "ErrorPage.html";
            IWebExchange webExchange = JakartaServletWebApplication
                    .buildApplication(req.getServletContext())
                    .buildExchange(req, res);

            WebContext ctx = new WebContext(webExchange, webExchange.getLocale());
            ctx.setVariable("error", "ACCESSO NON AUTORIZZATO");
            ctx.setVariable("description", "non hai i permessi per entrare in questa pagina");
            templateEngine.process(path, ctx, response.getWriter());
            return;
        }
        filterChain.doFilter(req, res);

    }
}
