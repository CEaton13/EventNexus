package com.app.eventnexus.services;

import com.app.eventnexus.dtos.requests.VenueRequest;
import com.app.eventnexus.dtos.responses.VenueResponse;
import com.app.eventnexus.exceptions.ConflictException;
import com.app.eventnexus.exceptions.ResourceNotFoundException;
import com.app.eventnexus.models.Organization;
import com.app.eventnexus.models.Venue;
import com.app.eventnexus.repositories.OrganizationRepository;
import com.app.eventnexus.repositories.VenueRepository;
import com.app.eventnexus.tenant.TenantContext;
import org.junit.jupiter.api.AfterEach;
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
 * Unit tests for {@link VenueService}.
 */
@ExtendWith(MockitoExtension.class)
class VenueServiceTest {

    @Mock private VenueRepository venueRepository;
    @Mock private OrganizationRepository organizationRepository;

    @InjectMocks
    private VenueService venueService;

    private Organization org;
    private Venue venue;

    @BeforeEach
    void setUp() {
        org = new Organization();
        org.setId(1L);
        org.setName("Test Org");
        org.setSlug("test-org");

        venue = new Venue("Main Hall", "123 Arena St", 8, org);
        venue.setId(10L);

        TenantContext.setTenantId(1L);
    }

    @AfterEach
    void tearDown() {
        TenantContext.clear();
    }

    @Test
    void findAll_returnsAllVenues() {
        when(venueRepository.findAll()).thenReturn(List.of(venue));

        List<VenueResponse> result = venueService.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Main Hall");
    }

    @Test
    void findAll_returnsEmptyList_whenNoVenues() {
        when(venueRepository.findAll()).thenReturn(List.of());

        List<VenueResponse> result = venueService.findAll();

        assertThat(result).isEmpty();
    }

    @Test
    void findById_returnsVenue_whenExists() {
        when(venueRepository.findById(10L)).thenReturn(Optional.of(venue));
        when(venueRepository.findUtilizationByVenueId(10L)).thenReturn(Optional.empty());

        VenueResponse result = venueService.findById(10L);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Main Hall");
    }

    @Test
    void findById_throwsResourceNotFoundException_whenMissing() {
        when(venueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.findById(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Venue");
    }

    @Test
    void create_persistsVenueWithOrgFromTenantContext() {
        VenueRequest request = new VenueRequest();
        request.setName("New Arena");
        request.setLocation("456 Stadium Rd");
        request.setStationCount(16);

        when(organizationRepository.findById(1L)).thenReturn(Optional.of(org));
        when(venueRepository.save(any(Venue.class))).thenAnswer(inv -> {
            Venue v = inv.getArgument(0);
            v.setId(20L);
            return v;
        });

        VenueResponse result = venueService.create(request);

        assertThat(result.getName()).isEqualTo("New Arena");
        assertThat(result.getStationCount()).isEqualTo(16);
    }

    @Test
    void create_throwsResourceNotFoundException_whenTenantOrgMissing() {
        VenueRequest request = new VenueRequest();
        request.setName("Ghost Venue");
        request.setLocation("Nowhere");
        request.setStationCount(4);

        when(organizationRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.create(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Organization");
    }

    @Test
    void update_persistsNewFields() {
        VenueRequest request = new VenueRequest();
        request.setName("Renamed Hall");
        request.setLocation("789 New St");
        request.setStationCount(12);

        when(venueRepository.findById(10L)).thenReturn(Optional.of(venue));
        when(venueRepository.save(any(Venue.class))).thenAnswer(inv -> inv.getArgument(0));

        VenueResponse result = venueService.update(10L, request);

        assertThat(result.getName()).isEqualTo("Renamed Hall");
        assertThat(result.getStationCount()).isEqualTo(12);
    }

    @Test
    void update_throwsResourceNotFoundException_whenVenueMissing() {
        VenueRequest request = new VenueRequest();
        request.setName("X");
        request.setLocation("Y");
        request.setStationCount(1);

        when(venueRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> venueService.update(99L, request))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void delete_succeeds_whenNoLinkedTournaments() {
        when(venueRepository.existsById(10L)).thenReturn(true);
        when(venueRepository.countLinkedTournaments(10L)).thenReturn(0L);

        venueService.delete(10L);

        verify(venueRepository).deleteById(10L);
    }

    @Test
    void delete_throwsConflictException_whenLinkedTournamentsExist() {
        when(venueRepository.existsById(10L)).thenReturn(true);
        when(venueRepository.countLinkedTournaments(10L)).thenReturn(2L);

        assertThatThrownBy(() -> venueService.delete(10L))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("referenced by existing tournaments");

        verify(venueRepository, never()).deleteById(any());
    }

    @Test
    void delete_throwsResourceNotFoundException_whenVenueMissing() {
        when(venueRepository.existsById(99L)).thenReturn(false);

        assertThatThrownBy(() -> venueService.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class);

        verify(venueRepository, never()).deleteById(any());
    }
}
