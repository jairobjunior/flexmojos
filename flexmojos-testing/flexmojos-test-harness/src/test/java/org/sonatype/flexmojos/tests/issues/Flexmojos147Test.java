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
package org.sonatype.flexmojos.tests.issues;

import java.io.File;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.testng.Assert;
import org.testng.annotations.Test;

public class Flexmojos147Test
    extends AbstractIssueTest
{

    @SuppressWarnings( "unchecked" )
    @Test
    public void m2Home()
        throws Exception
    {
        File testDir = getProject( "/issues/flexmojos-147" );
        test( testDir, "eclipse:clean" );
        test( testDir, "flexmojos:flexbuilder" );

        SAXReader reader = new SAXReader();
        Document document = reader.read( new File( testDir, ".actionScriptProperties" ) );

        List<Node> nodes =
            document.selectNodes( "/actionScriptProperties/compiler/libraryPath/libraryPathEntry[@kind=3]" );

        if ( nodes.size() != 13 )
        {
            Assert.fail( "Expected 13 ${M2_HOME} macros, but found " + nodes.size() );
        }

        for ( Node node : nodes )
        {
            String path = node.valueOf( "@path" );
            if ( !path.startsWith( "${M2_HOME}" ) )
            {
                Assert.fail( "${M2_HOME} prefix not found for: " + path );
            }
        }
    }

}
