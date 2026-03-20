package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.EquipmentRequest;
import com.app.eventnexus.dtos.responses.EquipmentResponse;
import com.app.eventnexus.dtos.responses.LoadoutResponse;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.Equipment;
import com.app.eventnexus.models.EquipmentLoadout;
import com.app.eventnexus.models.Team;
import com.app.eventnexus.models.Tournament;
import com.app.eventnexus.models.Venue;
import com.app.eventnexus.repositories.EquipmentLoadoutRepository;
import com.app.eventnexus.repositories.EquipmentRepository;
import com.app.eventnexus.repositories.TeamRepository;
import com.app.eventnexus.repositories.TournamentRepository;
import com.app.eventnexus.repositories.VenueRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Service for managing equipment and equipment loadout assignments.
 *
 * <p>Business rules enforced here:
 * <ul>
 *   <li>Equipment cannot be assigned if it is already marked as unavailable
 *       (i.e. it already has an active loadout for another tournament).</li>
 *   <li>Each piece of equipment can be assigned to at most one team per tournament
 *       — enforced by the unique DB constraint and a pre-check here.</li>
 *   <li>Equipment cannot be deleted while it has any active (non-returned) loadouts.</li>
 * </ul>
 *
 * <p>The {@code sync_equipment_availability} PostgreSQL trigger automatically
 * keeps {@code equipment.is_available} in sync with the {@code equipment_loadouts}
 * table — the service reads that flag but does not write it directly.
 */
@Service
public class EquipmentService {

    private final EquipmentRepository equipmentRepository;
    private final EquipmentLoadoutRepository loadoutRepository;
    private final VenueRepository venueRepository;
    private final TeamRepository teamRepository;
    private final TournamentRepository tournamentRepository;

    public EquipmentService(EquipmentRepository equipmentRepository,
                            EquipmentLoadoutRepository loadoutRepository,
                            VenueRepository venueRepository,
                            TeamRepository teamRepository,
                            TournamentRepository tournamentRepository) {
        this.equipmentRepository = equipmentRepository;
        this.loadoutRepository = loadoutRepository;
        this.venueRepository = venueRepository;
        this.teamRepository = teamRepository;
        this.tournamentRepository = tournamentRepository;
    }

    // ─── Equipment CRUD ────────────────────────────────────────────────────────

