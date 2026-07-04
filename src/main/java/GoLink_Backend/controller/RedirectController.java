package GoLink_Backend.controller;

import GoLink_Backend.model.Url;
import GoLink_Backend.repository.UrlRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;

@RestController
public class RedirectController {
    private final UrlRepository urlRepository;

    public RedirectController(UrlRepository urlRepository) {
        this.urlRepository = urlRepository;
    }

    @GetMapping("/{shortCode:[a-zA-Z0-9]{6,8}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        Optional<Url> urlOpt = urlRepository.findByShortCode(shortCode);
        if (urlOpt.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }

        Url url = urlOpt.get();
        url.setClicks(url.getClicks() + 1);
        urlRepository.save(url);

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(url.getOriginalUrl()))
                .build();
    }
}
