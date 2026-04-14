package bf.gov.mtdpce.service;

import bf.gov.mtdpce.entity.Article;
import bf.gov.mtdpce.event.ArticlePublishedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.core.io.ByteArrayResource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class FacebookPublisherService {

    @Value("${facebook.page-id}")
    private String pageId;

    @Value("${facebook.access-token}")
    private String accessToken;

    @Value("${facebook.graph-api-url}")
    private String graphApiUrl;

    @Value("${facebook.base-url}")
    private String baseUrl;

    private static final String UPLOAD_BASE_PATH = "/opt/mtdpce/uploads";

    private final RestTemplate restTemplate = new RestTemplate();

    @EventListener
    @Async
    public void onArticlePublished(ArticlePublishedEvent event) {
        Article article = event.getArticle();
        log.info("Publication Facebook de l'article : {}", article.getId());

        try {
            String featuredImage = article.getFeaturedImage();

            if (featuredImage != null && !featuredImage.isBlank()) {
                log.info("Image détectée : '{}'", featuredImage);
                publierAvecImage(article, featuredImage); // on passe le chemin relatif
            } else {
                log.info("Pas d'image pour l'article {}", article.getId());
                publierSansImage(article);
            }
        } catch (Exception e) {
            log.error("Erreur publication Facebook article {} : {}",
                    article.getId(), e.getMessage());
        }
    }

    private void publierAvecImage(Article article, String cheminRelatif) {
        try {
            // Publier directement la photo comme post (avec message et lien)
            // au lieu de créer un feed post avec attached_media
            String url = graphApiUrl + "/" + pageId + "/photos";

            String sousChemín = cheminRelatif.replace("/uploads", "");
            Path cheminFichier = Paths.get(UPLOAD_BASE_PATH + sousChemín);

            if (!Files.exists(cheminFichier)) {
                log.error("Fichier image introuvable : {}", cheminFichier);
                publierSansImage(article);
                return;
            }

            byte[] imageBytes = Files.readAllBytes(cheminFichier);
            String fileName = cheminFichier.getFileName().toString();

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("access_token", accessToken);
            body.add("published", "true");  // publié directement
            body.add("message", construireMessage(article));
            body.add("source", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ResponseEntity<String> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body, headers),
                    String.class
            );
            log.info("Article {} publié sur Facebook avec image : {}",
                    article.getId(), response.getBody());

        } catch (Exception e) {
            log.error("Erreur publication avec image : {}", e.getMessage());
            publierSansImage(article);
        }
    }

    private void publierSansImage(Article article) {
        String url = graphApiUrl + "/" + pageId + "/feed";

        Map<String, String> body = new HashMap<>();
        body.put("message", construireMessage(article));
        body.put("link", baseUrl + "/articles/" + article.getId());
        body.put("access_token", accessToken);

        ResponseEntity<String> response = restTemplate.postForEntity(
                url, body, String.class
        );
        log.info("Article {} publié sur Facebook sans image : {}",
                article.getId(), response.getBody());
    }

    private String uploaderImage(String cheminRelatif) {
        try {
            // cheminRelatif = "/uploads/images/2026/03/fichier.png"
            // On reconstruit le chemin physique :
            // /opt/mtdpce/uploads + /images/2026/03/fichier.png
            String sousChemín = cheminRelatif.replace("/uploads", "");
            Path cheminFichier = Paths.get(UPLOAD_BASE_PATH + sousChemín);

            log.info("Chemin physique de l'image : '{}'", cheminFichier);

            if (!Files.exists(cheminFichier)) {
                log.error("Fichier image introuvable : {}", cheminFichier);
                return null;
            }

            String url = graphApiUrl + "/" + pageId + "/photos";

            byte[] imageBytes = Files.readAllBytes(cheminFichier);
            String fileName = cheminFichier.getFileName().toString();

            log.info("Upload image vers Facebook : {} ({} bytes)", fileName, imageBytes.length);

            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("access_token", accessToken);
            body.add("published", "false");
            body.add("source", new ByteArrayResource(imageBytes) {
                @Override
                public String getFilename() {
                    return fileName;
                }
            });

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    url,
                    new HttpEntity<>(body, headers),
                    Map.class
            );

            if (response.getBody() != null && response.getBody().containsKey("id")) {
                String photoId = response.getBody().get("id").toString();
                log.info("Image uploadée avec succès, photoId : {}", photoId);
                return photoId;
            }

        } catch (Exception e) {
            log.error("Erreur upload image Facebook : {}", e.getMessage());
        }
        return null;
    }

    private String construireMessage(Article article) {
        return String.format(
                "%s\n\n%s\n\nCategorie : %s\n\nLire la suite : %s",
                article.getTitle(),
                article.getSummary(),
                article.getContent(),
                article.getCategory().name(),
                baseUrl + "/articles/" + article.getId()
        );
    }
}