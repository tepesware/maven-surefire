package org.apache.maven.surefire.its;

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

import com.googlecode.junittoolbox.ParallelParameterized;
import org.apache.maven.surefire.its.fixture.OutputValidator;
import org.apache.maven.surefire.its.fixture.SurefireJUnit4IntegrationTestCase;
import org.apache.maven.surefire.its.fixture.SurefireLauncher;
import org.apache.maven.surefire.its.fixture.TestFile;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.ArrayList;

import static java.nio.charset.StandardCharsets.UTF_8;

/**
 * Basic suite test using all known versions of JUnit 4.x
 *
 * @author Kristian Rosenvold
 */
@RunWith( ParallelParameterized.class )
public class ConsoleOutputIT
    extends SurefireJUnit4IntegrationTestCase
{
    @Parameters
    public static Iterable<Object[]> data()
    {
        ArrayList<Object[]> args = new ArrayList<>();
        args.add( new Object[] { "tcp" } );
        args.add( new Object[] { null } );
        return args;
    }

    @Parameter
    @SuppressWarnings( "checkstyle:visibilitymodifier" )
    public String profileId;

    @Test
    public void properNewlinesAndEncodingWithDefaultEncodings()
    {
        SurefireLauncher launcher =
            unpack( "/consoleOutput", profileId == null ? "" : profileId )
                .forkOnce();

        if ( profileId != null )
        {
            launcher.activateProfile( "tcp" );
        }

        OutputValidator outputValidator = launcher.executeTest();

        validate( outputValidator, profileId == null );
    }

    @Test
    public void properNewlinesAndEncodingWithDifferentEncoding()
    {
        SurefireLauncher launcher =
            unpack( "/consoleOutput", profileId == null ? "" : profileId )
                .forkOnce()
                .argLine( "-Dfile.encoding=UTF-16" );

        if ( profileId != null )
        {
            launcher.activateProfile( "tcp" );
        }

        OutputValidator outputValidator = launcher.executeTest();

        validate( outputValidator, profileId == null );
    }

    @Test
    public void properNewlinesAndEncodingWithoutFork()
    {
        SurefireLauncher launcher =
            unpack( "/consoleOutput", profileId == null ? "" : profileId )
                .forkNever();

        if ( profileId != null )
        {
            launcher.activateProfile( "tcp" );
        }

        OutputValidator outputValidator = launcher.executeTest();

        validate( outputValidator, false );
    }

    private void validate( final OutputValidator outputValidator, boolean includeShutdownHook )
    {
        TestFile xmlReportFile = outputValidator.getSurefireReportsXmlFile( "TEST-consoleOutput.Test1.xml" );
        xmlReportFile.assertContainsText( "SoutLine" );
        xmlReportFile.assertContainsText(  "äöüß" );
        xmlReportFile.assertContainsText(  "failing with ü" );

        TestFile outputFile = outputValidator.getSurefireReportsFile( "consoleOutput.Test1-output.txt", UTF_8 );
        outputFile.assertContainsText( "SoutAgain" );
        outputFile.assertContainsText( "SoutLine" );
        outputFile.assertContainsText( "äöüß" );

        if ( includeShutdownHook )
        {
            //todo it should not be reported in the last test which is completed
            //todo this text should be in null-output.txt
            outputFile.assertContainsText( "Printline in shutdown hook" );
        }
    }

    @Test
    public void largerSoutThanMemory()
    {
        SurefireLauncher launcher =
            unpack( "consoleoutput-noisy", profileId == null ? "" : "-" + profileId )
                .setMavenOpts( "-Xmx64m" )
                .sysProp( "thousand", "32000" );

        if ( profileId != null )
        {
            launcher.activateProfile( "tcp" );
        }

        launcher.executeTest()
            .verifyErrorFreeLog();
    }
}
