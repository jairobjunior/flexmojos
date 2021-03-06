/**
 *   Copyright 2008 Marvin Herman Froeder
 * -->
 * <!--
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * -->
 *
 * <!--
 *     http://www.apache.org/licenses/LICENSE-2.0
 * -->
 *
 * <!--
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.sonatype.flexmojos.test;

import static org.testng.AssertJUnit.assertEquals;

import java.io.File;
import java.io.FileReader;

import org.apache.maven.it.VerificationException;
import org.codehaus.plexus.util.xml.Xpp3DomBuilder;
import org.sonatype.flexmojos.test.report.TestCaseReport;
import org.sonatype.flexmojos.tests.AbstractFlexMojosTests;
import org.testng.annotations.Test;

public class IT0013IssuesTest
    extends AbstractFlexMojosTests
{

    public void testIssue( String issueNumber )
        throws Exception
    {
        File testDir = getProject( "/issues/" + issueNumber );
        test( testDir, "install" );
    }

    @Test
    public void issue11()
        throws Exception
    {
        testIssue( "issue-0011" );
    }

    @Test
    public void issue13()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0013" );
        test( testDir, "install" );

        File reportDir = new File( testDir, "target/surefire-reports" );
        assertEquals( 2, reportDir.listFiles().length );
    }

    @Test( expectedExceptions = { VerificationException.class } )
    public void issue14()
        throws Exception
    {
        testIssue( "issue-0014" );
    }

    @Test( expectedExceptions = { VerificationException.class } )
    public void issue15()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0015" );
        try
        {
            test( testDir, "install" );
        }
        finally
        {
            File reportDir = new File( testDir, "target/surefire-reports" );
            assertEquals( 2, reportDir.listFiles().length );

            File reportFile = new File( reportDir, "TEST-com.adobe.example.TestCalculator.xml" );
            TestCaseReport report = new TestCaseReport( Xpp3DomBuilder.build( new FileReader( reportFile ) ) );

            assertEquals( "com.adobe.example.TestCalculator", report.getName() );
            assertEquals( 2, report.getTests() );
            assertEquals( 1, report.getErrors() );
        }

    }

    @Test( expectedExceptions = { VerificationException.class } )
    public void issue27()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0027" );
        test( testDir, "org.sonatype.flexmojos:flexmojos-maven-plugin:" + getProperty( "version" ) + ":asdoc" );
    }

    @Test
    public void issue29()
        throws Exception
    {
        testIssue( "issue-0029" );
    }

    @Test
    public void issue32()
        throws Exception
    {
        testIssue( "issue-0032" );
    }

    @Test
    public void issue39()
        throws Exception
    {
        testIssue( "issue-0039" );
    }

    // A wierd but on this tests
    // @Test(timeOut=120000) public void issue43() throws Exception {
    // File testDir = ResourceExtractor.simpleExtractResources(getClass(),
    // "/issues/issue-0014");
    // List<String> args = new ArrayList<String>();
    // args.add("-Dmaven.test.failure.ignore=true");
    // test(testDir, "info.rvin.itest.issues", "issue-0014",
    // "1.0-SNAPSHOT", "swf", "install", args);
    // }

    @Test( expectedExceptions = { VerificationException.class } )
    public void issue44()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0044" );
        test( testDir, "org.sonatype.flexmojos:flexmojos-maven-plugin:" + getProperty( "version" ) + ":asdoc" );
    }

    @Test
    public void issue53()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0014" );
        test( testDir, "install", "-Dmaven.test.skip=true" );

        test( testDir, "install", "-DskipTests=true" );
    }

    @Test( groups = { "generator" } )
    public void issue61()
        throws Exception
    {
        testIssue( "issue-0061" );
    }

    @Test
    public void issue68()
        throws Exception
    {
        File testDir = getProject( "/issues/issue-0068" );
        test( testDir, "org.sonatype.flexmojos:flexmojos-maven-plugin:" + getProperty( "version" ) + ":asdoc" );
    }

    @Test
    public void issue70()
        throws Exception
    {
        testIssue( "issue-0070" );
    }

    @Test
    public void issue103()
        throws Exception
    {
        testIssue( "issue-0103/project" );
        // TODO check SWC content
    }

}
