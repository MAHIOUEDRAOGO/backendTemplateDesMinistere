package bf.gov.mtdpce.event;

import bf.gov.mtdpce.entity.Article;
import lombok.Getter;

@Getter
public class ArticlePublishedEvent {

    private final Article article;

    public ArticlePublishedEvent(Article article) {
        this.article = article;
    }
}
