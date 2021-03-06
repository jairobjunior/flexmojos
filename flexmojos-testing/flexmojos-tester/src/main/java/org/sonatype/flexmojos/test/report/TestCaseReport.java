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
package org.sonatype.flexmojos.test.report;

import java.util.ArrayList;
import java.util.List;

import org.codehaus.plexus.util.xml.Xpp3Dom;

@SuppressWarnings( "unused" )
public class TestCaseReport
{

    private int errors;

    private int failures;

    private List<TestMethodReport> methods;

    private String name;

    private int tests;

    private double time;

    private Xpp3Dom dom;

    public TestCaseReport( Xpp3Dom dom )
    {
        this.dom = dom;
    }

    public int getErrors()
    {
        return Integer.parseInt( dom.getAttribute( "errors" ) );
    }

    public int getFailures()
    {
        return Integer.parseInt( dom.getAttribute( "failures" ) );
    }

    public List<TestMethodReport> getMethods()
    {
        if ( this.methods == null )
        {
            this.methods = new ArrayList<TestMethodReport>();
            for ( Xpp3Dom child : dom.getChildren( "testcase" ) )
            {
                methods.add( new TestMethodReport( child ) );
            }
        }
        return this.methods;
    }

    public String getName()
    {
        return dom.getAttribute( "name" );
    }

    public int getTests()
    {
        return Integer.parseInt( dom.getAttribute( "tests" ) );
    }

    public double getTime()
    {
        return Double.parseDouble( dom.getAttribute( "time" ) );
    }

    public void setErrors( int errors )
    {
        throw new UnsupportedOperationException();
    }

    public void setFailures( int failures )
    {
        throw new UnsupportedOperationException();
    }

    public void setMethods( List<TestMethodReport> methods )
    {
        throw new UnsupportedOperationException();
    }

    public void setName( String name )
    {
        throw new UnsupportedOperationException();
    }

    public void setTests( int tests )
    {
        throw new UnsupportedOperationException();
    }

    public void setTime( double time )
    {
        throw new UnsupportedOperationException();
    }

}
