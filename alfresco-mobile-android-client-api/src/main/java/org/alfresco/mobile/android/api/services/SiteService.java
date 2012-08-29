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
package org.alfresco.mobile.android.api.services;

import java.util.List;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Site;

/**
 * Sites are a key concept within Alfresco Share for managing documents, wiki
 * pages, blog posts, discussions, and other collaborative content relating to
 * teams, projects, communities of interest, and other types of collaborative
 * sites. </br> There are various methods relating to the Sites service,
 * including the ability to:
 * <ul>
 * <li>List Sites (Favorites, all sites, user are member of)</li>
 * </ul>
 * 
 * @author Jean Marie Pascal
 */
public interface SiteService
{

    /**
     * @param siteShortName : Unique identifier name of the site.
     * @return Returns a site with the given short name, if the site doesn’t
     *         exist null is returned.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public Site getSite(String siteShortName) throws AlfrescoServiceException;

    /**
     * @return Return a list of all the sites in the repository the current user
     *         has visibility of.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public List<Site> getAllSites() throws AlfrescoServiceException;

    /**
     * @param listingContext : Listing context that define the behaviour of
     *            paging results
     *            {@link org.alfresco.mobile.android.api.model.ListingContext
     *            ListingContext}
     * @return Return a paged list of all the sites in the repository the
     *         current user has visibility of.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public PagingResult<Site> getAllSites(ListingContext listingContext) throws AlfrescoServiceException;

    /**
     * @return Returns a list of sites the current user has a explicit
     *         membership to.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public List<Site> getSites() throws AlfrescoServiceException;

    /**
     * @param listingContext : Listing context that define the behaviour of
     *            paging results
     *            {@link org.alfresco.mobile.android.api.model.ListingContext
     *            ListingContext}
     * @return Returns a paged list of sites the current user has a explicit
     *         membership to.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public PagingResult<Site> getSites(ListingContext listingContext) throws AlfrescoServiceException;

    /**
     * @return Returns a list of sites the current user has a explicit
     *         membership to and has marked as a favourite.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public List<Site> getFavoriteSites() throws AlfrescoServiceException;

    /**
     * @param listingContext : Listing context that define the behaviour of
     *            paging results
     *            {@link org.alfresco.mobile.android.api.model.ListingContext
     *            ListingContext}
     * @return Returns a paged list of sites the current user has a explicit
     *         membership to and has marked as a favourite.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public PagingResult<Site> getFavoriteSites(ListingContext listingContext) throws AlfrescoServiceException;

    /**
     * Get the documents container folder for the given site.
     * 
     * @param site : Unique identifier name of the site.
     * @return Returns the root folder container to share document library.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public Folder getDocumentLibrary(Site site) throws AlfrescoServiceException;

}