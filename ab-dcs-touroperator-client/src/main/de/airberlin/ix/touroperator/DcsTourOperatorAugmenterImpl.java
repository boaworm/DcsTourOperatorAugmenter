package de.airberlin.ix.touroperator;

import com.sabre.ix.client.DcsFlightLeg;

public class DcsTourOperatorAugmenterImpl implements DcsTourOperatorAugmenter {

    @Override
    public void augment(DcsFlightLeg dcsFlightLeg) {
        throw new RuntimeException("OOps!");
    }
}
