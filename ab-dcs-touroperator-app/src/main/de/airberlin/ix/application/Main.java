package de.airberlin.ix.application;

import com.sabre.ix.client.DcsFlightLeg;
import com.sabre.ix.client.DcsFlightLegServices;
import com.sabre.ix.client.context.Context;
import com.sabre.ix.client.context.ContextFactory;
import de.airberlin.ix.touroperator.DcsTourOperatorAnalyzer;

import java.util.List;

public class Main {

    // The purpose of this app/Main is to invoke the "client" code in such a way that would
    // also happen from the analyzer. This allows easier development and testing.

    public static void main(String[] args) {
        Context context = ContextFactory.createContext();

        DcsTourOperatorAnalyzer augmenter = new DcsTourOperatorAnalyzer();
        augmenter.setContext(context);

        DcsFlightLegServices dcsFlightLegServices = (DcsFlightLegServices) context.getDomainServices(DcsFlightLeg.class);
        List<DcsFlightLeg> dcsFlightLegs = dcsFlightLegServices.retrieveByCCL("DcsFlightLeg[OperatingCarrier=\"AB\" AND " +
                "OperatingFlightNumber=6436 AND " +
                "ScheduledDepartureDate=2014-07-01T00:00:00]");

        for(DcsFlightLeg dcsFlightLeg : dcsFlightLegs) {
            augmenter.analyze(dcsFlightLeg);
        }
    }

}
