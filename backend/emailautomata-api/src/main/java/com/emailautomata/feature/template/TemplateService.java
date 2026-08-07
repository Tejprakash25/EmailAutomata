package com.emailautomata.feature.template;

import com.emailautomata.core.error.DuplicateResourceException;
import com.emailautomata.core.error.ResourceNotFoundException;
import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.feature.template.dto.CreateTemplateRequest;
import com.emailautomata.feature.template.dto.TemplateResponse;
import com.emailautomata.feature.template.dto.TemplateSummaryResponse;
import com.emailautomata.feature.template.dto.UpdateTemplateRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Template CRUD, scoped to the owning user on every operation.
 *
 * <p>The principal is passed in rather than pulled from a static context, so
 * the service is a plain unit under test and has no hidden dependency on the
 * web layer.</p>
 */
@Service
public class TemplateService {

    private final TemplateRepository repository;

    public TemplateService(TemplateRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<TemplateSummaryResponse> list(AuthenticatedUser principal, Pageable pageable) {
        return repository.findByUserId(principal.id(), pageable)
                .map(TemplateSummaryResponse::from);
    }

    @Transactional(readOnly = true)
    public TemplateResponse get(AuthenticatedUser principal, Long id) {
        return TemplateResponse.from(require(principal, id));
    }

    @Transactional
    public TemplateResponse create(AuthenticatedUser principal, CreateTemplateRequest request) {
        String name = request.name().trim();

        if (repository.existsByUserIdAndNameIgnoreCase(principal.id(), name)) {
            throw new DuplicateResourceException("Template", "name", name);
        }

        EmailTemplate template = EmailTemplate.create(
                principal.id(), name, request.subject(), request.body());

        try {
            return TemplateResponse.from(repository.saveAndFlush(template));
        } catch (DataIntegrityViolationException ex) {
            // Lost the race on the (user_id, name) unique constraint.
            throw new DuplicateResourceException("Template", "name", name);
        }
    }

    @Transactional
    public TemplateResponse update(AuthenticatedUser principal, Long id, UpdateTemplateRequest request) {
        EmailTemplate template = require(principal, id);
        String newName = request.name().trim();

        // A rename that collides with another of this user's templates is a conflict.
        repository.findByUserIdAndNameIgnoreCase(principal.id(), newName)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> {
                    throw new DuplicateResourceException("Template", "name", newName);
                });

        template.applyContent(newName, request.subject(), request.body());

        try {
            return TemplateResponse.from(repository.saveAndFlush(template));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Template", "name", newName);
        }
    }

    @Transactional
    public void delete(AuthenticatedUser principal, Long id) {
        repository.delete(require(principal, id));
    }

    /**
     * Loads a template the caller owns, or throws a 404. Every mutating and
     * reading path funnels through here, so ownership is checked exactly once
     * and can never be forgotten on a new endpoint.
     */
    private EmailTemplate require(AuthenticatedUser principal, Long id) {
        return repository.findByIdAndUserId(id, principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Template", id));
    }
}