package ru.javaops.masterjava.upload;

import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.WebContext;
import ru.javaops.masterjava.common.web.ThymeleafAppUtil;
import ru.javaops.masterjava.xml.schema.User;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

@WebServlet("/")
@MultipartConfig
public class UploadServlet extends HttpServlet {
    private UserUpload userUpload = new UserUpload();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        TemplateEngine engine = ThymeleafAppUtil.getTemplateEngine(getServletContext());
        engine.process("upload", webContext, resp.getWriter());
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        List<User> users = new ArrayList<>();
        WebContext webContext = new WebContext(req, resp, req.getServletContext(), req.getLocale());
        TemplateEngine engine = ThymeleafAppUtil.getTemplateEngine(getServletContext());
        for (Part part : req.getParts()) {
            try (InputStream is = part.getInputStream()) {
                users.addAll(userUpload.process(is));
                webContext.setVariable("users", users);
                engine.process("result", webContext, resp.getWriter());

            } catch (XMLStreamException e) {
                e.printStackTrace();
            }
        }
    }
}
