package com.example.sparta_ticketing.domain.reserve.controller;

import com.example.sparta_ticketing.domain.auth.entity.AuthUser;
import com.example.sparta_ticketing.domain.reserve.dto.request.ReserveCancelRequest;
import com.example.sparta_ticketing.domain.reserve.dto.request.ReserveRequest;
import com.example.sparta_ticketing.domain.reserve.dto.response.ReserveResponse;
import com.example.sparta_ticketing.domain.reserve.service.ReserveService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ReserveController {
    private final ReserveService reserveService;

    @PostMapping("/reservation")
    public ResponseEntity<ReserveResponse> reserve(@RequestBody ReserveRequest reserveRequest,
                                                   @AuthenticationPrincipal AuthUser authUser) {
        return ResponseEntity.ok(reserveService.reserve(authUser, reserveRequest));
    }

    @DeleteMapping
    public void reserveCancel(@AuthenticationPrincipal AuthUser authUser,
                              @RequestBody ReserveCancelRequest request) {
        reserveService.reserveCancel(authUser, request);
    }
}
