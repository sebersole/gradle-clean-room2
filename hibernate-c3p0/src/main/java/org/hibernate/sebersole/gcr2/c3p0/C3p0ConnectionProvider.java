/*
 * Hibernate, Relational Persistence for Idiomatic Java
 *
 * License: GNU Lesser General Public License (LGPL), version 2.1 or later
 * See the lgpl.txt file in the root directory or http://www.gnu.org/licenses/lgpl-2.1.html
 */
package org.hibernate.sebersole.gcr2.c3p0;

import java.sql.Connection;

import org.hibernate.sebersole.gcr2.ConnectionProvider;

/**
 * @author Steve Ebersole
 */
public class C3p0ConnectionProvider implements ConnectionProvider {
	@Override
	public Connection acquireConnection() {
		throw new UnsupportedOperationException( "not yet implemented" );
	}

	@Override
	public void releaseConnection(Connection connection) {
		throw new UnsupportedOperationException( "not yet implemented" );
	}
}
