package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.EquipmentRequest;
import com.app.eventnexus.dtos.responses.EquipmentResponse;
import com.app.eventnexus.dtos.responses.LoadoutResponse;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.Equipment;
import com.app.eventnexus.models.EquipmentLoadout;
import com.app.eventnexus.models.Organization;
import com.app.eventnexus.models.Team;
import com.app.eventnexus.models.Tournament;
import com.app.eventnexus.models.Venue;
import com.app.eventnexus.repositories.EquipmentLoadoutRepository;
import com.app.eventnexus.repositories.EquipmentRepository;
import com.app.eventnexus.repositories.TeamRepository;
import com.app.eventnexus.repositories.TournamentRepository;
import com.app.eventnexus.repositories.VenueRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link EquipmentService}.
 */
@ExtendWith(MockitoExtension.class)
class EquipmentServiceTest {

    @Mock private EquipmentRepository equipmentRepository;
    @Mock private EquipmentLoadoutRepository loadoutRepository;
    @Mock private VenueRepository venueRepository;
    @Mock private TeamRepository teamRepository;
    @Mock private TournamentRepository tournamentRepository;

    @InjectMocks
    private EquipmentService equipmentService;

    private Organization org;
    private Venue venue;
    private Equipment equipment;
    private Team team;
    private Tournament tournament;

    @BeforeEach
    void setUp() {
        org = new Organization();
        org.setId(1L);

        venue = new Venue("Main Hall", "Addr", 8, org);
        venue.setId(5L);

        equipment = new Equipment(venue, "Gaming PC", "PC", "SN-001");
        equipment.setId(20L);
        equipment.setAvailable(true);

        team = new Team();
        team.setId(2L);
        team.setName("Alpha");
        team.setTag("ALP");

        tournament = new Tournament();
        tournament.setId(10L);
    }

    // ─── Equipment CRUD ────────────────────────────────────────────────────────

    @Test
    void findAll_returnsAllEquipment() {
        when(equipmentRepository.findAll()).thenReturn(List.of(equipment));

        List<EquipmentResponse> result = equipmentService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Gaming PC");
    }

    @Test
    void findById_returnsEquipment_whenExists() {
        when(equipmentRepository.findById(20L)).thenReturn(Optional.of(equipment));

        EquipmentResponse result = equipmentService.findById(20L);

        assertThat(result.getId()).isEqualTo(20L);
    }

