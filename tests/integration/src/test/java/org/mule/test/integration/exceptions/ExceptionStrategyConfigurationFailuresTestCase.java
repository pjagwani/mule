/*
 * $Id$
 * --------------------------------------------------------------------------------------
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 *
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.test.integration.exceptions;

import org.junit.Test;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.config.ConfigurationBuilder;
import org.mule.api.config.ConfigurationException;
import org.mule.api.context.MuleContextBuilder;
import org.mule.api.context.MuleContextFactory;
import org.mule.api.context.notification.MuleContextNotificationListener;
import org.mule.config.spring.SpringXmlConfigurationBuilder;
import org.mule.context.DefaultMuleContextBuilder;
import org.mule.context.DefaultMuleContextFactory;
import org.mule.context.notification.MuleContextNotification;
import org.mule.tck.junit4.AbstractMuleTestCase;
import org.mule.util.concurrent.Latch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class ExceptionStrategyConfigurationFailuresTestCase extends AbstractMuleTestCase
{

    @Test(expected = ConfigurationException.class)
    public void testNamedFlowExceptionStrategyFails() throws Exception
    {
        loadConfiguration("org/mule/test/integration/exceptions/named-flow-exception-strategy.xml");
    }

    @Test(expected = ConfigurationException.class)
    public void testNamedServiceExceptionStrategyFails() throws Exception
    {
        loadConfiguration("org/mule/test/integration/exceptions/named-service-exception-strategy.xml");
    }

    @Test(expected = ConfigurationException.class)
    public void testReferenceExceptionStrategyAsGlobalExceptionStrategy() throws Exception
    {
        loadConfiguration("org/mule/test/integration/exceptions/reference-global-exception-strategy.xml");
    }

    @Test(expected = ConfigurationException.class)
    public void testDefaultEsFailsAsReferencedExceptionStrategy() throws Exception
    {
        loadConfiguration("org/mule/test/integration/exceptions/default-es-as-referenced-exception-strategy.xml");
    }

    @Test(expected = ConfigurationException.class)
    public void testDefaultExceptionStrategyReferencesNonExistentExceptionStrategy() throws Exception
    {
        loadConfiguration("org/mule/test/integration/exceptions/default-exception-strategy-reference-non-existent-es.xml");
    }

    private void loadConfiguration(String configuration) throws MuleException, InterruptedException
    {
        MuleContextFactory muleContextFactory = new DefaultMuleContextFactory();
        List<ConfigurationBuilder> builders = new ArrayList<ConfigurationBuilder>();
        builders.add(new SpringXmlConfigurationBuilder(configuration));
        MuleContextBuilder contextBuilder = new DefaultMuleContextBuilder();
        MuleContext muleContext = muleContextFactory.createMuleContext(builders, contextBuilder);
        final AtomicReference<Latch> contextStartedLatch = new AtomicReference<Latch>();
        contextStartedLatch.set(new Latch());
        muleContext.registerListener(new MuleContextNotificationListener<MuleContextNotification>()
        {
            @Override
            public void onNotification(MuleContextNotification notification)
            {
                if (notification.getAction() == MuleContextNotification.CONTEXT_STARTED)
                {
                    contextStartedLatch.get().countDown();
                }
            }
        });
        muleContext.start();
        contextStartedLatch.get().await(20, TimeUnit.SECONDS);
    }

}