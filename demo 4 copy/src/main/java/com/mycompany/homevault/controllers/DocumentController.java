package com.mycompany.homevault.controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.mycompany.homevault.model.Document;
import com.mycompany.homevault.model.User;
import com.mycompany.homevault.repo.DocumentRepository;
import com.mycompany.homevault.repo.UserRepository;

import jakarta.servlet.http.HttpSession;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    @Autowired
    private DocumentRepository documentRepository;

    @Autowired
    private UserRepository userRepository;

    private final String uploadDir = "uploads/";

    @PostMapping("/upload")
    public ResponseEntity<String> uploadDocument(@RequestParam("file") MultipartFile file,
                                                  @RequestParam("username") String username,
                                                  HttpSession session) {
        if (session.getAttribute("user") == null) {
            return new ResponseEntity<>("User not logged in", HttpStatus.UNAUTHORIZED);
        }

        if (file.isEmpty()) {
            return new ResponseEntity<>("No file selected", HttpStatus.BAD_REQUEST);
        }

        try {
            File directory = new File(uploadDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String originalFileName = file.getOriginalFilename();
            String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;
            String filePath = uploadDir + uniqueFileName;

            Path path = Paths.get(filePath);
            Files.write(path, file.getBytes());

            User user = userRepository.findByUsername(username);
            if (user == null) {
                return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
            }

            Document document = new Document();
            document.setUser(user);
            document.setFilePath(filePath);
            document.setUploadTime(java.time.LocalDateTime.now().toString());

            documentRepository.save(document);

            return new ResponseEntity<>("File uploaded successfully", HttpStatus.OK);
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>("File upload failed", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/list")
    public ResponseEntity<List<Document>> listDocuments(HttpSession session) {
        if (session.getAttribute("user") == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        List<Document> documents = documentRepository.findAllDocuments();
        return new ResponseEntity<>(documents, HttpStatus.OK);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadDocument(@PathVariable("id") Long id, HttpSession session) {
        if (session.getAttribute("user") == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Document document = documentRepository.findById(id);
        if (document != null) {
            File file = new File(document.getFilePath());
            try {
                byte[] fileContent = Files.readAllBytes(file.toPath());
                return new ResponseEntity<>(fileContent, HttpStatus.OK);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
