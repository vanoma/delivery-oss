package com.vanoma.api.order.packages;

import com.vanoma.api.utils.exceptions.InvalidParameterException;

import java.io.Serializable;

public class CancelPackageJson implements Serializable {

    public CancelPackageJson() {
    }
    
    private String note;

    public String getNote() {
        return note;
    }

    // For testing
    public CancelPackageJson setNote(String note) {
        this.note = note;
        return this;
    }

    public void validate() {
        if (note == null) {
            throw new InvalidParameterException("crud.package.cancellation.note.required");
        }
    }
}
