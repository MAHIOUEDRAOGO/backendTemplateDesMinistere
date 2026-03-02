package bf.gov.mtdpce.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MinistreDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String profession;
    private String biographie;
    private String photo;
    private Boolean isActif;
    private Long ministereId;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
