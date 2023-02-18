package hello.springcoremvc211.controller;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.io.IOException;
import java.util.Collection;

@Slf4j
@Controller
@RequestMapping("/servlet/v1")
public class ServletUploadControllerV1 {
    @GetMapping("/upload")
    public String newFile() {
        return "upload-form";
    }

    @PostMapping("/upload")
    public String saveFile(
            HttpServletRequest req
    ) throws ServletException, IOException {
        log.info("request = {}", req);

        String itemName = req.getParameter("itemName");
        log.info("itemName = {}", itemName);

        Collection<Part> parts = req.getParts();
        log.info("parts = {}", parts);

        return "upload-form";
    }
}
