// SPDX-FileCopyrightText: Copyright Contributors to the GXF project
//
// SPDX-License-Identifier: Apache-2.0
package org.lfenergy.gxf.protocol.adapter.oslp.mikronika.device.communication.exception

import java.security.GeneralSecurityException

class PrivateKeyException(
    message: String,
) : GeneralSecurityException(message)
