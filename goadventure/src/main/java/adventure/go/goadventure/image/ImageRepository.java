package adventure.go.goadventure.image;

import adventure.go.goadventure.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepository extends JpaRepository<Image, Long> {
}