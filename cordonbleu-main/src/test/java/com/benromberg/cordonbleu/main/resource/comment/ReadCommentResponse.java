package com.benromberg.cordonbleu.main.resource.comment;

import com.benromberg.cordonbleu.main.resource.team.ReadUserResponse;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ReadCommentResponse {
    @JsonProperty
    private String id;

    @JsonProperty
    private ReadUserResponse user;

    @JsonProperty
    private LocalDateTime created;

    @JsonProperty
    private String text;

    @JsonProperty
    private String textAsHtml;

    @JsonCreator
    public ReadCommentResponse(String id, ReadUserResponse user, LocalDateTime created, String text, String textAsHtml) {
        this.id = id;
        this.user = user;
        this.created = created;
        this.text = text;
        this.textAsHtml = textAsHtml;
    }

    public String getId() {
        return id;
    }

    public ReadUserResponse getUser() {
        return user;
    }

    public LocalDateTime getCreated() {
        return created;
    }

    public String getText() {
        return text;
    }

    public String getTextAsHtml() {
        return textAsHtml;
    }
}
