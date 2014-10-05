package de.airberlin.ix.touroperator;

import com.sabre.ix.client.*;
import org.apache.log4j.Logger;

import java.util.List;

public class DcsTourOperatorAugmenterImpl implements DcsTourOperatorAugmenter {

    private static final Logger log = Logger.getLogger(DcsTourOperatorAugmenterImpl.class);

    @Override
    public void augment(DcsFlightLeg dcsFlightLeg) {

        // For each passenger and segment, we want to see if there is a link to a bookingNameItem. If so, go grab some data
        for(DcsPax dcsPax : dcsFlightLeg.getDcsPaxs()) {
            log.trace("Processing passenger: " + dcsPax);
            for(DcsPaxSegment dcsPaxSegment : dcsPax.getDcsPaxSegments()) {
                log.trace("Processing segment: " + dcsPaxSegment);
                List<DcsBookingLink> linkedDataObjects = dcsPaxSegment.getLinkedDataObjects(DcsBookingLink.class);
                log.trace("Found " + linkedDataObjects.size() + " links");
                for(DcsBookingLink dcsBookingLink : linkedDataObjects) {
                    log.trace("Processing DcsBookingLink: " + dcsBookingLink);

                    BookingNameItem bookingNameItem = dcsBookingLink.getBookingNameItem();
                    if(bookingNameItem == null) {
                        log.error("Link object points to NULL bookingNameItem: " + dcsBookingLink);
                        break;
                    }

                    // All well, lets figure out what's next.

                    // Primary goal: Look for TourOperator code:
                    Booking booking = bookingNameItem.getBookingName().getBooking();
                    Object tourOperator = booking.getDynamicAttribute("TourOperator");
                    if(tourOperator != null) {
                        String s = (String) tourOperator;
                        log.debug("Discovered a TourOperator code [" + s + "] for segment: " + dcsFlightLeg);
                        dcsPax.setDynamicAttribute("TourOperator", s);
                    } else {
                        log.trace("No TourOperator found on booking, moving on");
                    }

                    Object tourOperatorBookingNumber = booking.getDynamicAttribute("TourOperatorBookingNumber");
                    if(tourOperatorBookingNumber != null) {
                        String s = (String) tourOperatorBookingNumber;
                        log.debug("Discovered a TourOperatorBookingNumber code [" + s + "] for segment: " + dcsFlightLeg);
                        dcsPax.setDynamicAttribute("TourOperatorBookingNumber", s);
                    } else {
                        log.trace("No TourOperatorBookingNumber found on booking, moving on");
                    }

                }


            }
        }

    }
}
