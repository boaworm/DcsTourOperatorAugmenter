package de.airberlin.ix.touroperator;


import com.sabre.ix.client.*;
import com.sabre.ix.client.analyzer.Analyzer;
import com.sabre.ix.client.analyzer.ContextAware;
import com.sabre.ix.client.context.Context;
import com.sabre.ix.client.dao.DataRow;
import com.sabre.ix.client.services.QueryServices;
import org.apache.log4j.Logger;

import java.util.Collections;
import java.util.List;

public class DcsTourOperatorAnalyzer implements Analyzer<DcsFlightLeg>, ContextAware {

    private static final Logger log = Logger.getLogger(DcsTourOperatorAnalyzer.class);
    private Context context;
    private Implementation implementation = Implementation.OPTIMIZED;

    public enum Implementation {
        BASIC,
        OPTIMIZED
    }

    public DcsTourOperatorAnalyzer() {}
    public DcsTourOperatorAnalyzer(Implementation implementation) {
        this.implementation = implementation;
    }

    @Override
    public void analyze(DcsFlightLeg dcsFlightLeg) {
        try {
            switch (implementation) {
                case BASIC:
                    linkTraversalImplementation(dcsFlightLeg);
                    break;
                case OPTIMIZED:
                    queryServicesImplementation(dcsFlightLeg);
                    break;
                default:
                    throw new RuntimeException("Unsupported implementation chosen");
            }
        } catch (Exception e) {
            // We do not want exceptions to escape the analyzer unless needed.
            log.error("Failed to augment DcsFlightLeg: " + dcsFlightLeg, e);
        }
    }

    /**
     * Performance-optimized solution, leveraging query services to avoid loading the bookings or the links
     */
    private void queryServicesImplementation(DcsFlightLeg dcsFlightLeg) {

        QueryServices queryServices = context.getQueryServices();
        String mmSql = "SELECT b.rloc AS RLOC, b.tourOperator, b.tourOperatorBookingNumber, dbl.bookingNameItemId, dbl.dcsPaxId, dbl.dcsPaxSegmentId " +
                "FROM Booking b, DcsBookingLink dbl " +
                "WHERE dbl.bookingId = b.bookingId " +
                "AND dbl.dcsFlightLegId = " + dcsFlightLeg.getDcsFlightLegId();

        List<DataRow> dataRows = queryServices.retrieveData(mmSql, Collections.emptyList());

        // For each passenger and segment, we want to see if there is a matching row in the query services response
        for (DcsPax dcsPax : dcsFlightLeg.getDcsPaxs()) {
            log.trace("Processing passenger: " + dcsPax);
            for (DcsPaxSegment dcsPaxSegment : dcsPax.getDcsPaxSegments()) {
                log.trace("Processing segment: " + dcsPaxSegment);

                DataRow matchingRow = getRowMatchingSegment(dcsPaxSegment, dataRows);
                if (matchingRow != null) {
                    String tourOperator = matchingRow.getString("TOUROPERATOR");
                    String tourOperatorBookingNumber = matchingRow.getString("TOUROPERATORBOOKINGNUMBER");

                    if (tourOperator != null && !tourOperator.isEmpty()) {
                        dcsPax.setDynamicAttribute("TourOperator", tourOperator);
                    }
                    if (tourOperatorBookingNumber != null && !tourOperatorBookingNumber.isEmpty()) {
                        dcsPax.setDynamicAttribute("TourOperatorBookingNumber", tourOperatorBookingNumber);
                    }

                } else {
                    log.debug("We seem to not have a matching booking link for DcsPaxSegment: " + dcsPaxSegment);
                }
            }
        }
    }

    /**
     * Return a data row matching the dcsPaxSegment, or NULL if none can be found
     *
     * @param dcsPaxSegment Segment to search for
     * @param dataRows      Rows returned by query services
     * @return DataRow , or NULL
     */
    private DataRow getRowMatchingSegment(DcsPaxSegment dcsPaxSegment, List<DataRow> dataRows) {
        for (DataRow row : dataRows) {
            Long dcsPaxSegmentId = row.getLong("DCSPAXSEGMENTID");
            if (dcsPaxSegment.getDcsPaxSegmentId() == dcsPaxSegmentId) {
                return row;
            }
        }
        return null;
    }

    /**
     * Basic implementation. Traverses the link for all dcs pax segments
     */
    private void linkTraversalImplementation(DcsFlightLeg dcsFlightLeg) {
        // For each passenger and segment, we want to see if there is a link to a bookingNameItem. If so, go grab some data
        for (DcsPax dcsPax : dcsFlightLeg.getDcsPaxs()) {
            log.trace("Processing passenger: " + dcsPax);
            for (DcsPaxSegment dcsPaxSegment : dcsPax.getDcsPaxSegments()) {
                log.trace("Processing segment: " + dcsPaxSegment);
                List<DcsBookingLink> linkedDataObjects = dcsPaxSegment.getLinkedDataObjects(DcsBookingLink.class);
                log.trace("Found " + linkedDataObjects.size() + " links");
                for (DcsBookingLink dcsBookingLink : linkedDataObjects) {
                    log.trace("Processing DcsBookingLink: " + dcsBookingLink);

                    BookingNameItem bookingNameItem = dcsBookingLink.getBookingNameItem();
                    if (bookingNameItem == null) {
                        log.error("Link object points to NULL bookingNameItem: " + dcsBookingLink);
                        break;
                    }

                    // All well, lets figure out what's next.

                    // Primary goal: Look for TourOperator code:
                    Booking booking = bookingNameItem.getBookingName().getBooking();
                    Object tourOperator = booking.getDynamicAttribute("TourOperator");
                    if (tourOperator != null) {
                        String s = (String) tourOperator;
                        log.debug("Discovered a TourOperator code [" + s + "] for segment: " + dcsFlightLeg);
                        dcsPax.setDynamicAttribute("TourOperator", s);
                    } else {
                        log.trace("No TourOperator found on booking, moving on");
                    }

                    Object tourOperatorBookingNumber = booking.getDynamicAttribute("TourOperatorBookingNumber");
                    if (tourOperatorBookingNumber != null) {
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

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

}
