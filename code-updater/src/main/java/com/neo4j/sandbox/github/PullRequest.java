package com.neo4j.sandbox.github;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.Objects;

public class PullRequest {

    private final String title;
    private final String description;
    private final boolean draft;
    private final boolean maintainersCanModify;
    private final String base;
    private final String branch;

    public PullRequest(String title,
                       String description,
                       boolean draft,
                       boolean maintainersCanModify,
                       String base,
                       String branch) {

        this.title = title;
        this.description = description;
        this.draft = draft;
        this.maintainersCanModify = maintainersCanModify;
        this.base = base;
        this.branch = branch;
    }

    @JsonGetter("title")
    public String getTitle() {
        return title;
    }

    @JsonGetter("body")
    public String getDescription() {
        return description;
    }

    @JsonGetter("draft")
    public boolean isDraft() {
        return draft;
    }

    @JsonGetter("maintainer_can_modify")
    public boolean maintainersCanModify() {
        return maintainersCanModify;
    }

    @JsonGetter("base")
    public String getBase() {
        return base;
    }

    @JsonGetter("head")
    public String getBranch() {
        return branch;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PullRequest that = (PullRequest) o;
        return draft == that.draft &&
                maintainersCanModify == that.maintainersCanModify &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(base, that.base) &&
                Objects.equals(branch, that.branch);
    }

    @Override
    public int hashCode() {
        return Objects.hash(title, description, draft, maintainersCanModify, base, branch);
    }
}
