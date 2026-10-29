package com.example.controller;

import java.util.Map;

import randy.framework.annotation.RAuthenticated;
import randy.framework.annotation.RController;
import randy.framework.annotation.RRestApi;
import randy.framework.annotation.RUrlMapping;
import randy.framework.model.RSession;

/**
 * Sprint 10 / 10-bis — @RAuthenticated posé sur la CLASSE (TYPE).
 * Toutes les routes de ce controller exigent le rôle ADMIN, sans répéter l'annotation.
 * Une annotation sur une méthode remplace celle de la classe pour cette méthode.
 */
@RController
@RAuthenticated(roles = { "ADMIN" })
public class RAdminController {

    /** Hérite de la classe → ADMIN uniquement. */
    @RRestApi
    @RUrlMapping(value = "/sprint/admin/dashboard", method = "GET")
    public Object dashboard(RSession session) {
        return Map.of("message", "dashboard admin", "user", String.valueOf(session.get("user")));
    }

    /** L'annotation de la méthode gagne → ADMIN ou MANAGER. */
    @RAuthenticated(roles = { "ADMIN", "MANAGER" })
    @RRestApi
    @RUrlMapping(value = "/sprint/admin/rapport", method = "GET")
    public Object rapport(RSession session) {
        return Map.of("message", "rapport", "role", String.valueOf(session.get("role")));
    }
}
