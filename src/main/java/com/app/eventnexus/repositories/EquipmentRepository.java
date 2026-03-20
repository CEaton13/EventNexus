package com.app.eventnexus.repositories;

import com.app.eventnexus.models.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository for {@link Equipment} entities.
 */
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    /**
     * Returns all equipment associated with a specific venue.
     *
     * @param venueId the venue's primary key
     * @return list of equipment; never null
     */
    List<Equipment> findByVenueId(Long venueId);

    /**
     * Finds equipment by its serial number.
     *
     * @param serialNumber the unique serial number
     * @return an Optional containing the equipment, or empty if not found
     */
    Optional<Equipment> findBySerialNumber(String serialNumber);

}
