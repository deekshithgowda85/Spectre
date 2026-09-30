package com.example.dashboard_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.dashboard_service.dto.ProjectDtos.CreateRequest;
import com.example.dashboard_service.repository.ProjectRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProjectServiceTest {
    @Test
    void normalizesAndStoresWebsiteUrl() {
        ProjectRepository repository = Mockito.mock(ProjectRepository.class);
        AuditEventService auditEvents = Mockito.mock(AuditEventService.class);
        ProjectCheckService projectChecks = Mockito.mock(ProjectCheckService.class);
        ProjectService service = new ProjectService(repository, auditEvents, projectChecks);
        when(repository.existsByOwnerIdAndUrl("user", "https://example.com/")).thenReturn(false);
        when(repository.save(Mockito.any())).thenAnswer(invocation -> invocation.getArgument(0));
        service.create(new CreateRequest("Portfolio", " https://example.com/ "), "user");
        verify(repository).save(Mockito.argThat(project -> project.getUrl().equals("https://example.com/")));
        verify(auditEvents).recordProjectCreated("user", "null");
        verify(projectChecks).check(Mockito.any());
    }

    @Test
    void rejectsDuplicateWebsiteForOwner() {
        ProjectRepository repository = Mockito.mock(ProjectRepository.class);
        ProjectService service = new ProjectService(repository, Mockito.mock(AuditEventService.class),
                Mockito.mock(ProjectCheckService.class));
        when(repository.existsByOwnerIdAndUrl("user", "https://example.com")).thenReturn(true);
        IllegalStateException error = assertThrows(IllegalStateException.class,
                () -> service.create(new CreateRequest("Portfolio", "https://example.com"), "user"));
        assertEquals("This website is already being monitored.", error.getMessage());
    }

    @Test
    void scopesProjectLookupToOwner() {
        ProjectRepository repository = Mockito.mock(ProjectRepository.class);
        ProjectService service = new ProjectService(repository, Mockito.mock(AuditEventService.class),
                Mockito.mock(ProjectCheckService.class));
        when(repository.findByIdAndOwnerId(Mockito.any(), Mockito.eq("other"))).thenReturn(Optional.empty());
        assertThrows(IllegalArgumentException.class, () -> service.get(java.util.UUID.randomUUID(), "other"));
    }
}
