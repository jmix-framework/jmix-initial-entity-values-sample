package io.jmix.petclinic.entity.pet;

import io.jmix.core.metamodel.annotation.JmixEntity;
import io.jmix.petclinic.entity.NamedEntity;
import io.jmix.petclinic.entity.owner.Owner;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

@JmixEntity
@Table(name = "PETCLINIC_PET")
@Entity(name = "petclinic_Pet")
public class Pet extends NamedEntity {

    @Column(name = "IDENTIFICATION_NUMBER", nullable = false)
    @NotNull
    private String identificationNumber;

    @Column(name = "HEALTH_STATUS")
    private String healthStatus;
    @Column(name = "BIRTHDATE")
    private LocalDate birthdate;

    @JoinColumn(name = "TYPE_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private PetType type;

    @JoinColumn(name = "OWNER_ID")
    @ManyToOne(fetch = FetchType.LAZY)
    private Owner owner;

    public HealthStatus getHealthStatus() {
        return healthStatus == null ? null : HealthStatus.fromId(healthStatus);
    }

    public void setHealthStatus(HealthStatus healthStatus) {
        this.healthStatus = healthStatus == null ? null : healthStatus.getId();
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public void setBirthdate(LocalDate birthdate) {
        this.birthdate = birthdate;
    }

    public PetType getType() {
        return type;
    }

    public void setType(PetType type) {
        this.type = type;
    }

    public Owner getOwner() {
        return owner;
    }

    public void setOwner(Owner owner) {
        this.owner = owner;
    }

    public String getIdentificationNumber() {
        return identificationNumber;
    }

    public void setIdentificationNumber(String identificationNumber) {
        this.identificationNumber = identificationNumber;
    }
}