/*
 * This software and associated documentation files are
 *
 * Copyright © 2020-2020 Koninklijke Philips N.V.
 *
 * and is made available for use within Philips and/or within Philips products.
 *
 * All Rights Reserved
 */

package com.philips.research.collector.core.domain;

import com.philips.research.collector.core.BusinessException;
import com.philips.research.collector.core.ProjectService;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProjectInteractorTest {
    private static final String TITLE = "Title";
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final URL VALID_SPDX = ProjectInteractorTest.class.getResource("/valid.spdx");
    private static final UUID UNKNOWN_UUID = UUID.randomUUID();
    private static final String REFERENCE = "My/Reference";
    private static final String VERSION = "Version";
    private static final URI PURL = URI.create(String.format("%s@%s", REFERENCE, VERSION));
    private static final PackageDefinition PACKAGE = new PackageDefinition(REFERENCE);

    private final ProjectStore store = mock(ProjectStore.class);
    private final ProjectService interactor = new ProjectInteractor(store);

    @Test
    void listsProjects() {
        when(store.getProjects()).thenReturn(List.of(new Project(PROJECT_ID)));

        final var projects = interactor.projects();

        assertThat(projects.get(0).id).isEqualTo(PROJECT_ID);
    }

    @Test
    void createsProject() {
        var project = new Project(PROJECT_ID);
        when(store.createProject()).thenReturn(project);

        final var dto = interactor.createProject(TITLE);

        assertThat(dto.id).isEqualTo(PROJECT_ID);
        assertThat(dto.title).isEqualTo(TITLE);
    }

    @Test
    void readsProject() {
        var project = new Project(PROJECT_ID);
        when(store.readProject(PROJECT_ID)).thenReturn(Optional.of(project));

        final var dto = interactor.getProject(PROJECT_ID);

        assertThat(dto.id).isEqualTo(PROJECT_ID);
    }

    @Test
    void readProjectDependencies() {
        final var project = new Project(PROJECT_ID);
        project.addDependency(new Dependency(PACKAGE, VERSION));
        when(store.readProject(PROJECT_ID)).thenReturn(Optional.of(project));

        final var dtos = interactor.getDependencies(PROJECT_ID);

        assertThat(dtos).hasSize(1);
        assertThat(dtos.get(0).reference).isEqualTo(REFERENCE + '@' + VERSION);
    }

    @Test
    void readsProjectDependencyByReference() {
        final var project = new Project(PROJECT_ID);
        when(store.readProject(PROJECT_ID)).thenReturn(Optional.of(project));
        project.addDependency(new Dependency(null, VERSION));
        project.addDependency(new Dependency(PACKAGE, VERSION));

        final var dto = interactor.getDependency(PROJECT_ID, PURL);

        assertThat(dto.reference).isEqualTo(PURL.toString());
    }

    @Nested
    class SpdxImport {
        @Test
        void throws_importForUnknownProject() {
            when(store.readProject(UNKNOWN_UUID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> interactor.importSpdx(UNKNOWN_UUID, null))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining(UNKNOWN_UUID.toString());
        }

        @Test
        void importsProject() throws Exception {
            final var project = new Project(PROJECT_ID);
            when(store.readProject(PROJECT_ID)).thenReturn(Optional.of(project));

            try (InputStream stream = VALID_SPDX.openStream()) {
                interactor.importSpdx(PROJECT_ID, stream);
            }

            assertThat(project.getDependencies()).isNotEmpty();
        }
    }
}
