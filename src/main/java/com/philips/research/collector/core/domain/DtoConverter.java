package com.philips.research.collector.core.domain;

import com.philips.research.collector.core.ProjectService;

import java.util.List;
import java.util.stream.Collectors;

abstract class DtoConverter {
    static ProjectService.ProjectDto toDto(Project project) {
        final var dto = new ProjectService.ProjectDto();
        dto.id = project.getId();
        dto.title = project.getTitle();
        dto.packages = toDtoList(project.getRootPackages());
        return dto;
    }

    static ProjectService.PackageDto toDto(Package.Link link) {
        final var dto = toDto(link.getPackage());
        dto.relation = link.getRelation().name().toLowerCase();
        return dto;
    }

    static ProjectService.PackageDto toDto(Package pkg) {
        final var dto = new ProjectService.PackageDto();
        dto.license = pkg.getLicense();
        dto.title = pkg.getTitle();
        dto.reference = pkg.getReference();
        dto.children = toDtoChildList(pkg.getChildren());
        return dto;
    }

    static List<ProjectService.PackageDto> toDtoList(List<Package> packages) {
        return packages.stream().map(DtoConverter::toDto).collect(Collectors.toList());
    }

    static List<ProjectService.PackageDto> toDtoChildList(List<Package.Link> links) {
        return links.stream().map(DtoConverter::toDto).collect(Collectors.toList());
    }
}
