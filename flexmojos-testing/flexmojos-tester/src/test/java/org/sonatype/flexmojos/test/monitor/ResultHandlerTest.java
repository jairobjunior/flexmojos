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
package org.sonatype.flexmojos.test.monitor;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.sonatype.flexmojos.test.monitor.CommConstraints.END_OF_TEST_RUN;
import static org.sonatype.flexmojos.test.monitor.CommConstraints.END_OF_TEST_SUITE;
import static org.sonatype.flexmojos.test.monitor.CommConstraints.NULL_BYTE;
import static org.testng.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.codehaus.plexus.PlexusTestNGCase;
import org.codehaus.plexus.util.IOUtil;
import org.hamcrest.collection.IsCollectionContaining;
import org.sonatype.flexmojos.test.ThreadStatus;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class ResultHandlerTest
    extends PlexusTestNGCase
{

    private static final String REPORT2 = "Another cool report!" + END_OF_TEST_SUITE;

    private static final String REPORT1 = "Awesome complex report!" + END_OF_TEST_SUITE;

    private ResultHandler result;

    private int port;

    @BeforeMethod
    public void setUp()
        throws Exception
    {
        result = lookup( ResultHandler.class );

        ServerSocket ss = new ServerSocket( 0 );
        port = ss.getLocalPort();
        ss.close();

        set( result, "testReportPort", port );
    }

    @AfterMethod
    public void tearDown()
        throws Exception
    {
        result.stop();
    }

    @Test( timeOut = 10000 )
    public void stopNoResults()
        throws Exception
    {
        result.start(port);

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.STARTED.equals( result.getStatus() ) );

        Thread.sleep( 1000 );

        result.stop();

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.ERROR.equals( result.getStatus() ) );

        assertEquals( result.getError().getClass(), SocketException.class );
    }

    @Test( )
    public void sendResult()
        throws Exception
    {
        result.start(port);

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.STARTED.equals( result.getStatus() ) );

        Socket s = new Socket( "localhost", port );
        BufferedReader in = new BufferedReader( new InputStreamReader( s.getInputStream() ) );
        OutputStream out = s.getOutputStream();

        IOUtil.copy( REPORT1, out );
        IOUtil.copy( String.valueOf( NULL_BYTE ), out );

        Thread.yield();
        Thread.sleep( 100 );

        assertEquals( result.getTestReportData().size(), 1 );

        IOUtil.copy( REPORT2, out );
        IOUtil.copy( String.valueOf( NULL_BYTE ), out );

        Thread.yield();
        Thread.sleep( 100 );

        assertEquals( result.getTestReportData().size(), 2 );

        IOUtil.copy( END_OF_TEST_RUN, out );
        IOUtil.copy( String.valueOf( NULL_BYTE ), out );

        Thread.yield();
        Thread.sleep( 100 );

        do
        {
            Thread.yield();
            Thread.sleep( 100 );
        }
        while ( !ThreadStatus.DONE.equals( result.getStatus() ) );

        assertEquals( result.getTestReportData().size(), 2 );
        assertThat( result.getTestReportData(), IsCollectionContaining.hasItems( REPORT1, REPORT2 ) );
    }
}
