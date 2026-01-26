package bf.gov.mtdpce.repository;

import bf.gov.mtdpce.entity.Structure;
import bf.gov.mtdpce.entity.StructureType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StructureRepository extends JpaRepository<Structure, Long> {
    
    Page<Structure> findByIsActiveTrueOrderByDisplayOrderAsc(Pageable pageable);
    
    List<Structure> findByParentIsNullAndIsActiveTrueOrderByDisplayOrderAsc();
    
    List<Structure> findByParentIdAndIsActiveTrueOrderByDisplayOrderAsc(Long parentId);
    
    List<Structure> findByStructureTypeAndIsActiveTrueOrderByDisplayOrderAsc(StructureType type);
    
    @Query("SELECT s FROM Structure s WHERE s.isActive = true AND " +
           "(LOWER(s.name) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.acronym) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
           "LOWER(s.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Structure> searchStructures(String query, Pageable pageable);
    
    long countByIsActiveTrue();
    
    long countByStructureTypeAndIsActiveTrue(StructureType type);
}
