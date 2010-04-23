/**
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
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.fusesource.hawtbuf.amqp;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.FileFilter;
import java.util.Arrays;
import java.util.List;

/**
 * A Maven Mojo so that the AMQP compiler can be used with maven.
 *
 * @goal compile
 * @phase process-sources
 */
public class AmqpMojo extends AbstractMojo {

    /**
     * The maven project.
     *
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The directory where the amqp spec files (<code>*.xml</code>) are
     * located.
     *
     * @parameter default-value="${basedir}/src/main/amqp"
     */
    private File mainSourceDirectory;

    /**
     * The directory where the output files will be located.
     *
     * @parameter default-value="${project.build.directory}/generated-sources/amqp"
     */
    private File mainOutputDirectory;

    /**
     * The directory where the amqp spec files (<code>*.xml</code>) are
     * located.
     *
     * @parameter default-value="${basedir}/src/test/amqp"
     */
    private File testSourceDirectory;

    /**
     * The directory where the output files will be located.
     *
     * @parameter default-value="${project.build.directory}/test-generated-sources/amqp"
     */
    private File testOutputDirectory;


    public void execute() throws MojoExecutionException {

        File[] mainFiles = null;
        if ( mainSourceDirectory.exists() ) {
            mainFiles = mainSourceDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".xml");
            }
        });
            if (mainFiles==null || mainFiles.length==0) {
                getLog().warn("No amqp spec files found in directory: " + mainSourceDirectory.getPath());
            } else {
                processFiles(mainFiles, mainOutputDirectory);
                this.project.addCompileSourceRoot(mainOutputDirectory.getAbsolutePath());
            }
        }

        File[] testFiles = null;
        if ( testSourceDirectory.exists() ) {
            testFiles = testSourceDirectory.listFiles(new FileFilter() {
                public boolean accept(File pathname) {
                    return pathname.getName().endsWith(".xml");
                }
            });
            if (testFiles==null || testFiles.length==0) {
                getLog().warn("No amqp spec files found in directory: " + testSourceDirectory.getPath());
            } else {
                processFiles(testFiles, testOutputDirectory);
                this.project.addTestCompileSourceRoot(testOutputDirectory.getAbsolutePath());
            }
        }
    }

    private void processFiles(File[] mainFiles, File outputDir) throws MojoExecutionException {
        List<File> recFiles = Arrays.asList(mainFiles);
        for (File file : recFiles) {
            try {
                getLog().info("Compiling: "+file.getPath());

                Generator gen = new Generator();
                gen.setInputFiles(mainFiles);
                gen.setOutputDirectory(outputDir);
//                gen.setSourceDirectory(sourceDir);
                gen.generate();
            } catch (Exception e) {
                throw new MojoExecutionException(e.getMessage(), e);
            }
        }
    }

}