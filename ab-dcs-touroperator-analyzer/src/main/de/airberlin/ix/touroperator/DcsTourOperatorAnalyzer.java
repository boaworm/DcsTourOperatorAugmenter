package de.airberlin.ix.touroperator;


import com.sabre.ix.client.DcsFlightLeg;
import com.sabre.ix.client.analyzer.Analyzer;
import com.sabre.ix.client.analyzer.ContextAware;
import com.sabre.ix.client.analyzer.LifecycleAware;
import com.sabre.ix.client.context.Context;
import org.apache.log4j.Logger;

public class DcsTourOperatorAnalyzer implements Analyzer<DcsFlightLeg>, ContextAware, LifecycleAware {

    private static final Logger log = Logger.getLogger(DcsTourOperatorAnalyzer.class);

    private DcsTourOperatorAugmenter augmenter;
    private Context context;

    @Override
    public void analyze(DcsFlightLeg dcsFlightLeg) {
        try {
            augmenter.augment(dcsFlightLeg);
        } catch (Exception e) {
            // We do not want exceptions to escape the analyzer unless needed.
            log.error("Failed to augment DcsFlightLeg: " + dcsFlightLeg, e);
        }
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public void initialize() {
        augmenter = new DcsTourOperatorAugmenterImpl();
    }

    @Override
    public void destroy() {
        augmenter = null;
    }
}
