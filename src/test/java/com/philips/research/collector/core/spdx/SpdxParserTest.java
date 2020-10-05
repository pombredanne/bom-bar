/*
 * Copyright (c) 2020-2020, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.collector.core.spdx;

import com.philips.research.collector.core.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpdxParserTest {
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final String TITLE = "Name";
    private static final String REFERENCE = "maven/namespace/name";
    private static final String VERSION = "Version";
    private static final String LICENSE = "License";

    private final Project project = new Project(PROJECT_ID);
    private final ProjectStore store = mock(ProjectStore.class);

    private final SpdxParser parser = new SpdxParser(project, store);
    private final PackageDefinition pkg = new PackageDefinition(REFERENCE);

    @BeforeEach
    void beforeEach() {
        when(store.getOrCreatePackageDefinition(REFERENCE)).thenReturn(pkg);
        when(store.createDependency(any(), any())).thenAnswer(
                (a) -> new Dependency(a.getArgument(1), a.getArgument(2)));
    }

    @Test
    void addsPackageAsDependency() {
        final var spdx = spdxStream(
                "PackageName: " + TITLE,
                "PackageLicenseConcluded: " + LICENSE,
                "ExternalRef: PACKAGE-MANAGER purl pkg:" + REFERENCE + "@" + VERSION,
                "PackageVersion: Nope");

        parser.parse(spdx);

        assertThat(project.getDependencies()).hasSize(1);
        final var dependency = project.getDependencies().get(0);
        assertThat(dependency.getPackage()).contains(pkg);
        assertThat(dependency.getVersion()).isEqualTo(VERSION);
        assertThat(dependency.getTitle()).isEqualTo(TITLE);
        assertThat(dependency.getLicense()).isEqualTo(LICENSE);
    }

    @Test
    void addsAnonymousDependency() {
        final var spdx = spdxStream(
                "PackageName: " + TITLE,
                "PackageVersion: " + VERSION,
                "PackageLicenseConcluded: " + LICENSE);

        parser.parse(spdx);

        assertThat(project.getDependencies()).hasSize(1);
        final var dependency = project.getDependencies().get(0);
        assertThat(dependency.getPackage()).isEmpty();
        assertThat(dependency.getTitle()).isEqualTo(TITLE);
        assertThat(dependency.getVersion()).isEqualTo(VERSION);
        assertThat(dependency.getLicense()).isEqualTo(LICENSE);
    }

    @Test
    void replacesPackages() {
        project.addDependency(new Dependency(pkg, "Old stuff"));
        final var spdx = spdxStream(
                "PackageName: " + TITLE,
                "ExternalRef: PACKAGE-MANAGER purl pkg:" + REFERENCE + "@" + VERSION);

        parser.parse(spdx);

        assertThat(project.getDependencies()).hasSize(1);
        final var dependency = project.getDependencies().get(0);
        assertThat(dependency.getPackage()).contains(pkg);
    }

    @Test
    void createsChildRelations() {
        when(store.createRelation(any(), any()))
                .thenAnswer((a) -> new Relation(a.getArgument(0), a.getArgument(1)));

        parser.parse(spdxStream(
                "Relationship: parent DYNAMIC_LINK child",
                "Relationship: parent DEPENDS_ON child",
                "PackageName: Parent package",
                "SPDXID: parent", // Start of parent
                "ExternalRef: PACKAGE-MANAGER purl pkg:" + REFERENCE + "@1.0",
                "PackageName: Child package",
                "SPDXID: child", // Start of child
                "ExternalRef: PACKAGE-MANAGER purl pkg:" + REFERENCE + "@2.0"));

        final var parent = project.getDependencies().get(0);
        final var child = project.getDependencies().get(1);

        assertThat(child.getRelations()).isEmpty();
        assertThat(parent.getRelations()).hasSize(2);
        var relation = parent.getRelations().get(0);
        assertThat(relation.getType()).isEqualTo(Relation.Type.DYNAMIC_LINK);
        assertThat(relation.getTarget()).isEqualTo(child);
    }

    private InputStream spdxStream(String... lines) {
        final var string = String.join("\n", lines);
        return new ByteArrayInputStream(string.getBytes());
    }
}
