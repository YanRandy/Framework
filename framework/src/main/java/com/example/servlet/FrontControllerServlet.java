package com.example.servlet;

import java.io.IOException;
import java.io.PrintWriter;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class FrontControllerServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) 
    throws ServletException, IOException {
        processRequest(request.getRequestURL().toString(), response);
    }
    private void processRequest(String requestURL, HttpServletResponse response) throws IOException {
        // system answe
        System.out.println("URL: " + requestURL);
        try {
        response.setContentType("text/plain,charset=UTF-8");
        PrintWriter out = response.getWriter();
        out.println("URL: " + requestURL);
        } catch (IOException e) {
            System.err.println("Error response: " + e.getMessage());
        }
    }
}
