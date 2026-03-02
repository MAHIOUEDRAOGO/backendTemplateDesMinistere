package bf.gov.mtdpce.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "ministre")
@Data
public class Ministre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String nom;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String prenom;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String profession;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String biographie;

    @Column(columnDefinition = "TEXT")
    private String photo;

    @Column(name = "is_actif")
    private Boolean isActif = false;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ministere_id", nullable = false)
    private Ministere ministere;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

}
