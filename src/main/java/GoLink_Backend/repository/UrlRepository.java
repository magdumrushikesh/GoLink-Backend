package GoLink_Backend.repository;

import GoLink_Backend.model.Url;
import GoLink_Backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UrlRepository extends JpaRepository<Url, Long> {
    Optional<Url> findByShortCode(String shortCode);
    List<Url> findByUserOrderByCreatedAtDesc(User user);
    boolean existsByShortCode(String shortCode);
}
