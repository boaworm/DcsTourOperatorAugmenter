package de.airberlin.ix.touroperator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class DcsTourOperatorAugmenterImplTest {

    DcsTourOperatorAugmenter augmenter;

    @Before
    public void setUp() {
        augmenter = new DcsTourOperatorAugmenterImpl();
    }

    @After
    public void tearDown() {
        augmenter = null;
    }

    @Test
    public void verifySomething() {

    }
}
