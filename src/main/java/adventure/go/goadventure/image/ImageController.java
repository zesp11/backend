package adventure.go.goadventure.image;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private ImageUploadService imageUploadService;

    @PostMapping("/upload")
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) throws IOException {
        String imageUrl = imageUploadService.uploadImage(file);
        return ResponseEntity.ok().body("{\"message\": \"Image uploaded successfully\", \"url\": \"" + imageUrl + "\"}");
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getImageUrl(@PathVariable Long id) {
        String imageUrl = imageUploadService.getImageUrlById(id);
        return ResponseEntity.ok().body("{\"url\": \"" + imageUrl + "\"}");
    }

    @GetMapping("/check-credentials")
    public ResponseEntity<String> checkCredentials() {
        return ResponseEntity.ok("Credentials are valid");
    }
}