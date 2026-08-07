package com.emailautomata.feature.dispatch.dto;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * A compose request either draws subject/body from a template or supplies them
 * inline, and targets an explicit set of recipient ids and/or a whole list.
 */
public record ComposeRequest(

        Long templateId,

        @Size(max = 255, message = "Subject must be 255 characters or fewer")
        String subject,

        String body,

        List<Long> recipientIds,

        Long listId
) {

    /** At least one addressing method must be supplied. */
    @AssertTrue(message = "Select at least one recipient or a list")
    public boolean hasTargets() {
        return (recipientIds != null && !recipientIds.isEmpty()) || listId != null;
    }

    /** Content must come from either a template or an inline subject+body. */
    @AssertTrue(message = "Provide a template, or a subject and body")
    public boolean hasContent() {
        boolean inline = subject != null && !subject.isBlank() && body != null && !body.isBlank();
        return templateId != null || inline;
    }
}