package com.ecotrack.service;

import com.ecotrack.entity.Organization;
import com.ecotrack.entity.OrganizationMember;
import com.ecotrack.entity.User;
import com.ecotrack.repository.ActivityRepository;
import com.ecotrack.repository.OrganizationMemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {
    @Mock
    private OrganizationMemberRepository members;

    @Mock
    private ActivityRepository activities;

    @InjectMocks
    private OrganizationService service;

    @Test
    void dashboardAggregatesMemberEmissions() {
        User user = new User();
        user.setId(4L);
        user.setName("Person");
        Organization organization = new Organization();
        organization.setId(2L);
        organization.setName("Eco Team");
        OrganizationMember member = new OrganizationMember();
        member.setUser(user);
        member.setOrganization(organization);
        member.setRole("MEMBER");
        when(members.findFirstByUserEmail("person@example.com")).thenReturn(Optional.of(member));
        when(members.findByOrganizationId(2L)).thenReturn(List.of(member));
        when(activities.memberCategoryTotals(eq(List.of(4L)), any(), any()))
                .thenReturn(List.<Object[]>of(new Object[]{"TRANSPORT", 30.0}));
        org.mockito.Mockito.doReturn(List.<Object[]>of(new Object[]{4L, "Person", 30.0}),
            List.<Object[]>of(new Object[]{4L, "Person", 20.0}))
            .when(activities).memberTotals(eq(List.of(4L)), any(), any());

        var result = service.dashboard("person@example.com");

        assertEquals("Eco Team", result.get("organizationName"));
        assertEquals(30.0, result.get("totalCo2e"));
        assertEquals(20.0, result.get("previousMonthCo2e"));
        assertEquals(-50.0, result.get("monthlyReductionPercentage"));
        assertEquals(1, result.get("employeeCount"));
        assertEquals(30.0, result.get("averageEmployeeCo2e"));
    }

    @Test
    void membersReturnsTheOrganizationMemberProjection() {
        User user = new User();
        user.setId(4L);
        user.setName("Person");
        user.setEmail("person@example.com");
        Organization organization = new Organization();
        organization.setId(2L);
        OrganizationMember member = new OrganizationMember();
        member.setUser(user);
        member.setOrganization(organization);
        member.setRole("ADMIN");
        when(members.findFirstByUserEmail("person@example.com")).thenReturn(Optional.of(member));
        when(members.findByOrganizationId(2L)).thenReturn(List.of(member));

        var result = service.members("person@example.com");

        assertEquals(1, result.size());
        assertEquals("Person", result.get(0).get("name"));
        assertEquals("person@example.com", result.get(0).get("email"));
        assertEquals("ADMIN", result.get(0).get("role"));
    }

    @Test
    void dashboardRejectsUsersWithoutMembership() {
        when(members.findFirstByUserEmail("person@example.com")).thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> service.dashboard("person@example.com"));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}