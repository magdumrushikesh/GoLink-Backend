package GoLink_Backend.controller;

import GoLink_Backend.model.Url;
import GoLink_Backend.model.User;
import GoLink_Backend.repository.UrlRepository;
import GoLink_Backend.repository.UserRepository;
import GoLink_Backend.utils.Base62Utils;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.hibernate.validator.constraints.URL;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@CrossOrigin
public class UrlController {
    private final UrlRepository urlRepository;
    private final UserRepository userRepository;

    public UrlController(UrlRepository urlRepository, UserRepository userRepository) {
        this.urlRepository = urlRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/shorten")
    public ResponseEntity<?> shortenUrl(@Valid @RequestBody ShortenRequest request, Authentication authentication) {
        User user = null;
        if (authentication != null && authentication.isAuthenticated() && 
            !"anonymousUser".equals(authentication.getPrincipal())) {
            String email = authentication.getName();
            user = userRepository.findByEmail(email).orElse(null);
        }

        // Generate unique short code
        String shortCode;
        int maxAttempts = 5;
        int attempts = 0;
        do {
            shortCode = Base62Utils.generateShortCode(6);
            attempts++;
        } while (urlRepository.existsByShortCode(shortCode) && attempts < maxAttempts);

        if (urlRepository.existsByShortCode(shortCode)) {
            return ResponseEntity.status(500).body(Map.of("message", "Failed to generate a unique short code. Please try again."));
        }

        Url url = new Url(request.getOriginalUrl(), shortCode, user);

        Url savedUrl = urlRepository.save(url);

        Map<String, Object> response = new HashMap<>();
        response.put("id", savedUrl.getId());
        response.put("originalUrl", savedUrl.getOriginalUrl());
        response.put("shortCode", savedUrl.getShortCode());
        response.put("clicks", savedUrl.getClicks());
        response.put("createdAt", savedUrl.getCreatedAt());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/urls")
    public ResponseEntity<?> getUserUrls(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));

        List<Url> urls = urlRepository.findByUserOrderByCreatedAtDesc(user);

        List<Map<String, Object>> response = urls.stream().map(url -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", url.getId());
            map.put("originalUrl", url.getOriginalUrl());
            map.put("shortCode", url.getShortCode());
            map.put("clicks", url.getClicks());
            map.put("createdAt", url.getCreatedAt());
            return map;
        }).collect(Collectors.toList());

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/urls/{id}")
    public ResponseEntity<?> deleteUrl(@PathVariable Long id, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("message", "Unauthorized"));
        }

        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));

        Url url = urlRepository.findById(id).orElse(null);
        if (url == null) {
            return ResponseEntity.notFound().build();
        }

        if (url.getUser() == null || !url.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).body(Map.of("message", "You don't have permission to delete this URL"));
        }

        urlRepository.delete(url);
        return ResponseEntity.ok(Map.of("message", "URL deleted successfully"));
    }

    public static class ShortenRequest {
        @NotBlank
        @URL(message = "Please provide a valid URL")
        private String originalUrl;

        // Getters and Setters
        public String getOriginalUrl() {
            return originalUrl;
        }

        public void setOriginalUrl(String originalUrl) {
            this.originalUrl = originalUrl;
        }
    }
}
