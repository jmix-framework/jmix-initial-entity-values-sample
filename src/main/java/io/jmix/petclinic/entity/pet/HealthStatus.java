package io.jmix.petclinic.entity.pet;

import io.jmix.core.metamodel.datatype.EnumClass;

import org.springframework.lang.Nullable;


public enum HealthStatus implements EnumClass<String> {

    HEALTHY("HEALTHY"),
    SICK("SICK"),
    IN_RECOVERY("IN_RECOVERY"),
    UNKNOWN("UNKNOWN");

    private final String id;

    HealthStatus(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Nullable
    public static HealthStatus fromId(String id) {
        for (HealthStatus at : HealthStatus.values()) {
            if (at.getId().equals(id)) {
                return at;
            }
        }
        return null;
    }
}