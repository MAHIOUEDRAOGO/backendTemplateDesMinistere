package bf.gov.mtdpce.service;

import bf.gov.mtdpce.dto.ArticleDTO;
import bf.gov.mtdpce.entity.Article;
import bf.gov.mtdpce.entity.ArticleCategory;
import bf.gov.mtdpce.entity.ArticleStatus;
import bf.gov.mtdpce.entity.User;
import bf.gov.mtdpce.exception.ResourceNotFoundException;
import bf.gov.mtdpce.repository.ArticleRepository;
import bf.gov.mtdpce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ArticleService {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private UserRepository userRepository;

    public Page<ArticleDTO> getAllArticles(Pageable pageable) {
        return articleRepository.findAll(pageable).map(this::convertToDTO);
    }

    public Page<ArticleDTO> getPublishedArticles(Pageable pageable) {
        return articleRepository.findByStatus(ArticleStatus.PUBLISHED, pageable).map(this::convertToDTO);
    }

    public Page<ArticleDTO> getArticlesByCategory(ArticleCategory category, Pageable pageable) {
        return articleRepository.findByStatusAndCategory(ArticleStatus.PUBLISHED, category, pageable)
                .map(this::convertToDTO);
    }

    public Page<ArticleDTO> searchPublishedArticles(String search, Pageable pageable) {
        return articleRepository.searchPublishedArticles(search, ArticleStatus.PUBLISHED, pageable)
                .map(this::convertToDTO);
    }

    public List<ArticleDTO> getLatestArticles() {
        return articleRepository.findTop5ByStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public List<ArticleDTO> getFeaturedArticles() {
        return articleRepository.findByFeaturedTrueAndStatusOrderByPublishedAtDesc(ArticleStatus.PUBLISHED)
                .stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public ArticleDTO getArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
        return convertToDTO(article);
    }

    @Transactional
    public ArticleDTO getPublishedArticleById(Long id) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));
        
        if (article.getStatus() != ArticleStatus.PUBLISHED) {
            throw new ResourceNotFoundException("Article", "id", id);
        }
        
        // Increment view count
        article.setViewCount(article.getViewCount() + 1);
        articleRepository.save(article);
        
        return convertToDTO(article);
    }

    @Transactional
    public ArticleDTO createArticle(ArticleDTO articleDTO, Long authorId) {
        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur", "id", authorId));

        Article article = Article.builder()
                .title(articleDTO.getTitle())
                .summary(articleDTO.getSummary())
                .content(articleDTO.getContent())
                .featuredImage(articleDTO.getFeaturedImage())
                .category(articleDTO.getCategory() != null ? articleDTO.getCategory() : ArticleCategory.ACTUALITE)
                .status(articleDTO.getStatus() != null ? articleDTO.getStatus() : ArticleStatus.DRAFT)
                .featured(articleDTO.getFeatured() != null ? articleDTO.getFeatured() : false)
                .author(author)
                .viewCount(0)
                .build();

        if (article.getStatus() == ArticleStatus.PUBLISHED) {
            article.setPublishedAt(LocalDateTime.now());
        }

        return convertToDTO(articleRepository.save(article));
    }

    @Transactional
    public ArticleDTO updateArticle(Long id, ArticleDTO articleDTO) {
        Article article = articleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Article", "id", id));

        if (articleDTO.getTitle() != null) article.setTitle(articleDTO.getTitle());
        if (articleDTO.getSummary() != null) article.setSummary(articleDTO.getSummary());
        if (articleDTO.getContent() != null) article.setContent(articleDTO.getContent());
        if (articleDTO.getFeaturedImage() != null) article.setFeaturedImage(articleDTO.getFeaturedImage());
        if (articleDTO.getCategory() != null) article.setCategory(articleDTO.getCategory());
        if (articleDTO.getFeatured() != null) article.setFeatured(articleDTO.getFeatured());

        if (articleDTO.getStatus() != null) {
            if (articleDTO.getStatus() == ArticleStatus.PUBLISHED && article.getStatus() != ArticleStatus.PUBLISHED) {
                article.setPublishedAt(LocalDateTime.now());
            }
            article.setStatus(articleDTO.getStatus());
        }

        return convertToDTO(articleRepository.save(article));
    }

    @Transactional
    public void deleteArticle(Long id) {
        if (!articleRepository.existsById(id)) {
            throw new ResourceNotFoundException("Article", "id", id);
        }
        articleRepository.deleteById(id);
    }

    public Long countPublishedArticles() {
        return articleRepository.countByStatus(ArticleStatus.PUBLISHED);
    }

    private ArticleDTO convertToDTO(Article article) {
        return ArticleDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .summary(article.getSummary())
                .content(article.getContent())
                .featuredImage(article.getFeaturedImage())
                .category(article.getCategory())
                .status(article.getStatus())
                .viewCount(article.getViewCount())
                .featured(article.getFeatured())
                .authorName(article.getAuthor().getFirstName() + " " + article.getAuthor().getLastName())
                .authorId(article.getAuthor().getId())
                .publishedAt(article.getPublishedAt())
                .createdAt(article.getCreatedAt())
                .updatedAt(article.getUpdatedAt())
                .build();
    }
}
