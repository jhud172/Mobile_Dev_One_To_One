package uk.ac.cardiff.trainerhub.domain

object BusinessRules {
    fun validateClientDraft(draft: NewClientDraft): List<String> {
        val errors = mutableListOf<String>()
        if (draft.fullName.isBlank()) errors += "Full name is required."
        if (draft.email.isBlank() || !draft.email.contains("@")) errors += "A valid email is required."
        if (draft.phone.isBlank()) errors += "Phone number is required."
        if (draft.goal.isBlank()) errors += "A coaching goal is required."
        return errors
    }

    fun validatePlanDraft(draft: PlanDraft): List<String> {
        val errors = mutableListOf<String>()
        if (draft.title.isBlank()) errors += "Plan title is required."
        if (draft.focus.isBlank()) errors += "Plan focus is required."
        if (draft.weeks.any { it.summary.isBlank() }) errors += "Each week needs a summary."
        if (draft.weeks.none { it.exercises.isNotEmpty() }) errors += "At least one exercise is required."
        return errors
    }

    fun validateSessionDraft(draft: SessionDraft): List<String> {
        val errors = mutableListOf<String>()
        if (draft.title.isBlank()) errors += "Session title is required."
        if (draft.location.isBlank()) errors += "Location is required."
        if (draft.durationMinutes <= 0) errors += "Duration must be greater than zero."
        return errors
    }

    fun validateInvoiceDraft(draft: InvoiceDraft): List<String> {
        val errors = mutableListOf<String>()
        if (draft.description.isBlank()) errors += "Invoice description is required."
        if (draft.amountPence <= 0) errors += "Amount must be greater than zero."
        return errors
    }
}

