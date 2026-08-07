package com.emailautomata.feature.dispatch;

import com.emailautomata.core.error.BusinessException;
import com.emailautomata.core.error.ErrorCode;
import com.emailautomata.core.error.ResourceNotFoundException;
import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.feature.dispatch.dto.ComposePreviewResponse;
import com.emailautomata.feature.dispatch.dto.ComposePreviewResponse.UnresolvedRecipient;
import com.emailautomata.feature.dispatch.dto.ComposeRequest;
import com.emailautomata.feature.dispatch.dto.DispatchResponse;
import com.emailautomata.feature.recipient.Recipient;
import com.emailautomata.feature.recipient.RecipientRepository;
import com.emailautomata.feature.template.EmailTemplate;
import com.emailautomata.feature.template.TemplateRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Composes a template and a set of recipients into a persisted draft dispatch,
 * rendering each recipient's content and recording who could not be resolved.
 *
 * <p>Reaches across into the template and recipient slices' repositories. That
 * cross-slice read is deliberate and one-directional: dispatch depends on them,
 * never the reverse, which keeps the dependency acyclic.</p>
 */
@Service
public class ComposeService {

    private final DispatchRepository dispatches;
    private final DispatchRecipientRepository dispatchRecipients;
    private final TemplateRepository templates;
    private final RecipientRepository recipients;

    public ComposeService(DispatchRepository dispatches,
                          DispatchRecipientRepository dispatchRecipients,
                          TemplateRepository templates,
                          RecipientRepository recipients) {
        this.dispatches = dispatches;
        this.dispatchRecipients = dispatchRecipients;
        this.templates = templates;
        this.recipients = recipients;
    }

    /**
     * Creates a draft dispatch with one rendered row per recipient.
     *
     * <p>Rendering happens for every recipient, including those with missing
     * fields — their row is still created (as PENDING) so the draft reflects
     * the full intended audience, but the returned preview names them so the
     * caller can decide whether to proceed. Sending (Commit 9) is what will
     * refuse to transmit an unresolved row.</p>
     */
    @Transactional
    public ComposeResult compose(AuthenticatedUser principal, ComposeRequest request) {
        String subject;
        String body;
        Long templateId = null;

        if (request.templateId() != null) {
            EmailTemplate template = templates.findByIdAndUserId(request.templateId(), principal.id())
                    .orElseThrow(() -> new ResourceNotFoundException("Template", request.templateId()));
            subject = template.getSubject();
            body = template.getBody();
            templateId = template.getId();
        } else {
            subject = request.subject();
            body = request.body();
        }

        List<Recipient> targets = resolveTargets(principal, request);
        if (targets.isEmpty()) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED,
                    "No recipients matched the selection.");
        }

        Dispatch dispatch = dispatches.save(
                Dispatch.draft(principal.id(), templateId, subject, body, targets.size()));

        List<UnresolvedRecipient> unresolved = new ArrayList<>();
        List<DispatchRecipient> rows = new ArrayList<>(targets.size());

        for (Recipient recipient : targets) {
            Map<String, String> fields = recipient.getFields();

            var renderedSubject = TemplateRenderer.render(subject, fields);
            var renderedBody = TemplateRenderer.render(body, fields);

            var missing = new ArrayList<>(renderedSubject.missing());
            renderedBody.missing().forEach(m -> {
                if (!missing.contains(m)) missing.add(m);
            });

            if (!missing.isEmpty()) {
                unresolved.add(new UnresolvedRecipient(recipient.getEmail(), missing));
            }

            rows.add(DispatchRecipient.rendered(
                    dispatch.getId(),
                    recipient.getId(),
                    recipient.getEmail(),
                    recipient.getDisplayName(),
                    renderedSubject.text(),
                    renderedBody.text()));
        }

        dispatchRecipients.saveAll(rows);

        ComposePreviewResponse preview = new ComposePreviewResponse(
                targets.size(), targets.size() - unresolved.size(), unresolved);

        return new ComposeResult(DispatchResponse.of(dispatch, rows), preview);
    }

    @Transactional(readOnly = true)
    public Page<DispatchResponse> list(AuthenticatedUser principal, Pageable pageable) {
        return dispatches.findByUserId(principal.id(), pageable).map(DispatchResponse::summary);
    }

    @Transactional(readOnly = true)
    public DispatchResponse get(AuthenticatedUser principal, Long id) {
        Dispatch dispatch = dispatches.findByIdAndUserId(id, principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Dispatch", id));
        return DispatchResponse.of(dispatch, dispatchRecipients.findByDispatchIdOrderByIdAsc(id));
    }

    /**
     * Resolves the explicit recipient ids and/or list into a de-duplicated set
     * of this user's recipients. Ids that don't belong to the user are silently
     * ignored rather than erroring — the caller asked for a set, and we return
     * the valid members of it.
     */
    private List<Recipient> resolveTargets(AuthenticatedUser principal, ComposeRequest request) {
        Map<Long, Recipient> byId = new LinkedHashMap<>();

        if (request.recipientIds() != null) {
            for (Long id : request.recipientIds()) {
                recipients.findByIdAndUserId(id, principal.id())
                        .ifPresent(r -> byId.put(r.getId(), r));
            }
        }

        if (request.listId() != null) {
            // Page through the user's recipients and keep those in the list.
            // A dedicated query arrives if this ever needs to scale further.
            recipients.findByUserId(principal.id(), Pageable.ofSize(1000))
                    .filter(r -> request.listId().equals(r.getListId()))
                    .forEach(r -> byId.put(r.getId(), r));
        }

        return new ArrayList<>(byId.values());
    }

    /** Pairs the created draft with its readiness preview. */
    public record ComposeResult(DispatchResponse dispatch, ComposePreviewResponse preview) {
    }
}