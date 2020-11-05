package io.github.jhannes.execjar.mojo;

/*
 * Copyright 2001-2005 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import execjar.ExecJarFactory;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.plugins.annotations.ResolutionScope;
import org.apache.maven.project.MavenProject;

import java.io.File;
import java.io.IOException;
import java.util.Set;

@Mojo(name = "execjar", defaultPhase = LifecyclePhase.PACKAGE, requiresDependencyResolution = ResolutionScope.RUNTIME)
public class ExecJarMojo extends AbstractMojo {

    @Parameter(defaultValue = "${project.build.directory}")
    private File outputDirectory;

    @Parameter(defaultValue = "${project}", required = true, readonly = true)
    private MavenProject project;

    @Parameter(property = "mainClass")
    private String mainClass;


    public void execute() throws MojoExecutionException {

        ExecJarFactory execJarFactory = new ExecJarFactory();
        execJarFactory.setMainClass(mainClass);
        execJarFactory.setOutputFile(new File(outputDirectory, "output.jar"));

        if (project.getArtifact().getFile() == null) {
            getLog().error("Artifact file is null: " + project.getArtifact());
        }
        execJarFactory.addDependency(project.getArtifact().getFile());
        Set<Artifact> dependencies = project.getArtifacts();
        for (Artifact dependency : dependencies) {
            if (!dependency.isOptional()) {
                getLog().info(dependency.getScope() + " dependency " + dependency);
                getLog().info(dependency.getFile().toString());
                execJarFactory.addDependency(dependency.getFile());
            }
        }

        try {
            execJarFactory.execute();
        } catch (IOException e) {
            throw new MojoExecutionException("Failed to create " + execJarFactory.getOutputFile(), e);
        }

        getLog().debug(execJarFactory.getEntries().toString());
        if (!execJarFactory.getDuplicateEntries().isEmpty()) {
            getLog().warn("Duplicates: " + execJarFactory.getDuplicateEntries());
        }
    }

}
