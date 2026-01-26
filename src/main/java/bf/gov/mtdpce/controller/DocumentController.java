package bf.gov.mtdpce.controller;

import bf.gov.mtdpce.dto.ApiResponse;
import bf.gov.mtdpce.dto.DocumentDTO;
import bf.gov.mtdpce.entity.DocumentCategory;
import bf.gov.mtdpce.security.UserDetailsImpl;
import bf.gov.mtdpce.service.DocumentService;
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
@RequestMapping("/api/v1/documents")
@Tag(name = "Documents", description = "API de gestion des documents et ressources")
public class DocumentController {

    @Autowired
    private DocumentService documentService;

    // Public endpoints
    @GetMapping("/public")
    @Operation(summary = "Documents publics", description = "Récupère la liste des documents publics")
    public ResponseEntity<ApiResponse<Page<DocumentDTO>>> getPublicDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(documentService.getPublicDocuments(pageable)));
    }

    @GetMapping("/public/latest")
    @Operation(summary = "Derniers documents", description = "Récupère les 10 derniers documents publics")
    public ResponseEntity<ApiResponse<List<DocumentDTO>>> getLatestPublicDocuments() {
        return ResponseEntity.ok(ApiResponse.success(documentService.getLatestPublicDocuments()));
    }

    @GetMapping("/public/category/{category}")
    @Operation(summary = "Documents par catégorie", description = "Récupère les documents d'une catégorie")
    public ResponseEntity<ApiResponse<Page<DocumentDTO>>> getDocumentsByCategory(
            @PathVariable DocumentCategory category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return ResponseEntity.ok(ApiResponse.success(documentService.getDocumentsByCategory(category, pageable)));
    }

    @GetMapping("/public/search")
    @Operation(summary = "Recherche de documents", description = "Recherche dans les documents publics")
    public ResponseEntity<ApiResponse<Page<DocumentDTO>>> searchPublicDocuments(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        return ResponseEntity.ok(ApiResponse.success(documentService.searchPublicDocuments(query, pageable)));
    }

    @GetMapping("/public/{id}")
    @Operation(summary = "Détail document", description = "Récupère un document par son ID")
    public ResponseEntity<ApiResponse<DocumentDTO>> getPublicDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getDocumentById(id)));
    }

    @PostMapping("/public/{id}/download")
    @Operation(summary = "Télécharger document", description = "Incrémente le compteur de téléchargement")
    public ResponseEntity<ApiResponse<Void>> downloadDocument(@PathVariable Long id) {
        documentService.incrementDownloadCount(id);
        return ResponseEntity.ok(ApiResponse.success("Téléchargement enregistré", null));
    }

    // Protected endpoints
    @GetMapping
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Tous les documents", description = "Récupère tous les documents (admin)")
    public ResponseEntity<ApiResponse<Page<DocumentDTO>>> getAllDocuments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(ApiResponse.success(documentService.getAllDocuments(pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Détail document (admin)", description = "Récupère un document par son ID (admin)")
    public ResponseEntity<ApiResponse<DocumentDTO>> getDocumentById(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(documentService.getDocumentById(id)));
    }

    @PostMapping
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Créer un document", description = "Crée un nouveau document")
    public ResponseEntity<ApiResponse<DocumentDTO>> createDocument(
            @Valid @RequestBody DocumentDTO documentDTO,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(ApiResponse.success("Document créé", documentService.createDocument(documentDTO, userDetails.getId())));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('MODERATOR') or hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Modifier un document", description = "Met à jour un document existant")
    public ResponseEntity<ApiResponse<DocumentDTO>> updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentDTO documentDTO) {
        return ResponseEntity.ok(ApiResponse.success("Document mis à jour", documentService.updateDocument(id, documentDTO)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Supprimer un document", description = "Supprime un document")
    public ResponseEntity<ApiResponse<Void>> deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
        return ResponseEntity.ok(ApiResponse.success("Document supprimé", null));
    }
}
