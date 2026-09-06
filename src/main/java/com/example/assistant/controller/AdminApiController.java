package com.example.assistant.controller;

import com.example.assistant.dto.admin.BroadcastRequestDto;
import com.example.assistant.dto.admin.BroadcastResultDto;
import com.example.assistant.dto.admin.DashboardStatsDto;
import com.example.assistant.dto.admin.UserDto;
import com.example.assistant.service.admin.AdminService;
import com.example.assistant.service.admin.BroadcastService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminApiController {

    private final AdminService adminService;
    private final BroadcastService broadcastService;

    @GetMapping("/stats")
    public ResponseEntity<DashboardStatsDto> getStats() {
        return ResponseEntity.ok(adminService.getDashboardStats());
    }

    @GetMapping("/users")
    public ResponseEntity<Page<UserDto>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(adminService.getUsers(PageRequest.of(page, size)));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<UserDto> toggleUserStatus(@PathVariable Long id) {
        return ResponseEntity.ok(adminService.toggleUserStatus(id));
    }

    @PutMapping("/users/{id}/tier")
    public ResponseEntity<UserDto> updateUserTier(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        String tier = payload.getOrDefault("tier", "FREE");
        return ResponseEntity.ok(adminService.updateUserTier(id, tier));
    }

    @PostMapping("/broadcast")
    public CompletableFuture<ResponseEntity<BroadcastResultDto>> broadcast(
            @Valid @RequestBody BroadcastRequestDto request) {
        return broadcastService.broadcastMessage(request).thenApply(ResponseEntity::ok);
    }
}
