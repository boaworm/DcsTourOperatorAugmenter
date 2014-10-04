package de.airberlin.ix.touroperator;

import com.sabre.ix.client.DcsFlightLeg;

public interface DcsTourOperatorAugmenter {

    /**
     * Gather information from linked Booking objects into the DcsFlightLeg
     * */
    void augment(DcsFlightLeg dcsFlightLeg);

}
