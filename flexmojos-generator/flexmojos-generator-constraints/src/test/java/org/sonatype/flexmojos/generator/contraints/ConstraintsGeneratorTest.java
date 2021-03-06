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
package org.sonatype.flexmojos.generator.contraints;

import java.io.File;

import org.codehaus.plexus.PlexusTestCase;
import org.sonatype.flexmojos.generator.api.GenerationException;
import org.sonatype.flexmojos.generator.api.GenerationRequest;
import org.sonatype.flexmojos.generator.api.Generator;

public class ConstraintsGeneratorTest
    extends PlexusTestCase
{

    private Generator generator;

    @Override
    protected void setUp()
        throws Exception
    {
        super.setUp();

        this.generator = lookup( Generator.class, "constraints" );
    }

    public void testGenerate()
        throws GenerationException
    {
        GenerationRequest request = new GenerationRequest();
        request.setTransientOutputFolder( new File( "./target/files" ) );
        request.addClass( "org.sonatype.flexmojos.generator.contraints.ConstraintDemo", null );
        request.setClassLoader( Thread.currentThread().getContextClassLoader() );

        generator.generate( request );
    }

}
