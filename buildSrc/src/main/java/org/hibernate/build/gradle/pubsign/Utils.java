/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.build.gradle.pubsign;import java.lang.reflect.Field;

import org.gradle.api.GradleException;
import org.gradle.api.file.FileCollection;
import org.gradle.api.publish.maven.MavenPublication;

/**
 * @author Steve Ebersole
 */
public class Utils {
	public static String capitalize(String str) {
		return Character.toTitleCase( str.charAt( 0 ) ) + str.substring( 1 );
	}

	private static final String PUBLICATION_IMPL_CLASS_NAME = "org.gradle.api.publish.maven.internal.publication.DefaultMavenPublication";
	private static final Class PUBLICATION_IMPL_CLASS;
	private static final String POM_FILE_COLLECTION_FIELD_NAME = "pomFile";
	private static final Field POM_FILE_COLLECTION_FIELD;

	static {
		try {
			PUBLICATION_IMPL_CLASS = Utils.class.getClassLoader().loadClass( PUBLICATION_IMPL_CLASS_NAME );
		}
		catch (ClassNotFoundException e) {
			throw new GradleException( "Could not locate DefaultMavenPublication class : " + PUBLICATION_IMPL_CLASS_NAME, e );
		}

		try {
			POM_FILE_COLLECTION_FIELD = PUBLICATION_IMPL_CLASS.getDeclaredField( POM_FILE_COLLECTION_FIELD_NAME );
			POM_FILE_COLLECTION_FIELD.setAccessible( true );
		}
		catch (NoSuchFieldException e) {
			throw new GradleException( "Could not locate DefaultMavenPublication#pomFile field", e );
		}
	}

	public static FileCollection getPomFileCollection(MavenPublication mavenPublication) {
		try {
			final FileCollection pomFileCollection = (FileCollection) POM_FILE_COLLECTION_FIELD.get( mavenPublication );
			if ( pomFileCollection == null ) {
				throw new GradleException( "Accessing DefaultMavenPublication#pomFile (FileCollection) returned `null`" );
			}
			return pomFileCollection;
		}
		catch (IllegalAccessException e) {
			throw new GradleException( "Could not access DefaultMavenPublication#pomFile field [" + mavenPublication + "]", e );
		}
	}
}
