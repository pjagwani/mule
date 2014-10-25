/*
 * Copyright (c) MuleSoft, Inc.  All rights reserved.  http://www.mulesoft.com
 * The software in this package is published under the terms of the CPAL v1.0
 * license, a copy of which has been included with this distribution in the
 * LICENSE.txt file.
 */
package org.mule.module.oauth2.internal.state;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the OAuth state for a particular config
 */
public class ConfigOAuthState
{

    private Map<String, UserOAuthState> oauthStatePerUser = new HashMap<String, UserOAuthState>();

    /**
     * Retrieves the oauth state for a particular user. If there's no state for that user a new state is retrieve so never returns null.
     *
     * @param userId id of the user.
     * @return oauth state
     */
    public UserOAuthState getStateForUser(String userId)
    {
        if (!oauthStatePerUser.containsKey(userId))
        {
            oauthStatePerUser.put(userId, new UserOAuthState());
        }
        return oauthStatePerUser.get(userId);
    }
}
