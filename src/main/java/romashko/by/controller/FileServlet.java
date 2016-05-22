package romashko.by.controller;

import org.apache.commons.codec.digest.DigestUtils;
import romashko.by.model.Package;
import romashko.by.service.MainService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@WebServlet(urlPatterns = {"/test"})
public class FileServlet extends HttpServlet {
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        int num = Integer.parseInt(request.getParameter("num"));
        String checkSum = request.getParameter("checksum");
        String data = new String(Base64.getDecoder().decode(request.getParameter("data")), StandardCharsets.UTF_8);
        boolean isEnd = Boolean.parseBoolean(request.getParameter("isend"));
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");
        PrintWriter out = response.getWriter();
        if (DigestUtils.md5Hex(data).equals(checkSum)) {
            out.print("OK");
            response.setStatus(HttpServletResponse.SC_OK);
            out.flush();
            MainService.getMainService().addPackage(new Package(num, data.getBytes()), isEnd);
        } else {
            out.print("REPEAT");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            out.flush();
        }

    }
}
