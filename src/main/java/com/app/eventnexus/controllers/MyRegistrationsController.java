package com.app.eventnexus.controllers;

import com.app.eventnexus.dtos.responses.MyRegistrationResponse;
import com.app.eventnexus.security.UserPrincipal;
import com.app.eventnexus.services.MyRegistrationsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Thin REST controller exposing the authenticated user's tournament registrations.
 * Returns registrations across all teams the user manages, regardless of org context.
 */
@RestController
public class MyRegistrationsController {

    private final MyRegistrationsService myRegistrationsService;

    public MyRegistrationsController(MyRegistrationsService myRegistrationsService) {
        this.myRegistrationsService = myRegistrationsService;
    }

    /**
     * Returns all tournament registrations for teams managed by the authenticated user.
     * Spectators and admins with no managed teams will receive an empty list.
     *
     * @param authentication the current authenticated caller
     * @return 200 OK with list of registration summaries
     */
    @GetMapping("/api/users/me/registrations")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<MyRegistrationResponse>> getMyRegistrations(
            Authentication authentication) {
        Long userId = ((UserPrincipal) authentication.getPrincipal()).getUserId();
        return ResponseEntity.ok(myRegistrationsService.getRegistrationsForManager(userId));
    }
}