    @Test
    void findById_throwsResourceNotFoundException_whenMissing() {
        when(equipmentRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipmentService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Equipment");
    }

    @Test
    void findByVenueId_throwsResourceNotFoundException_whenVenueMissing() {
        when(venueRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> equipmentService.findByVenueId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venue");
    }

    @Test
    void create_succeeds_withUniqueSerialNumber() {
        EquipmentRequest request = new EquipmentRequest();
        request.setVenueId(5L);
        request.setName("Monitor");
        request.setCategory("Display");
        request.setSerialNumber("SN-999");

        when(venueRepository.findById(5L)).thenReturn(Optional.of(venue));
        when(equipmentRepository.findBySerialNumber("SN-999")).thenReturn(Optional.empty());
        when(equipmentRepository.save(any(Equipment.class))).thenAnswer(inv -> {
            Equipment e = inv.getArgument(0);
            e.setId(30L);
            return e;
        });

        EquipmentResponse result = equipmentService.create(request);

        assertThat(result.getName()).isEqualTo("Monitor");
    }

    @Test
    void create_throwsConflictException_whenSerialNumberAlreadyRegistered() {
        EquipmentRequest request = new EquipmentRequest();
        request.setVenueId(5L);
        request.setName("Duplicate");
        request.setCategory("PC");
        request.setSerialNumber("SN-001");

        when(venueRepository.findById(5L)).thenReturn(Optional.of(venue));
        when(equipmentRepository.findBySerialNumber("SN-001")).thenReturn(Optional.of(equipment));

        assertThatThrownBy(() -> equipmentService.create(request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("SN-001");

        verify(equipmentRepository, never()).save(any());
    }

    @Test
    void create_throwsResourceNotFoundException_whenVenueMissing() {
        EquipmentRequest request = new EquipmentRequest();
        request.setVenueId(99L);
        request.setName("PC");
        request.setCategory("PC");

        when(venueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> equipmentService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venue");
    }

    @Test
    void update_throwsConflictException_whenNewSerialNumberTakenByDifferentItem() {
        Equipment other = new Equipment(venue, "Other PC", "PC", "SN-002");
        other.setId(21L);

        EquipmentRequest request = new EquipmentRequest();
        request.setName("Updated");
        request.setCategory("PC");
        request.setSerialNumber("SN-002");

        when(equipmentRepository.findById(20L)).thenReturn(Optional.of(equipment));
        when(equipmentRepository.findBySerialNumber("SN-002")).thenReturn(Optional.of(other));

        assertThatThrownBy(() -> equipmentService.update(20L, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("SN-002");
    }

    @Test
    void delete_succeeds_whenNoActiveLoadouts() {
        when(equipmentRepository.existsById(20L)).thenReturn(true);
        when(loadoutRepository.countActiveByEquipmentId(20L)).thenReturn(0L);

        equipmentService.delete(20L);

        verify(equipmentRepository).deleteById(20L);
    }

    @Test
    void delete_throwsConflictException_whenActiveLoadoutsExist() {
        when(equipmentRepository.existsById(20L)).thenReturn(true);
        when(loadoutRepository.countActiveByEquipmentId(20L)).thenReturn(1L);

        assertThatThrownBy(() -> equipmentService.delete(20L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("active loadout");

        verify(equipmentRepository, never()).deleteById(any());
    }

    @Test
    void delete_throwsResourceNotFoundException_whenEquipmentMissing() {
        when(equipmentRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> equipmentService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    // ─── Loadout operations ────────────────────────────────────────────────────

    @Test
    void assignLoadout_succeeds_whenEquipmentAvailable() {
        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(team));
        when(equipmentRepository.findById(20L)).thenReturn(Optional.of(equipment));
        when(loadoutRepository.findActiveByEquipmentIdAndTournamentId(20L, 10L))
                .thenReturn(Optional.empty());

        EquipmentLoadout loadout = new EquipmentLoadout(equipment, team, tournament);
        loadout.setId(100L);
        when(loadoutRepository.save(any(EquipmentLoadout.class))).thenReturn(loadout);

        LoadoutResponse result = equipmentService.assignLoadout(10L, 2L, 20L);

        assertThat(result).isNotNull();
        verify(loadoutRepository).save(any(EquipmentLoadout.class));
    }

    @Test
    void assignLoadout_throwsConflictException_whenEquipmentUnavailable() {
        equipment.setAvailable(false);

        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(team));
        when(equipmentRepository.findById(20L)).thenReturn(Optional.of(equipment));

        assertThatThrownBy(() -> equipmentService.assignLoadout(10L, 2L, 20L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void assignLoadout_throwsConflictException_whenAlreadyAssignedInTournament() {
        EquipmentLoadout existing = new EquipmentLoadout(equipment, team, tournament);
        existing.setId(50L);

        when(tournamentRepository.findById(10L)).thenReturn(Optional.of(tournament));
        when(teamRepository.findById(2L)).thenReturn(Optional.of(team));
        when(equipmentRepository.findById(20L)).thenReturn(Optional.of(equipment));
        when(loadoutRepository.findActiveByEquipmentIdAndTournamentId(20L, 10L))
                .thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> equipmentService.assignLoadout(10L, 2L, 20L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("already assigned");
    }

    @Test
    void removeLoadout_throwsResourceNotFoundException_whenLoadoutMissing() {
        when(loadoutRepository.existsById(999L)).thenReturn(false);

        assertThatThrownBy(() -> equipmentService.removeLoadout(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("EquipmentLoadout");
    }

    @Test
    void removeLoadout_succeeds_whenLoadoutExists() {
        when(loadoutRepository.existsById(100L)).thenReturn(true);

        equipmentService.removeLoadout(100L);

        verify(loadoutRepository).deleteById(100L);
    }
}
