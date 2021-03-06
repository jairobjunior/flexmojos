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
package org.sonatype.flexmojos.utilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.maven.artifact.Artifact;
import org.apache.maven.model.Build;
import org.apache.maven.plugin.MojoExecutionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.input.SAXBuilder;

import eu.cedarsoft.utils.ZipExtractor;

public class FDKConfigResolver
{
    private File configDirectory;

    private Document config;

    private Boolean isAIR;

    private String[] fontManagers;

    private List<Namespace> namespaces;

    public FDKConfigResolver( Collection<Artifact> dependencies, Build build, String sdkVersion )
        throws MojoExecutionException
    {
        configDirectory = new File( build.getOutputDirectory(), "config-" + sdkVersion );
        if ( !configDirectory.exists() )
        {
            resolveConfigDirectory( dependencies );
        }

        for ( Artifact artifact : dependencies )
        {
            if ( "airglobal".equals( artifact.getArtifactId() ) )
            {
                isAIR = true;
                break;
            }
            else if ( "playerglobal".equals( artifact.getArtifactId() ) )
            {
                isAIR = false;
                break;
            }
        }
    }

    private void resolveConfigDirectory( Collection<Artifact> dependencies )
        throws MojoExecutionException
    {
        Artifact configArtifact =
            MavenUtils.searchFor( dependencies, "com.adobe.flex.framework", "framework", null, "zip", "configs" );
        if ( configArtifact != null )
        {
            ZipExtractor zipExtractor;
            configDirectory.mkdirs();
            try
            {
                zipExtractor = new ZipExtractor( configArtifact.getFile() );
                zipExtractor.extract( configDirectory );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Unable to extract SDK configuration", e );
            }
        }
    }

    private Document getConfig()
        throws MojoExecutionException
    {
        if ( config == null )
        {
            File configFile = new File( configDirectory, ( isAIR ? "air" : "flex" ) + "-config.xml" );
            if ( !configFile.isFile() )
            {
                throw new MojoExecutionException( "Unable to find SDK config file " + configFile );
            }

            try
            {
                SAXBuilder parser = new SAXBuilder();
                config = parser.build( configFile );
            }
            catch ( Exception e )
            {
                throw new MojoExecutionException( "Error parsing " + configFile, e );
            }
        }

        return config;
    }

    @SuppressWarnings( "unchecked" )
    public String[] getFontManagers()
        throws MojoExecutionException
    {
        if ( fontManagers == null )
        {
            if ( !configDirectory.exists() )
            {
                return null;
            }

            Element node = getConfig().getRootElement().getChild( "compiler" );
            if ( node == null )
            {
                return null;
            }
            node = node.getChild( "fonts" );
            if ( node == null )
            {
                return null;
            }
            node = node.getChild( "managers" );
            if ( node == null )
            {
                return null;
            }

            List<String> managers = new ArrayList<String>();
            for ( Element element : (Iterable<Element>) node.getChildren() )
            {
                managers.add( element.getValue() );
            }

            fontManagers = managers.toArray( new String[managers.size()] );
        }
        return fontManagers;
    }

    @SuppressWarnings( "unchecked" )
    public List<Namespace> getNamespaces()
        throws MojoExecutionException
    {
        if ( namespaces == null )
        {
            if ( !configDirectory.exists() )
            {
                return null;
            }

            List<Namespace> namespaces = new ArrayList<Namespace>();

            Element node = getConfig().getRootElement().getChild( "compiler" );
            if ( node == null )
            {
                return null;
            }
            node = node.getChild( "namespaces" );
            if ( node == null )
            {
                return null;
            }

            for ( Element element : (Iterable<Element>) node.getChildren() )
            {
                Element uriNode = element.getChild( "uri" );
                Element manifestNode = element.getChild( "manifest" );
                File manifest = new File( configDirectory, manifestNode.getValue() );
                namespaces.add( new Namespace( uriNode.getValue(), manifest ) );
            }

            this.namespaces = namespaces;
        }
        return namespaces;

    }
}
