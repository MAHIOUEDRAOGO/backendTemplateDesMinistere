package bf.gov.mtdpce.dto;

import bf.gov.mtdpce.entity.ArticleCategory;
import bf.gov.mtdpce.entity.ArticleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ArticleDTO {

    private Long id;

    @NotBlank(message = "Le titre est requis")
    @Size(max = 255, message = "Le titre ne doit pas dépasser 255 caractères")
    private String title;

    @NotBlank(message = "Le résumé est requis")
    @Size(max = 500, message = "Le résumé ne doit pas dépasser 500 caractères")
    private String summary;

    @NotBlank(message = "Le contenu est requis")
    private String content;

    private String featuredImage;
    private ArticleCategory category;
    private ArticleStatus status;
    private Integer viewCount;
    private Boolean featured;
    private String authorName;
    private Long authorId;
    private LocalDateTime publishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
