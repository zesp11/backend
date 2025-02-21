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
        String fileUrl = String.valueOf(imageUploadService.uploadImage(file));
        return ResponseEntity.ok().body("{\"file_url\": \"" + fileUrl + "\"}");
    }

    @GetMapping("/check-credentials")
    public ResponseEntity<String> checkCredentials() {
        // Implement your logic to check Cloudinary credentials
        return ResponseEntity.ok("Credentials are valid");
    }
}