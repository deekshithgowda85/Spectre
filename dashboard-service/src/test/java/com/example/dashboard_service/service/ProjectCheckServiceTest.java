package com.example.dashboard_service.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.sun.net.httpserver.HttpServer;
import com.example.dashboard_service.model.Project;
import com.example.dashboard_service.model.ProjectCheck;
import com.example.dashboard_service.repository.ProjectCheckRepository;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class ProjectCheckServiceTest {
    @Test
    void persistsOneCheckForEachHealthPath() throws Exception {
        ProjectCheckRepository repository = Mockito.mock(ProjectCheckRepository.class);
        List<ProjectCheck> saved = new ArrayList<>();
        when(repository.save(any(ProjectCheck.class))).thenAnswer(invocation -> {
            saved.add(invocation.getArgument(0));
            return invocation.getArgument(0);
        });

        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/", exchange -> {
            exchange.sendResponseHeaders(200, -1);
            exchange.close();
        });
        server.start();
        try {
            String url = "http://localhost:" + server.getAddress().getPort();
            new ProjectCheckService(repository).check(new Project("Site", url, "owner"));
        } finally {
            server.stop(0);
        }

        verify(repository, Mockito.times(3)).save(any(ProjectCheck.class));
        assertEquals(List.of("/health", "/actuator/health", "/"), saved.stream().map(ProjectCheck::getPath).toList());
        assertEquals(List.of(ProjectCheck.CheckStatus.UP, ProjectCheck.CheckStatus.UP, ProjectCheck.CheckStatus.UP),
                saved.stream().map(ProjectCheck::getStatus).toList());
    }
}