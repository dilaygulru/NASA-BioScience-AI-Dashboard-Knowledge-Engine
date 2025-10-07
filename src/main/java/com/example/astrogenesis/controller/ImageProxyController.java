package com.example.astrogenesis.controller;

import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@RestController
public class ImageProxyController {

    private final RestTemplate restTemplate;

    public ImageProxyController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    /**
     * Proxy endpoint to fetch external images and bypass CORS
     * Usage: /api/image-proxy?url=https://example.com/image.jpg
     */
    @GetMapping("/api/image-proxy")
    public ResponseEntity<byte[]> getImage(@RequestParam String url) {
        try {
            System.out.println("🖼️ [Image Proxy] Received request for: " + url);

            // Validate URL (security measure)
            if (!url.startsWith("https://www.ncbi.nlm.nih.gov/")) {
                System.err.println("⚠️ [Image Proxy] Rejected invalid URL: " + url);
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body("Error: Invalid URL. Only NCBI URLs are allowed.".getBytes());
            }

            System.out.println("🔄 [Image Proxy] URL validated, fetching...");

            // Set headers to mimic a browser request
            HttpHeaders headers = new HttpHeaders();
            headers.set("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36");
            headers.set("Accept", "image/avif,image/webp,image/apng,image/svg+xml,image/*,*/*;q=0.8");
            headers.set("Referer", "https://www.ncbi.nlm.nih.gov/");

            HttpEntity<String> entity = new HttpEntity<>(headers);

            System.out.println("📡 [Image Proxy] Sending request to NCBI...");

            // Fetch the image with error handling
            ResponseEntity<byte[]> response;
            try {
                response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    byte[].class
                );
            } catch (RestClientException e) {
                System.err.println("⚠️ [Image Proxy] RestClientException: " + e.getMessage());
                return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
                    .body(("Error fetching image: " + e.getMessage()).getBytes());
            }

            System.out.println("📨 [Image Proxy] Response status: " + response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                System.out.println("✅ [Image Proxy] Successfully fetched image (" + response.getBody().length + " bytes)");

                // Determine content type from response or default to jpeg
                String contentType = "image/jpeg";
                if (response.getHeaders().getContentType() != null) {
                    contentType = response.getHeaders().getContentType().toString();
                }

                System.out.println("📄 [Image Proxy] Content-Type: " + contentType);

                // Return image with proper headers
                HttpHeaders responseHeaders = new HttpHeaders();
                responseHeaders.setContentType(MediaType.parseMediaType(contentType));
                responseHeaders.setCacheControl(CacheControl.maxAge(86400, java.util.concurrent.TimeUnit.SECONDS));
                responseHeaders.setContentLength(response.getBody().length);

                return new ResponseEntity(response.getBody(), responseHeaders, HttpStatus.OK);
            } else {
                System.err.println("⚠️ [Image Proxy] Failed to fetch image. Status: " + response.getStatusCode());
                return ResponseEntity.status(response.getStatusCode())
                    .body(("Error: Failed to fetch image from NCBI. Status: " + response.getStatusCode()).getBytes());
            }

        } catch (Exception e) {
            System.err.println("❌ [Image Proxy] EXCEPTION occurred!");
            System.err.println("    Exception type: " + e.getClass().getName());
            System.err.println("    Message: " + e.getMessage());
            System.err.println("    Stack trace:");
            e.printStackTrace();

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(("Error: " + e.getClass().getSimpleName() + " - " + e.getMessage()).getBytes());
        }
    }
}
