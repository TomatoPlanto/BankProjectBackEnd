package com.banking.models.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

// minimal info for an iban lookup - no balance so a customer cant snoop others
@Getter
@Setter
public class IbanLookupResponseDTO {
    private UUID accountId;
    private String iban;
    private String ownerName;

    public IbanLookupResponseDTO(UUID accountId, String iban, String ownerName) {
        this.accountId = accountId;
        this.iban = iban;
        this.ownerName = ownerName;
    }
}