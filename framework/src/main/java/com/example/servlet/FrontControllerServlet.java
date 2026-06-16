package com.example.servlet;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import com.example.util.Utilitaire;

import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontControllerServlet extends HttpServlet {
    private List<String> listControllers = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
        String packageToScan = config.getInitParameter("packageToScan");
        this.listControllers = Utilitaire.scanControllers(packageToScan);
        System.out.println("Controllers found: " + listControllers.size());
    }
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)     throws ServletException, IOException {
        processRequest(request.getRequestURL().toString(), response);
    }
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
        processRequest(request.getRequestURL().toString(), response);
    }
    private void processRequest(String requestURL, HttpServletResponse response) throws IOException {
        // system answer
        System.out.println("URL: " + requestURL);
        try {
        response.setContentType("text/plain,charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("URL: " + requestURL);
        for (String controller : listControllers) {
            System.out.println("Controller: " + controller);
            out.println("Controller: " + controller);
        }
        } catch (IOException e) {
            System.err.println("Error response: " + e.getMessage());
        }
    }
}
