package com.philips.research.collector.controller;

import com.philips.research.collector.core.ProjectService;
import pl.tlinkowski.annotation.basic.NullOr;

import java.util.List;
import java.util.UUID;

@SuppressWarnings("NotNullFieldNotInitialized")
class ProjectJson {
    public UUID id;
    public String title;
    public @NullOr List<PackageJson> packages;

    @SuppressWarnings("unused")
    ProjectJson() {
    }

    ProjectJson(ProjectService.ProjectDto dto) {
        this.id = dto.id;
        this.title = dto.title;
        this.packages = PackageJson.toList(dto.packages);
    }
}
