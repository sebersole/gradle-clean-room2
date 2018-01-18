/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.pubsign;/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */

import java.io.File;

import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.api.tasks.TaskDependency;

/**
 * A MavenArtifact class for the signature artifacts
 *
 * @author Steve Ebersole
 */
public class MavenPublishingSignatureArtifact implements MavenArtifact {
	private final MavenPublicationSigningTask signingTask;

	private final File fileToSign;
	private final File signatureFile;

	private final String classifier;
	private final String extension;

	public MavenPublishingSignatureArtifact(
			MavenPublicationSigningTask signingTask,
			File fileToSign,
			File signatureFile,
			String classifier,
			String extension) {
		this.signingTask = signingTask;
		this.fileToSign = fileToSign;
		this.signatureFile = signatureFile;
		this.classifier = classifier;
		this.extension = extension;
	}

	public File getFileToSign() {
		return fileToSign;
	}

	@Override
	public File getFile() {
		return signatureFile;
	}

	@Override
	public String getExtension() {
		return extension;
	}

	@Override
	public void setExtension(String s) {
		throw new UnsupportedOperationException( "org.hibernate.build.gradle.pubsign.MavenPublishingSignatureArtifact is not mutable" );
	}

	@Override
	public String getClassifier() {
		return classifier;
	}

	@Override
	public void setClassifier(String s) {
		throw new UnsupportedOperationException( "org.hibernate.build.gradle.pubsign.MavenPublishingSignatureArtifact is not mutable" );
	}

	@Override
	public void builtBy(Object... objects) {
		throw new UnsupportedOperationException( "org.hibernate.build.gradle.pubsign.MavenPublishingSignatureArtifact is not mutable" );
	}

	@Override
	public TaskDependency getBuildDependencies() {
		return signingTask.getTaskDependencies();
	}
}
