package bf.gov.mtdpce.controller;

import bf.gov.mtdpce.dto.ApiResponse;
import bf.gov.mtdpce.dto.ArticleDTO;
import bf.gov.mtdpce.entity.ArticleCategory;
import bf.gov.mtdpce.security.UserDetailsImpl;
import bf.gov.mtdpce.service.ArticleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/articles")
@Tag(name = "Articles", description = "API de gestion des articles et actualités")
public class ArticleController {

    @Autowired
    private ArticleService articleService;

    // Public endpoints
    @GetMapping("/published")
    @Operation(summary = "Articles publiés", description = "Récupère la liste des articles publiés")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getPublishedArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return ResponseEntity.ok(ApiResponse.success(articleService.getPublishedArticles(pageable)));
    }

    @GetMapping("/published/latest")
    @Operation(summary = "Derniers articles", description = "Récupère les 5 derniers articles publiés")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getLatestArticles() {
        return ResponseEntity.ok(ApiResponse.success(articleService.getLatestArticles()));
    }

    @GetMapping("/published/featured")
    @Operation(summary = "Articles à la une", description = "Récupère les articles mis en avant")
    public ResponseEntity<ApiResponse<List<ArticleDTO>>> getFeaturedArticles() {
        return ResponseEntity.ok(ApiResponse.success(articleService.getFeaturedArticles()));
    }

    @GetMapping("/published/category/{category}")
    @Operation(summary = "Articles par catégorie", description = "Récupère les articles publiés d'une catégorie")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getArticlesByCategory(
            @PathVariable ArticleCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("publishedAt").descending());
        return ResponseEntity.ok(ApiResponse.success(articleService.getArticlesByCategory(category, pageable)));
    }

    @GetMapping("/published/search")
    @Operation(summary = "Recherche d'articles", description = "Recherche dans les articles publiés")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> searchPublishedArticles(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(articleService.searchPublishedArticles(query, pageable)));
    }

    @GetMapping("/published/{id}")
    @Operation(summary = "Détail article publié", description = "Récupère un article publié par son ID")
    public ResponseEntity<ApiResponse<ArticleDTO>> getPublishedArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(articleService.getPublishedArticleById(id)));
    }

    // Protected endpoints
    @GetMapping
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Tous les articles", description = "Récupère tous les articles (admin)")
    public ResponseEntity<ApiResponse<Page<ArticleDTO>>> getAllArticles(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(ApiResponse.success(articleService.getAllArticles(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Détail article", description = "Récupère un article par son ID (admin)")
    public ResponseEntity<ApiResponse<ArticleDTO>> getArticleById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(articleService.getArticleById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Créer un article", description = "Crée un nouvel article")
    public ResponseEntity<ApiResponse<ArticleDTO>> createArticle(
            @Valid @RequestBody ArticleDTO articleDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Article créé", articleService.createArticle(articleDTO, userDetails.getId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Modifier un article", description = "Met à jour un article existant")
    public ResponseEntity<ApiResponse<ArticleDTO>> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleDTO articleDTO) {
        return ResponseEntity.ok(ApiResponse.success("Article mis à jour", articleService.updateArticle(id, articleDTO)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Supprimer un article", description = "Supprime un article")
    public ResponseEntity<ApiResponse<Void>> deleteArticle(@PathVariable Long id) {
        articleService.deleteArticle(id);
        return ResponseEntity.ok(ApiResponse.success("Article supprimé", null));
    }
}
