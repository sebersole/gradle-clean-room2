/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.pubsign;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

import org.gradle.api.GradleException;
import org.gradle.api.internal.AbstractTask;
import org.gradle.api.publish.maven.MavenArtifact;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.OutputDirectory;
import org.gradle.api.tasks.TaskAction;
import org.gradle.plugins.signing.signatory.pgp.PgpSignatory;
import org.gradle.plugins.signing.signatory.pgp.PgpSignatoryFactory;
import org.gradle.plugins.signing.type.pgp.ArmoredSignatureType;

/**
 * @author Steve Ebersole
 */
public class MavenPublicationSigningTask extends AbstractTask {
	private MavenPublication mavenPublication;
	private File signatureDirectory;

	public MavenPublicationSigningTask() {
	}

	public MavenPublication getMavenPublication() {
		return mavenPublication;
	}

	public void setMavenPublication(MavenPublication mavenPublication) {
		this.mavenPublication = mavenPublication;
	}

	@OutputDirectory
	public File getSignatureDirectory() {
		return signatureDirectory;
	}

	public void setSignatureDirectory(File signatureDirectory) {
		this.signatureDirectory = signatureDirectory;
	}

	@Input
	public String getPom() {
		final StringBuffer buffer = new StringBuffer();
		mavenPublication.getPom().withXml( xmlProvider -> buffer.append( xmlProvider.asString() ) );
		return buffer.toString();
	}

	@InputFiles
	public Set<File> getArtifactsToSign() {
		return mavenPublication.getArtifacts().stream()
				.filter( mavenArtifact -> ! MavenPublishingSignatureArtifact.class.isInstance( mavenArtifact ) )
				.map( MavenArtifact::getFile )
				.collect( Collectors.toSet() );
	}


	@TaskAction
	public void generateSignatures() {
		final ArmoredSignatureType signatureType = new ArmoredSignatureType();
		final PgpSignatory pgpSignatory = new PgpSignatoryFactory().createSignatory( getProject() );

		getProject().getLogger().debug(
				"Generating signatures for MavenPublication `{}:{}`",
				getProject().getName(),
				mavenPublication.getName()
		);

		//noinspection ResultOfMethodCallIgnored
		signatureDirectory.mkdirs();

		final List<MavenArtifact> signatureArtifacts = new ArrayList<>();

		// for each artifact in this publication, do generate the signature and add the
		// signature as a new MavenArtifact
		for ( MavenArtifact mavenArtifact : mavenPublication.getArtifacts() ) {
			final File fileToSign = mavenArtifact.getFile();
			final File signatureFile = new File( signatureDirectory, mavenArtifact.getFile().getName() + ".asc" );

			sign( fileToSign, signatureFile, signatureType, pgpSignatory );

			signatureArtifacts.add(
					new MavenPublishingSignatureArtifact(
							this,
							fileToSign,
							signatureFile,
							mavenArtifact.getClassifier(),
							"jar.asc"
					)
			);
		}

		mavenPublication.getArtifacts().addAll( signatureArtifacts );

		// also sign the POM file...
		final File generatedPomFile = Utils.getPomFileCollection( mavenPublication ).getSingleFile();
		final File pomSignatureFile = new File(
				signatureDirectory,
				String.format(
						Locale.ROOT,
						"%s-%s.pom.asc",
						mavenPublication.getArtifactId(),
						mavenPublication.getVersion()
				)
		);
		sign( generatedPomFile, pomSignatureFile, signatureType, pgpSignatory );
		mavenPublication.getArtifacts().add(
				new MavenPublishingSignatureArtifact(
						this,
						generatedPomFile,
						pomSignatureFile,
						null,
						"pom.asc"
				)
		);
	}

	private void sign(
			File fileToSign,
			File signatureFile,
			ArmoredSignatureType signatureType,
			PgpSignatory pgpSignatory) {
		getProject().getLogger().info(
				"Signing file `{}` [=> `{}`]",
				fileToSign.getAbsolutePath(),
				signatureFile.getAbsolutePath()
		);

		makeFileExist( signatureFile );


		try ( final FileInputStream inputStream = new FileInputStream( fileToSign ) ) {
			try ( final FileOutputStream outputStream = new FileOutputStream( signatureFile ) ) {
				try {
					signatureType.sign( pgpSignatory, inputStream, outputStream );
				}
				catch (Exception e) {
					throw new GradleException( "Unable to generate signature file : " + signatureFile.getAbsolutePath(), e );
				}
			}
			catch (IOException e) {
				throw new GradleException( "Unable to open OutputStream over signature file : " + signatureFile.getAbsolutePath(), e );
			}
		}
		catch (IOException e) {
			throw new GradleException( "Unable to open InputStream over file to sign : " + fileToSign.getAbsolutePath(), e );
		}
	}

	@SuppressWarnings("ResultOfMethodCallIgnored")
	private static void makeFileExist(File file) {
		if ( file.exists() ) {
			return;
		}

		try {
			file.createNewFile();
		}
		catch (IOException ignore) {
		}
	}
}
