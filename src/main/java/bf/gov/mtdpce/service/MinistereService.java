package bf.gov.mtdpce.service;

import bf.gov.mtdpce.dto.MinistereDTO;
import bf.gov.mtdpce.entity.Ministere;
import bf.gov.mtdpce.entity.User;
import bf.gov.mtdpce.repository.MinistereRepository;
import bf.gov.mtdpce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class MinistereService {

    @Autowired
    private MinistereRepository ministereRepository;

    @Autowired
    private UserRepository userRepository;


    public Page<MinistereDTO> getAll(Pageable pageable) {
        return ministereRepository.findAll(pageable)
                .map(this::convertToDTO);
    }

    public MinistereDTO getById(Long id) {
        Ministere ministere = ministereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ministère non trouvé"));

        return convertToDTO(ministere);
    }

    public MinistereDTO create(MinistereDTO dto, Long authorId) {

        if (ministereRepository.existsByAcronyme(dto.getAcronyme())) {
            throw new RuntimeException("Un ministère avec cet acronyme existe déjà.");
        }

        User author = userRepository.findById(authorId)
                .orElseThrow(() -> new RuntimeException("Auteur non trouvé"));

        Ministere ministere = new Ministere();
        ministere.setNomGeneral(dto.getNomGeneral());
        ministere.setNomReel(dto.getNomReel());
        ministere.setAcronyme(dto.getAcronyme());
        ministere.setLogo(dto.getLogo());

        Ministere saved = ministereRepository.save(ministere);

        return convertToDTO(saved);
    }

    public MinistereDTO update(Long id, MinistereDTO ministereDTO) {

        Ministere ministere = ministereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ministère non trouvé"));

        // Vérifie unicité acronyme si changé
        if (!ministere.getAcronyme().equals(ministereDTO.getAcronyme())
                && ministereRepository.existsByAcronyme(ministereDTO.getAcronyme())) {

            throw new RuntimeException("Un ministère avec cet acronyme existe déjà.");
        }

        ministere.setNomGeneral(ministereDTO.getNomGeneral());
        ministere.setNomReel(ministereDTO.getNomReel());
        ministere.setAcronyme(ministereDTO.getAcronyme());

        // Logo seulement si fourni
        if (ministereDTO.getLogo() != null) {
            ministere.setLogo(ministereDTO.getLogo());
        }

        Ministere updated = ministereRepository.save(ministere);

        return convertToDTO(updated);
    }

    public void delete(Long id) {

        Ministere ministere = ministereRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Ministère non trouvé"));

        ministereRepository.delete(ministere);
    }

    private MinistereDTO convertToDTO(Ministere ministere) {

        MinistereDTO dto = new MinistereDTO();
        dto.setId(ministere.getId());
        dto.setNomGeneral(ministere.getNomGeneral());
        dto.setNomReel(ministere.getNomReel());
        dto.setAcronyme(ministere.getAcronyme());
        dto.setLogo(ministere.getLogo());
        dto.setCreatedAt(ministere.getCreatedAt());
        dto.setUpdatedAt(ministere.getUpdatedAt());

        return dto;
    }
}

