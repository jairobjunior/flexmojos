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
import java.net.URL;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.factory.ArtifactFactory;
import org.apache.maven.artifact.metadata.ArtifactMetadataRetrievalException;
import org.apache.maven.artifact.metadata.ArtifactMetadataSource;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.resolver.AbstractArtifactResolutionException;
import org.apache.maven.artifact.resolver.ArtifactResolutionResult;
import org.apache.maven.artifact.resolver.ArtifactResolver;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.model.Build;
import org.apache.maven.model.Resource;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.artifact.InvalidDependencyVersionException;

import eu.cedarsoft.utils.ZipExtractor;

/**
 * Utility class to help get information from Maven objects like files, source paths, resolve dependencies, etc.
 * 
 * @author velo.br
 */
public class MavenUtils
{

    private static final String FREE_BSD = "freebsd";

    private static final String WINDOWS_OS = "windows";

    private static final String MAC_OS = "mac os x";

    private static final String MAC_OS_DARWIN = "darwin";

    private static final String LINUX_OS = "linux";

    private static final String SOLARIS_OS = "sunos";

    private static final String VISTA = "vista";

    private static final Properties flexmojosProperties;

    static
    {
        flexmojosProperties = new Properties();
        try
        {
            flexmojosProperties.load( MavenUtils.class.getResourceAsStream( "/flexmojos.properties" ) );
        }
        catch ( IOException e )
        {
            throw new RuntimeException( "Unable to load flexmojos.properties", e );
        }
    }

    private MavenUtils()
    {
    }

    /**
     * Resolve a resource file in a maven project resources folders
     * 
     * @param project maven project
     * @param fileName sugested name on pom
     * @return source file or null if source not found
     * @throws MojoFailureException
     */
    @SuppressWarnings( "unchecked" )
    public static File resolveResourceFile( MavenProject project, String fileName )
        throws MojoExecutionException
    {

        File file = new File( fileName );

        if ( file.exists() )
        {
            return file;
        }

        if ( file.isAbsolute() )
        {
            throw new MojoExecutionException( "File " + fileName + " not found" );
        }

        List<Resource> resources = project.getBuild().getResources();

        for ( Resource resourceFolder : resources )
        {
            File resource = new File( resourceFolder.getDirectory(), fileName );
            if ( resource.exists() )
            {
                return resource;
            }
        }

        throw new MojoExecutionException( "File " + fileName + " not found" );
    }

