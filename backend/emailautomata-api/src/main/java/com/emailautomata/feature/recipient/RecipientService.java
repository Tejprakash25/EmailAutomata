package com.emailautomata.feature.recipient;

import com.emailautomata.core.error.DuplicateResourceException;
import com.emailautomata.core.error.ResourceNotFoundException;
import com.emailautomata.core.security.AuthenticatedUser;
import com.emailautomata.feature.recipient.dto.CreateListRequest;
import com.emailautomata.feature.recipient.dto.CreateRecipientRequest;
import com.emailautomata.feature.recipient.dto.CsvImportRequest;
import com.emailautomata.feature.recipient.dto.CsvImportResult;
import com.emailautomata.feature.recipient.dto.CsvImportResult.RowError;
import com.emailautomata.feature.recipient.dto.RecipientListResponse;
import com.emailautomata.feature.recipient.dto.RecipientResponse;
import com.emailautomata.feature.recipient.dto.UpdateRecipientRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Recipient and list management, scoped to the owner on every path.
 */
@Service
public class RecipientService {

    private final RecipientRepository recipients;
    private final RecipientListRepository lists;

    public RecipientService(RecipientRepository recipients, RecipientListRepository lists) {
        this.recipients = recipients;
        this.lists = lists;
    }

    // ------------------------------------------------------------- recipients

    @Transactional(readOnly = true)
    public Page<RecipientResponse> list(AuthenticatedUser principal, Pageable pageable) {
        return recipients.findByUserId(principal.id(), pageable).map(RecipientResponse::from);
    }

    @Transactional
    public RecipientResponse create(AuthenticatedUser principal, CreateRecipientRequest request) {
        String email = request.email().toLowerCase().trim();

        if (recipients.existsByUserIdAndEmailIgnoreCase(principal.id(), email)) {
            throw new DuplicateResourceException("Recipient", "email", email);
        }
        validateListOwnership(principal, request.listId());

        Recipient recipient = Recipient.create(
                principal.id(), request.listId(), email, request.displayName(), request.fieldsOrEmpty());

        try {
            return RecipientResponse.from(recipients.saveAndFlush(recipient));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Recipient", "email", email);
        }
    }

    @Transactional
    public RecipientResponse update(AuthenticatedUser principal, Long id, UpdateRecipientRequest request) {
        Recipient recipient = requireRecipient(principal, id);
        validateListOwnership(principal, request.listId());
        recipient.update(request.listId(), request.displayName(), request.fieldsOrEmpty());
        return RecipientResponse.from(recipients.saveAndFlush(recipient));
    }

    @Transactional
    public void delete(AuthenticatedUser principal, Long id) {
        recipients.delete(requireRecipient(principal, id));
    }

    // ------------------------------------------------------------------ lists

    @Transactional(readOnly = true)
    public List<RecipientListResponse> allLists(AuthenticatedUser principal) {
        return lists.findByUserIdOrderByNameAsc(principal.id()).stream()
                .map(list -> RecipientListResponse.from(list, 0))
                .toList();
    }

    @Transactional
    public RecipientListResponse createList(AuthenticatedUser principal, CreateListRequest request) {
        String name = request.name().trim();

        if (lists.existsByUserIdAndNameIgnoreCase(principal.id(), name)) {
            throw new DuplicateResourceException("List", "name", name);
        }

        try {
            RecipientList saved = lists.saveAndFlush(RecipientList.create(principal.id(), name));
            return RecipientListResponse.from(saved, 0);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("List", "name", name);
        }
    }

    @Transactional
    public void deleteList(AuthenticatedUser principal, Long id) {
        // Recipients are detached by the ON DELETE SET NULL constraint, not removed.
        lists.delete(requireList(principal, id));
    }

    // ----------------------------------------------------------------- import

    /**
     * Imports a CSV batch. Rows that parse and are new are saved; malformed and
     * duplicate rows are reported with their line number. The whole thing runs
     * in one transaction, but individual row rejections do not roll it back —
     * they were never added.
     */
    @Transactional
    public CsvImportResult importCsv(AuthenticatedUser principal, CsvImportRequest request) {
        validateListOwnership(principal, request.listId());

        var outcome = RecipientCsvParser.parse(request.csv());
        List<RowError> errors = new ArrayList<>(outcome.errors());

        var parsedRows = outcome.rows();
        if (parsedRows.isEmpty()) {
            return new CsvImportResult(0, errors.size(), errors);
        }

        // One query establishes which addresses already exist for this user.
        List<String> candidateEmails = parsedRows.stream().map(RecipientCsvParser.ParsedRow::email).toList();
        Set<String> existing = new HashSet<>(recipients.findExistingEmails(principal.id(), candidateEmails));

        // Guard against the same address appearing twice within one CSV.
        Set<String> seenInBatch = new HashSet<>();
        List<Recipient> toSave = new ArrayList<>();

        for (var row : parsedRows) {
            if (existing.contains(row.email()) || !seenInBatch.add(row.email())) {
                errors.add(new RowError(row.line(), row.email(), "Already in your recipients."));
                continue;
            }
            toSave.add(Recipient.create(
                    principal.id(), request.listId(), row.email(), row.displayName(), row.fields()));
        }

        recipients.saveAll(toSave);
        errors.sort((a, b) -> Integer.compare(a.line(), b.line()));

        return new CsvImportResult(toSave.size(), errors.size(), errors);
    }

    // ---------------------------------------------------------------- helpers

    private Recipient requireRecipient(AuthenticatedUser principal, Long id) {
        return recipients.findByIdAndUserId(id, principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient", id));
    }

    private RecipientList requireList(AuthenticatedUser principal, Long id) {
        return lists.findByIdAndUserId(id, principal.id())
                .orElseThrow(() -> new ResourceNotFoundException("List", id));
    }

    /** A recipient may only be filed under a list the same user owns. */
    private void validateListOwnership(AuthenticatedUser principal, Long listId) {
        if (listId != null) {
            requireList(principal, listId);
        }
    }
}