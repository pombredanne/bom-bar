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

import com.philips.research.collector.core.ProjectService;

import java.util.stream.Collectors;

abstract class DtoConverter {
    static ProjectService.ProjectDto toDto(Project project) {
        final var dto = toBaseDto(project);
        dto.packages = project.getRootDependencies().stream()
                .map(DtoConverter::toBaseDto)
                .collect(Collectors.toList());
        return dto;
    }

    static ProjectService.ProjectDto toBaseDto(Project project) {
        final var dto = new ProjectService.ProjectDto();
        dto.id = project.getId();
        dto.title = project.getTitle();
        return dto;
    }

    static ProjectService.PackageDto toDto(Dependency dependency) {
        // Add once there are more elaborate details
        final var dto = toBaseDto(dependency);
        dto.children = dependency.getRelations().stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
        return dto;
    }

    static ProjectService.PackageDto toDto(Relation relation) {
        final var dto = toBaseDto(relation);
        dto.children = relation.getTarget().getRelations().stream()
                .map(DtoConverter::toDto)
                .collect(Collectors.toList());
        return dto;
    }

    static ProjectService.PackageDto toBaseDto(Relation relation) {
        final var dto = toBaseDto(relation.getTarget());
        dto.relation = relation.getType().name().toLowerCase();
        return dto;
    }

    private static ProjectService.PackageDto toBaseDto(Dependency dependency) {
        final var dto = new ProjectService.PackageDto();
        dependency.getPackage().ifPresent(pkg -> dto.reference = pkg.getReference() + "@" + dependency.getVersion());
        dto.title = dependency.getTitle();
        dto.version = dependency.getVersion();
        dto.license = dependency.getLicense();
        return dto;
    }
}
