package io.jmix.petclinic.entity.visit;

// tag::entity-saving-event-listener[]
import io.jmix.data.Sequence;
import io.jmix.data.Sequences;
import io.jmix.core.event.EntitySavingEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component("petclinic_VisitNumberGeneratorEntityListener")
public class VisitNumberGeneratorEntityListener {

    private final Sequences sequences;

    public VisitNumberGeneratorEntityListener(Sequences sequences) {
        this.sequences = sequences;
    }

    @EventListener
    public void calculateVisitNumber(final EntitySavingEvent<Visit> event) { // <1>

        if (!event.isNewEntity()) { // <2>
            return;
        }

        long sequenceNumber = sequences.createNextValue(Sequence.withName("visit_number")); // <3>
        int visitYear = event.getEntity().getVisitStart().getYear();

        String visitNumber = "V-%s-%06d".formatted(visitYear, sequenceNumber); // <4>

        event.getEntity().setVisitNumber(visitNumber);
    }
}

// end::entity-saving-event-listener[]