    /**
     * Returns all equipment items across all venues.
     *
     * @return list of all equipment as response DTOs; never null
     */
    @Transactional(readOnly = true)
    public List<EquipmentResponse> findAll() {
        return equipmentRepository.findAll()
                .stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    /**
     * Returns a single piece of equipment by its ID.
     *
     * @param id the equipment's primary key
     * @return the equipment as a response DTO
     * @throws ResourceNotFoundException if no equipment exists with the given ID
     */
    @Transactional(readOnly = true)
    public EquipmentResponse findById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));
        return EquipmentResponse.from(equipment);
    }

    /**
     * Returns all equipment associated with a specific venue.
     *
     * @param venueId the venue's primary key
     * @return list of equipment for that venue; never null
     * @throws ResourceNotFoundException if no venue exists with the given ID
     */
    @Transactional(readOnly = true)
    public List<EquipmentResponse> findByVenueId(Long venueId) {
        if (!venueRepository.existsById(venueId)) {
            throw new ResourceNotFoundException("Venue", venueId);
        }
        return equipmentRepository.findByVenueId(venueId)
                .stream()
                .map(EquipmentResponse::from)
                .toList();
    }

    /**
     * Creates a new piece of equipment associated with a venue.
     *
     * @param request equipment details (venueId, name, category, serialNumber)
     * @return the newly created equipment as a response DTO
     * @throws ResourceNotFoundException if the referenced venue does not exist
     * @throws ConflictException         if the serial number is already in use
     */
    @Transactional
    public EquipmentResponse create(EquipmentRequest request) {
        Venue venue = venueRepository.findById(request.getVenueId())
                .orElseThrow(() -> new ResourceNotFoundException("Venue", request.getVenueId()));

        if (request.getSerialNumber() != null
                && equipmentRepository.findBySerialNumber(request.getSerialNumber()).isPresent()) {
            throw new ConflictException(
                    "Serial number '" + request.getSerialNumber() + "' is already registered.");
        }

        Equipment equipment = new Equipment(
                venue,
                request.getName(),
                request.getCategory(),
                request.getSerialNumber());

        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    /**
     * Updates the mutable fields of an existing piece of equipment.
     * The venue association cannot be changed via this method.
     *
     * @param id      the equipment's primary key
     * @param request updated values
     * @return the updated equipment as a response DTO
     * @throws ResourceNotFoundException if no equipment exists with the given ID
     * @throws ConflictException         if the new serial number is already taken by another item
     */
    @Transactional
    public EquipmentResponse update(Long id, EquipmentRequest request) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", id));

        if (request.getSerialNumber() != null
                && !request.getSerialNumber().equals(equipment.getSerialNumber())) {
            equipmentRepository.findBySerialNumber(request.getSerialNumber()).ifPresent(existing -> {
                if (!existing.getId().equals(id)) {
                    throw new ConflictException(
                            "Serial number '" + request.getSerialNumber() + "' is already registered.");
                }
            });
        }

        equipment.setName(request.getName());
        equipment.setCategory(request.getCategory());
        equipment.setSerialNumber(request.getSerialNumber());
        equipment.setUpdatedAt(LocalDateTime.now());

        return EquipmentResponse.from(equipmentRepository.save(equipment));
    }

    /**
     * Deletes a piece of equipment by its ID.
     * Deletion is blocked if the equipment currently has any active (non-returned) loadouts.
     *
     * @param id the equipment's primary key
     * @throws ResourceNotFoundException if no equipment exists with the given ID
     * @throws ConflictException         if the equipment has active loadout assignments
     */
    @Transactional
    public void delete(Long id) {
        if (!equipmentRepository.existsById(id)) {
            throw new ResourceNotFoundException("Equipment", id);
        }
        if (loadoutRepository.countActiveByEquipmentId(id) > 0) {
            throw new ConflictException(
                    "Equipment " + id + " cannot be deleted while it has active loadout assignments.");
        }
        equipmentRepository.deleteById(id);
    }

    // ─── Loadout Operations ────────────────────────────────────────────────────

    /**
     * Assigns a piece of equipment to a team for a specific tournament.
     *
     * <p>The equipment must be currently available (not assigned elsewhere),
     * and must not already be assigned to another team for this tournament.
     * The {@code sync_equipment_availability} trigger will set
     * {@code equipment.is_available = false} after the INSERT.
     *
     * @param tournamentId the tournament's primary key
     * @param teamId       the team's primary key
     * @param equipmentId  the equipment's primary key
     * @return the new loadout as a response DTO
     * @throws ResourceNotFoundException if any of the referenced entities do not exist
     * @throws ConflictException         if the equipment is unavailable or already assigned in this tournament
     */
    @Transactional
    public LoadoutResponse assignLoadout(Long tournamentId, Long teamId, Long equipmentId) {
        Tournament tournament = tournamentRepository.findById(tournamentId)
                .orElseThrow(() -> new ResourceNotFoundException("Tournament", tournamentId));
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new ResourceNotFoundException("Team", teamId));
        Equipment equipment = equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipment", equipmentId));

        if (!equipment.isAvailable()) {
            throw new ConflictException(
                    "Equipment " + equipmentId + " is not available — it is currently assigned to another tournament.");
        }

        loadoutRepository.findActiveByEquipmentIdAndTournamentId(equipmentId, tournamentId)
                .ifPresent(existing -> {
                    throw new ConflictException(
                            "Equipment " + equipmentId + " is already assigned in tournament " + tournamentId + ".");
                });

        EquipmentLoadout loadout = new EquipmentLoadout(equipment, team, tournament);
        return LoadoutResponse.from(loadoutRepository.save(loadout));
    }

    /**
     * Removes an equipment loadout assignment, making the equipment available again.
     *
     * <p>Deletion of the loadout row triggers the {@code sync_equipment_availability}
     * DB trigger, which sets {@code equipment.is_available = true}.
     *
     * @param loadoutId the loadout's primary key
     * @throws ResourceNotFoundException if no loadout exists with the given ID
     */
    @Transactional
    public void removeLoadout(Long loadoutId) {
        if (!loadoutRepository.existsById(loadoutId)) {
            throw new ResourceNotFoundException("EquipmentLoadout", loadoutId);
        }
        loadoutRepository.deleteById(loadoutId);
    }

    /**
     * Returns all equipment loadout assignments for a given tournament.
     *
     * @param tournamentId the tournament's primary key
     * @return list of loadouts; never null
     * @throws ResourceNotFoundException if no tournament exists with the given ID
     */
    @Transactional(readOnly = true)
    public List<LoadoutResponse> getLoadoutsForTournament(Long tournamentId) {
        if (!tournamentRepository.existsById(tournamentId)) {
            throw new ResourceNotFoundException("Tournament", tournamentId);
        }
        return loadoutRepository.findByTournamentId(tournamentId)
                .stream()
                .map(LoadoutResponse::from)
                .toList();
    }

    /**
     * Returns all equipment loadout assignments for a specific team in a given tournament.
     *
     * @param tournamentId the tournament's primary key
     * @param teamId       the team's primary key
     * @return list of loadouts for that team; never null
     */
    @Transactional(readOnly = true)
    public List<LoadoutResponse> getLoadoutsForTeam(Long tournamentId, Long teamId) {
        return loadoutRepository.findByTournamentIdAndTeamId(tournamentId, teamId)
                .stream()
                .map(LoadoutResponse::from)
                .toList();
    }
}