    /**
     * Get dependency artifacts for a project using the local and remote repositories to resolve the artifacts
     * 
     * @param project maven project
     * @param resolver artifact resolver
     * @param localRepository artifact repository
     * @param remoteRepositories List of remote repositories
     * @param artifactMetadataSource artifactMetadataSource
     * @param artifactFactory TODO
     * @return all dependencies from the project
     * @throws MojoExecutionException thrown if an exception occured during artifact resolving
     */
    @SuppressWarnings( "unchecked" )
    public static Set<Artifact> getDependencyArtifacts( MavenProject project, ArtifactResolver resolver,
                                                        ArtifactRepository localRepository, List remoteRepositories,
                                                        ArtifactMetadataSource artifactMetadataSource,
                                                        ArtifactFactory artifactFactory )
        throws MojoExecutionException
    {
        Set<Artifact> artifacts = project.getDependencyArtifacts();
        if ( artifacts == null )
        {
            try
            {
                artifacts = project.createArtifacts( artifactFactory, null, null );
            }
            catch ( InvalidDependencyVersionException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
            project.setDependencyArtifacts( artifacts );
        }

        ArtifactResolutionResult arr;
        try
        {
            arr =
                resolver.resolveTransitively( project.getDependencyArtifacts(), project.getArtifact(),
                                              remoteRepositories, localRepository, artifactMetadataSource );
        }
        catch ( AbstractArtifactResolutionException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }

        Set<Artifact> result = arr.getArtifacts();

        // ## 6/18/09 StoneRiver Change to resolve RELEASE Artifact version ##
        for ( Artifact artifact : result )
        {
            if ( artifact.getVersion().equals( Artifact.RELEASE_VERSION ) )
            {
                getReleaseVersion( artifact, localRepository, remoteRepositories, artifactMetadataSource );
            }
        }

        return result;

    }

    /**
     * Get the Release version for the Artifact.
     * 
     * @param artifact The artifact to update with the release version.
     * @param localRepository artifact repository
     * @param remoteRepositories List of remote repositories
     * @param artifactMetadataSource artifactMetadataSource
     * @throws MojoExecutionException thrown if an exception occured during artifact resolving
     */
    @SuppressWarnings( "unchecked" )
    private static void getReleaseVersion( Artifact artifact, ArtifactRepository localRepository,
                                           List remoteRepositories, ArtifactMetadataSource artifactMetadataSource )
        throws MojoExecutionException
    {
        try
        {
            List<ArtifactVersion> artifactVersions =
                artifactMetadataSource.retrieveAvailableVersions( artifact, localRepository, remoteRepositories );
            if ( artifactVersions != null && !artifactVersions.isEmpty() )
            {
                ArtifactVersion release = artifactVersions.get( artifactVersions.size() - 1 );
                artifact.setBaseVersion( release.toString() );
                artifact.setVersion( release.toString() );
            }
        }
        catch ( ArtifactMetadataRetrievalException ex )
        {
            throw new MojoExecutionException( "Error retrieving release version for artifact: " + artifact.getId(), ex );
        }
    }

    /**
     * Get the file reference of an SWC artifact.<br>
     * If the artifact file does not exist in the [build-dir]/libraries/[scope] directory, the artifact file is copied
     * to that location.
     * 
     * @param a artifact for which to retrieve the file reference
     * @param build build for which to get the artifact
     * @return swc artifact file reference
     * @throws MojoExecutionException thrown if an IOException occurs while copying the file to the
     *             [build-dir]/libraries/[scope] directory
     */
    public static File getArtifactFile( Artifact a, Build build )
        throws MojoExecutionException
    {
        File dest =
            new File( build.getOutputDirectory(), "libraries/" + a.getArtifactId()
                + ( a.getClassifier() != null ? ( "-" + a.getClassifier() ) : "" ) + ".swc" );
        if ( !dest.exists() )
        {
            try
            {
                FileUtils.copyFile( a.getFile(), dest );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( e.getMessage(), e );
            }
        }
        return dest;
    }

    /**
     * Use the resolver to resolve the given artifact in the local or remote repositories.
     * 
     * @param project Active project
     * @param artifact Artifact to be resolved
     * @param resolver ArtifactResolver to use for resolving the artifact
     * @param localRepository ArtifactRepository
     * @param remoteRepositories List of remote artifact repositories
     * @throws MojoExecutionException thrown if an exception occured during artifact resolving
     * @return resolved artifact
     */
    @SuppressWarnings( "unchecked" )
    public static Artifact resolveArtifact( MavenProject project, Artifact artifact, ArtifactResolver resolver,
                                            ArtifactRepository localRepository, List remoteRepositories )
        throws MojoExecutionException
    {
        try
        {
            artifact = project.replaceWithActiveArtifact( artifact );
            if ( !artifact.isResolved() )
            {
                resolver.resolve( artifact, remoteRepositories, localRepository );
            }
            return artifact;
        }
        catch ( AbstractArtifactResolutionException e )
        {
            throw new MojoExecutionException( e.getMessage(), e );
        }
    }

    /**
     * Returns the file reference to the fonts file. Depending on the os, the correct fonts.ser file is used. The fonts
     * file is copied to the build directory.
     * 
     * @param build Build for which to get the fonts file
     * @return file reference to fonts file
     * @throws MojoExecutionException thrown if the config file could not be copied to the build directory
     */
    public static File getFontsFile( Build build )
        throws MojoExecutionException
    {
        URL url;
        if ( MavenUtils.isMac() )
        {
            url = MavenUtils.class.getResource( "/fonts/macFonts.ser" );
        }
        else
        {
            // TODO And linux?!
            // if(os.contains("windows")) {
            url = MavenUtils.class.getResource( "/fonts/winFonts.ser" );
        }
        File fontsSer = new File( build.getOutputDirectory(), "fonts.ser" );
        try
        {
            FileUtils.copyURLToFile( url, fontsSer );
        }
        catch ( IOException e )
        {
            throw new MojoExecutionException( "Error copying fonts file.", e );
        }
        return fontsSer;
    }

    /**
     * Returns the file reference to a localize resourceBundlePath. Replaces the {locale} variable in the given
     * resourceBundlePath with given locale.
     * 
     * @param resourceBundlePath Path to resource bundle.
     * @param locale Locale
     * @return File reference to the resourceBundlePath for given locale
     */
    public static File getLocaleResourcePath( String resourceBundlePath, String locale )
    {
        String path = resourceBundlePath.replace( "{locale}", locale );
        File localePath = new File( path );
        return localePath.exists() ? localePath : null;
    }

    public static String osString()
    {
        return System.getProperty( "os.name" ).toLowerCase();
    }

    /**
     * Return a boolean to show if we are running on Windows.
     * 
     * @return true if we are running on Windows.
     */
    public static boolean isWindows()
    {
        return osString().startsWith( WINDOWS_OS );
    }

    /**
     * Return a boolean to show if we are running on Linux.
     * 
     * @return true if we are running on Linux.
     */
    public static boolean isLinux()
    {
        return osString().startsWith( LINUX_OS ) ||
        // I know, but people said that workds...
            osString().startsWith( FREE_BSD );
    }

    /**
     * Return a boolean to show if we are running on Solaris.
     * 
     * @return true if we are running on Solaris.
     */
    public static boolean isSolaris()
    {
        return osString().startsWith( SOLARIS_OS );
    }

    /**
     * Return a boolean to show if we are running on a unix-based OS.
     * 
     * @return true if we are running on a unix-based OS.
     */
    public static boolean isUnixBased()
    {
        return isLinux() || isSolaris();
    }

    /**
     * Return a boolean to show if we are running on Mac OS X.
     * 
     * @return true if we are running on Mac OS X.
     */
    public static boolean isMac()
    {
        return osString().startsWith( MAC_OS ) || osString().startsWith( MAC_OS_DARWIN );
    }

    /**
     * Return a boolean to show if we are running on Windows Vista.
     * 
     * @return true if we are running on Windows Vista.
     */
    public static boolean isWindowsVista()
    {
        return osString().startsWith( WINDOWS_OS ) && osString().contains( VISTA );
    }

    public static Artifact searchFor( Collection<Artifact> artifacts, String groupId, String artifactId,
                                      String version, String type, String classifier )
    {
        for ( Artifact artifact : artifacts )
        {
            if ( equals( artifact.getGroupId(), groupId ) && equals( artifact.getArtifactId(), artifactId )
                && equals( artifact.getVersion(), version ) && equals( artifact.getType(), type )
                && equals( artifact.getClassifier(), classifier ) )
            {
                return artifact;
            }
        }

        return null;
    }

    private static boolean equals( String str1, String str2 )
    {
        // If is null is not relevant
        if ( str1 == null || str2 == null )
        {
            return true;
        }

        return str1.equals( str2 );
    }

    public static String getFlexMojosVersion()
    {
        return flexmojosProperties.getProperty( "version" );
    }

    public static String replaceArtifactCoordinatesTokens( String sample, Artifact artifact )
    {
        sample = sample.replace( "{groupId}", artifact.getGroupId() );
        sample = sample.replace( "{artifactId}", artifact.getArtifactId() );
        sample = sample.replace( "{version}", artifact.getBaseVersion() );

        return sample;
    }

    public static String getRslUrl( String sample, Artifact artifact, String extension )
    {
        String url = replaceArtifactCoordinatesTokens( sample, artifact );
        url = url.replace( "{extension}", extension );

        return url;
    }

    public static String getRuntimeLocaleOutputPath( String sample, Artifact artifact, String locale, String extension )
    {
        String path = replaceArtifactCoordinatesTokens( sample, artifact );
        path = path.replace( "{locale}", locale );
        path = path.replace( "{extension}", extension );

        return path;
    }

    public static Set<File> getFilesSet( Collection<Artifact> dependencies )
    {
        if ( dependencies == null )
        {
            return null;
        }

        Set<File> files = new LinkedHashSet<File>();
        for ( Artifact artifact : dependencies )
        {
            if ( !artifact.isResolved() )
            {
                throw new IllegalArgumentException( "Unable to handle unresolved artifact: " + artifact );
            }

            if ( artifact.getFile() == null )
            {
                throw new NullPointerException( "Artifact file not defined: " + artifact );
            }

            files.add( artifact.getFile() );
        }
        return files;
    }

    public static File[] getFiles( Collection<Artifact> dependencies )
    {
        if ( dependencies == null )
        {
            return null;
        }

        return getFilesSet( dependencies ).toArray( new File[dependencies.size()] );
    }

    public static File getSparkCss( Build build, Artifact configs )
        throws MojoExecutionException
    {
        File sparkCss = null;
        if ( configs != null && configs.getFile().exists() )
        {
            ZipExtractor ze;

            File dest = new File( build.getOutputDirectory(), "configs" );
            dest.mkdirs();
            try
            {
                ze = new ZipExtractor( configs.getFile() );
                ze.extract( dest );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Unable to extract framework configs", e );
            }

            sparkCss = new File( dest, "themes/Spark/spark.css" );
        }

        if ( sparkCss == null || !sparkCss.exists() )
        {
            File fontsSer = new File( build.getOutputDirectory(), "spark.css" );
            fontsSer.getParentFile().mkdirs();
            try
            {
                FileUtils.copyURLToFile( MavenUtils.class.getResource( "/theme/spark.css" ), fontsSer );
            }
            catch ( IOException e )
            {
                throw new MojoExecutionException( "Error copying fonts file.", e );
            }
        }
        return sparkCss;
    }

}
