package org.apache.maven.plugin.surefire;

/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import org.apache.maven.plugin.surefire.log.api.ConsoleLogger;
import org.apache.maven.plugin.surefire.log.api.ConsoleLoggerDecorator;
import org.apache.maven.plugin.surefire.log.api.PrintStreamLogger;
import org.apache.maven.surefire.booter.IsolatedClassLoader;
import org.apache.maven.surefire.booter.SurefireReflector;
import org.junit.Before;
import org.junit.Test;

import static org.apache.maven.surefire.util.ReflectionUtils.getMethod;
import static org.apache.maven.surefire.util.ReflectionUtils.invokeMethodWithArray;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * @author <a href="mailto:tibordigana@apache.org">Tibor Digana (tibor17)</a>
 * @see ConsoleLogger
 * @see SurefireReflector
 * @since 2.20
 */
public class SurefireReflectorTest
{
    private ConsoleLogger logger;
    private ClassLoader cl;

    @Before
    public void prepareData()
    {
        logger = spy( new PrintStreamLogger( System.out ) );
        cl = new IsolatedClassLoader( Thread.currentThread().getContextClassLoader(), false, "role" );
    }

    @Test
    public void shouldProxyConsoleLogger()
    {
        Object mirror = SurefireReflector.createConsoleLogger( logger, cl );
        assertThat( mirror, is( notNullValue() ) );
        assertThat( mirror.getClass().getInterfaces()[0].getName(), is( ConsoleLogger.class.getName() ) );
        assertThat( mirror, is( not( sameInstance( (Object) logger ) ) ) );
        assertThat( mirror, is( instanceOf( ConsoleLoggerDecorator.class ) ) );
        invokeMethodWithArray( mirror, getMethod( mirror, "info", String.class ), "Hi There!" );
        verify( logger, times( 1 ) ).info( "Hi There!" );
    }
}