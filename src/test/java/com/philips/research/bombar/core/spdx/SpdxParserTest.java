/*
 * Copyright (c) 2020-2021, Koninklijke Philips N.V., https://www.philips.com
 * SPDX-License-Identifier: MIT
 */

package com.philips.research.bombar.core.spdx;

import com.philips.research.bombar.core.PersistentStore;
import com.philips.research.bombar.core.domain.Dependency;
import com.philips.research.bombar.core.domain.Package;
import com.philips.research.bombar.core.domain.Project;
import com.philips.research.bombar.core.domain.Relation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SpdxParserTest {
    private static final UUID PROJECT_ID = UUID.randomUUID();
    private static final String TITLE = "Name";
    private static final URI REFERENCE = URI.create("maven/namespace/name");
    private static final String VERSION = "Version";
    private static final String LICENSE = "License";

    private final Project project = new Project(PROJECT_ID);
    private final PersistentStore store = mock(PersistentStore.class);

    private final SpdxParser parser = new SpdxParser(project, store);
    private final Package pkg = new Package(REFERENCE);

    @BeforeEach
    void beforeEach() {
        //noinspection unchecked
        when(store.getPackageDefinition(REFERENCE)).thenReturn(Optional.empty(), Optional.of(pkg));
        when(store.createPackageDefinition(REFERENCE)).thenReturn(pkg);
        when(store.createDependency(eq(project), any(), any())).thenAnswer(
                (a) -> new Dependency(a.getArgument(1), a.getArgument(2)));
    }

    @Test
    void setsUpdateTimestamp() {
        final var iso = "2010-01-29T18:30:22Z";
        final var spdx = spdxStream("Created: " + iso);

        parser.parse(spdx);

        assertThat(project.getLastUpdate()).contains(Instant.parse(iso));
    }

    @Test
    void setsInitialProjectTitle() {
        final var spdx = spdxStream(
                "DocumentName: " + TITLE
        );

        parser.parse(spdx);

        assertThat(project.getTitle()).isEqualTo(TITLE);
    }

    @Test
    void ignoresProjectTitle_alreadySet() {
        project.setTitle(TITLE);
        final var spdx = spdxStream(
                "DocumentName: Something else"
        );

        parser.parse(spdx);

        assertThat(project.getTitle()).isEqualTo(TITLE);
    }

    @Test
    void addsPackageAsDependency() {
        final var spdx = spdxStream(
                "PackageName: " + TITLE,
                "SPDXID: package",
                "PackageLicenseConcluded: " + LICENSE,
                "ExternalRef: PACKAGE-MANAGER purl pkg:" + REFERENCE + "@" + VERSION,
                "PackageVersion: Nope");

        parser.parse(spdx);

        assertThat(project.getDependencies()).hasSize(1);
        //noinspection OptionalGetWithoutIsPresent
        final var dependency = project.getDependency("package").get();
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
        final var dependency = project.getDependencies().iterator().next();
        assertThat(dependency.getKey()).isNotBlank();
        assertThat(dependency.getPackage()).isEmpty();
        assertThat(dependency.getTitle()).isEqualTo(TITLE);
        assertThat(dependency.getVersion()).isEqualTo(VERSION);
        assertThat(dependency.getLicense()).isEqualTo(LICENSE);
    }

    @Test
    void replacesPackages() {
        project.addDependency(new Dependency("Old", "Old stuff"));
        final var spdx = spdxStream(
                "PackageName: " + TITLE,
                "SPDXID: package");

        parser.parse(spdx);

        assertThat(project.getDependencies()).hasSize(1);
        assertThat(project.getDependency("package")).isNotEmpty();
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
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

        final var parent = project.getDependency("parent").get();
        final var child = project.getDependency("child").get();
        assertThat(child.getRelations()).isEmpty();
        assertThat(parent.getRelations()).hasSize(2);
        var relation = parent.getRelations().stream()
                .filter(r -> r.getType().equals(Relation.Relationship.DYNAMIC_LINK))
                .findFirst().get();
        assertThat(relation.getTarget()).isEqualTo(child);
        assertThat(child.getUsages()).contains(parent);
        assertThat(parent.getUsages()).isEmpty();
    }

    @Test
    @SuppressWarnings("OptionalGetWithoutIsPresent")
    void expandsNonSpdxLicenses() {
        parser.parse(spdxStream(
                "PackageName: Custom license",
                "SPDXID: 1",
                "PackageLicenseConcluded: Apache-2.0 OR (MIT AND LicenseRef-Custom) OR LicenseRef-Custom",
                "PackageName: Broken",
                "SPDXID: 2",
                "PackageLicenseConcluded: LicenseRef-Broken",
                "LicenseID: LicenseRef-Custom",
                "LicenseName: Name"));

        final var dependency = project.getDependency("1").get();
        final var broken = project.getDependency("2").get();
        assertThat(dependency.getLicense()).isEqualTo("Apache-2.0 OR (MIT AND \"Name\") OR \"Name\"");
        assertThat(broken.getLicense()).isEqualTo("\"LicenseRef-Broken\"");
    }

    @Test
    void copiesMissingPackageInformation() throws Exception {
        parser.parse(spdxStream(
                "PackageName: Name",
                "SPDXID: 1",
                "ExternalRef: PACKAGE-MANAGER purl pkg:" + REFERENCE + "@1.0",
                "PackageHomePage: http://example.com",
                "PackageSupplier: Vendor",
                "PackageSummary: <text>Summary</text>"));

        //noinspection OptionalGetWithoutIsPresent
        final var pkg = project.getDependency("1").get().getPackage().get();
        assertThat(pkg.getName()).isEqualTo("Name");
        assertThat(pkg.getHomepage()).contains(new URL("http://example.com"));
        assertThat(pkg.getVendor()).contains("Vendor");
        assertThat(pkg.getDescription()).contains("Summary");
    }

    @Test
    void ignoresPackageSummaryIfAlreadySet() {

    }

    private InputStream spdxStream(String... lines) {
        final var string = String.join("\n", lines);
        return new ByteArrayInputStream(string.getBytes());
    }
}
