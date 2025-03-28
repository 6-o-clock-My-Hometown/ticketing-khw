package com.example.sparta_ticketing.domain.show.controller;

import com.example.sparta_ticketing.domain.auth.entity.AuthUser;
import com.example.sparta_ticketing.domain.show.dto.request.CreateShowRequestDto;
import com.example.sparta_ticketing.domain.show.dto.request.UpdateShowRequestDto;
import com.example.sparta_ticketing.domain.show.dto.response.PagingShowResponse;
import com.example.sparta_ticketing.domain.show.dto.response.ShowResponseDto;
import com.example.sparta_ticketing.domain.show.service.ShowService;
import jakarta.validation.Valid;
import com.example.sparta_ticketing.domain.user.enums.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class ShowController {

    private final ShowService showService;

    /**
      공연 생성 API
    */
    @Secured(UserRole.Authority.DIRECTOR)
    @PostMapping("/shows")
    public void createShow(
            @AuthenticationPrincipal AuthUser authUser,
            @RequestBody CreateShowRequestDto createShowRequestDto
            ) {
        showService.createShow(authUser, createShowRequestDto);
    }

    /**
     공연 목록 조회 API
     */
    @GetMapping("/shows")
    public ResponseEntity<PagingShowResponse> getShowList(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ){
        return ResponseEntity.ok(showService.getShowList(page, size));
    }

    /**
     * 특정 공연 조회 API
     */
    @GetMapping("/shows/{showId}")
    public ResponseEntity<ShowResponseDto> getShow (
            @PathVariable Long showId,
            @AuthenticationPrincipal AuthUser authUser
    ) {
        return ResponseEntity.ok(showService.getShow(showId, authUser));
    }

    /**
     * 공연 수정 API
     */
    @Secured(UserRole.Authority.DIRECTOR)
    @PatchMapping("/shows/{showId}")
    public void updateShow(
            @PathVariable Long showId,
            @Valid @RequestBody UpdateShowRequestDto updateShowRequestDto
    ) {
        showService.updateShow(showId, updateShowRequestDto);
    }

    /**
     * 공연 삭제 API
     */
    @Secured(UserRole.Authority.DIRECTOR)
    @DeleteMapping("/shows/{showId}")
    public void deleteShow(
            @PathVariable Long showId
    ) {
        showService.deleteShow(showId);
    }
}
