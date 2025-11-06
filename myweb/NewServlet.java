import java.io.*;
import jakarta.servlet.*;
import jakarta.servlet.http.*;

public class NewServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse res)
            throws IOException {
        res.setContentType("text/html");
        PrintWriter out = res.getWriter();
        out.println("<h1>Hello from MyNewServlet!</h1>");
    }
}