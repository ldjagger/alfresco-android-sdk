/*******************************************************************************
 * Copyright (C) 2005-2012 Alfresco Software Limited.
 * 
 * This file is part of the Alfresco Mobile SDK.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 ******************************************************************************/
package org.alfresco.mobile.android.api.model.impl;

import java.util.HashMap;
import java.util.Map;

import org.alfresco.mobile.android.api.model.RepositoryCapabilities;

/**
 * 
 * @author Jean Marie Pascal
 *
 */
public abstract class AbstractRepositoryCapabilities implements RepositoryCapabilities
{

    /** Map of Alfresco Specific capabilities. */
    protected Map<String, Boolean> capabilities = new HashMap<String, Boolean>(2);

    // ////////////////////////////////////////////////////////////////////////////////////
    // DEVELOPER Flags
    // ////////////////////////////////////////////////////////////////////////////////////
    /**
     * Like action and LikeService are only available since Alfresco V4. This
     * flag indicate if this feature is available with the current repository.
     * NB : It's a simple test based on repository Informations version and
     * edition.
     * 
     * @return true if version 4 or superior of Alfresco. false in other case.
     */
    public boolean doesSupportLikingNodes()
    {
        return capabilities.get(CAPABILITY_LIKE);
    }

    /**
     * Comment count is only available since Alfresco V4. This flag indicate if
     * this feature is available with the current repository. NB : It's a simple
     * test based on repository informations version and edition.
     * 
     * @return true if version 4 or superior of Alfresco. false in other case.
     */
    public boolean doesSupportCommentsCount()
    {
        return capabilities.get(CAPABILITY_COMMENTS_COUNT);
    }

    @Override
    public boolean doesSupportCapability(String capability)
    {
        return capabilities.get(capability);
    }
}
