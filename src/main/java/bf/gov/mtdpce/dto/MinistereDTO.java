package bf.gov.mtdpce.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MinistereDTO {
    private Long id;
    private String nomGeneral;
    private String nomReel;
    private String acronyme;
    private String logo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
