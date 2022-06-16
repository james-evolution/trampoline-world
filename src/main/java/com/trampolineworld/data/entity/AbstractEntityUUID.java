package com.trampolineworld.data.entity;

import java.util.UUID;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import org.hibernate.annotations.Type;

@MappedSuperclass
public abstract class AbstractEntityUUID {

    @Id
    @GeneratedValue
    @Type(type = "uuid-char")
    private UUID id;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        }
        return super.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof AbstractEntityUUID)) {
            return false; // null or other class
        }
        AbstractEntityUUID other = (AbstractEntityUUID) obj;

        if (id != null) {
            return id.equals(other.id);
        }
        return super.equals(other);
    }
}