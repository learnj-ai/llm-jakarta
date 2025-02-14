package learning.jakarta.ai.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.util.Base64;

@Slf4j
@WebServlet("/download/*")
public class DownloadServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("Downloading file");

        String requestPath = request.getPathInfo();
        if (requestPath == null || requestPath.isEmpty()) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "File name is missing");
            return;
        }

        try {
            String decodedPath = new String(Base64.getDecoder().decode(requestPath.substring(1, requestPath.length() - 4)));
            log.info("Decoded file path: {}", decodedPath);

            File file = Paths.get(decodedPath + ".zip").toFile();
            if (!file.exists() || file.isDirectory()) {
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "File not found");
                return;
            }

            response.setContentType("application/zip");
            response.setHeader("Content-Disposition", "attachment; filename=\"" + file.getName() + "\"");
            response.setContentLengthLong(file.length());

            try (FileInputStream fis = new FileInputStream(file); OutputStream os = response.getOutputStream()) {
                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
            }
        } catch (IllegalArgumentException e) {
            log.error("Invalid Base64 input", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid file path");
        } catch (IOException e) {
            log.error("Error occurred while downloading file", e);
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Error occurred while downloading file");
        }
    }
}