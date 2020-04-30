/**
 * Copyright (c) 2010-2020 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.helvar.internal.net;

import org.eclipse.jdt.annotation.NonNullByDefault;

import java.io.IOException;

/**
 * Listener for telnet session events.
 *
 * @author Unknown - Initial contribution
 * @author Tom Playford - Adopted for Helvar Handler
 *
 */
@NonNullByDefault
public interface TelnetSessionListener {

    void inputAvailable();

    void error(IOException exception);
}
