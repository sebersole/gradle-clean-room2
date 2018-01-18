/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.pubsign;

import java.io.File;

import org.gradle.api.GradleException;
import org.gradle.api.Project;

/**
 * @author Steve Ebersole
 */
public class MavenPublicationSigningExtension {
	private final Project project;

	private File signatureDirectory;


	public MavenPublicationSigningExtension(Project project) {
		this.project = project;
		this.signatureDirectory = new File( project.getBuildDir(), "publishing/signing/maven" );
	}

	public File getSignatureDirectory() {
		return signatureDirectory;
	}

	public void setSignatureDirectory(Object reference) {
		this.signatureDirectory = project.file( reference );
		if ( this.signatureDirectory == null ) {
			throw new GradleException( "Output directory for MavenPublication signing cannot be null" );
		}
	}
}
