/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.pubsign;

import java.io.File;
import java.util.Locale;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.repositories.ArtifactRepository;
import org.gradle.api.publish.Publication;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.api.publish.maven.MavenPublication;

/**
 * @author Steve Ebersole
 */
public class MavenPublicationSigningPlugin implements Plugin<Project> {
	public static final String SIGNING_EXTENSION_NAME = "mavenPublicationSigning";

	public static final String TASK_GROUP_NAME = "maven-publish-signing";
	public static final String LIFECYCLE_TASK_NAME = "generateSignatures";


	@Override
	public void apply(Project project) {
		project.getPluginManager().apply( "maven-publish" );

		final MavenPublicationSigningExtension signingExtension = new MavenPublicationSigningExtension( project );
		project.getExtensions().add( SIGNING_EXTENSION_NAME, signingExtension );

		final Task lifecycleTask = project.getTasks().create( LIFECYCLE_TASK_NAME );
		lifecycleTask.setGroup( TASK_GROUP_NAME );

		final PublishingExtension publishingExtension = (PublishingExtension) project.getExtensions().getByName( "publishing" );

		project.afterEvaluate( p -> {
			for ( Publication publication : publishingExtension.getPublications() ) {
				if ( ! MavenPublication.class.isInstance( publication ) ) {
					continue;
				}

				final MavenPublication mavenPublication = (MavenPublication) publication;

				p.getLogger().debug( "Creating MavenPublicationSigningTask for `{}:{}`", p.getName(), mavenPublication.getName() );

				final String capitalizedPubName = Utils.capitalize( mavenPublication.getName() );

				final String publicationSignatureTaskName = "generateSignaturesFor" + capitalizedPubName + "Publication";
				final MavenPublicationSigningTask signingTask = p.getTasks().create(
						publicationSignatureTaskName,
						MavenPublicationSigningTask.class
				);
				signingTask.setGroup( TASK_GROUP_NAME );
				signingTask.setSignatureDirectory( new File( signingExtension.getSignatureDirectory(), mavenPublication.getName() ) );
				signingTask.setMavenPublication( mavenPublication );

				lifecycleTask.dependsOn( signingTask );

				// make sure this current signingTask instance "depends on" each artifact's
				// "build dependencies" (which is often the task that produces it, e.g.).

				for ( MavenArtifact mavenArtifact : mavenPublication.getArtifacts() ) {
					signingTask.dependsOn( mavenArtifact.getBuildDependencies() );
				}

				// Same for the POM artifact...
				//
				// DefaultMavenPublication contains a `#pomFile` field as a org.gradle.api.file.FileCollection.
				// Ideally we'd use that FileCollection as a task dependency for this current `signingTask`
				// so that the POM file generation would be a dependency of signing - which makes sense.
				//
				// However, we cannot for 2 reasons:
				//		1) `DefaultMavenPublication#pomFile` is private and only part of Gradle internals - I hacked
				//			around that temporarily by using reflection
				//		2) `DefaultMavenPublication#pomFile` is not known until very late (seemingly undefined) in the Project
				//			config/init phase : https://discuss.gradle.org/t/access-generatepomfilefor-publication-from-plugin-code/25426/16
				final String pomGenerationTaskName = "generatePomFileFor" + capitalizedPubName + "Publication";
				signingTask.dependsOn( pomGenerationTaskName );


				for ( ArtifactRepository publishRepository : publishingExtension.getRepositories() ) {
					// find the publish task for this particular publication to the current repository
					final Task publishTask = project.getTasks().findByName(
							String.format(
									Locale.ROOT,
									"publish%sPublicationTo%sRepository",
									Utils.capitalize( mavenPublication.getName() ),
									Utils.capitalize( publishRepository.getName() )
							)
					);

					if ( publishTask != null ) {
						// if it is, we skip it... but here it is not null, so add our task to its dependencies
						publishTask.dependsOn( signingTask );
					}
				}

				final Task publishToMavenLocalTask = project.getTasks().getByName( "publishToMavenLocal" ).dependsOn( signingTask );
				publishToMavenLocalTask.dependsOn( signingTask );
			}
		});
	}

}
