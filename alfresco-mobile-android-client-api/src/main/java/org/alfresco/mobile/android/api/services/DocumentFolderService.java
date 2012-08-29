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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.exceptions.AlfrescoServiceException;
import org.alfresco.mobile.android.api.model.ContentFile;
import org.alfresco.mobile.android.api.model.ContentStream;
import org.alfresco.mobile.android.api.model.Document;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.Node;
import org.alfresco.mobile.android.api.model.PagingResult;
import org.alfresco.mobile.android.api.model.Permissions;

/**
 * DocumentFolderService manages Folders and Documents in an Alfresco
 * repository. The service provides methods to create and update nodes. The
 * DocumentFolderService supports the following methods:
 * <ul>
 * <li>Create nodes and set property values</li>
 * <li>Read node properties and content, read and navigate node associations
 * (browse folder)</li>
 * <li>Update properties and content of nodes.</li>
 * <li>Delete nodes. If the archive store is enabled, the node is not deleted
 * but moved from its current node to the archive node store; nodes in the
 * archive store can then be restored or purged.</li>
 * </ul>
 * 
 * @author Jean Marie Pascal
 */
public interface DocumentFolderService
{
    /**
     * Lists all immediate child nodes of the given context folder. </br> By
     * default, this list contains a maximum of 50 elements. </br> Use
     * {@link #getChildren(Folder, ListingContext)} to change this behaviour.
     * 
     * @param folder : context folder
     * @return Returns a list of the immediate child nodes of the given folder.
     * @see #getChildren(Folder, ListingContext)
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public List<Node> getChildren(Folder folder) throws AlfrescoServiceException;

    /**
     * Lists immediate child nodes of the given context folder.
     * 
     * @param folder : context folder
     * @param listingContext : Listing context that define the behaviour of
     *            paging results
     *            {@link org.alfresco.mobile.android.api.model.ListingContext
     *            ListingContext}
     * @return Returns a paged list of the immediate child nodes of the given
     *         folder.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public PagingResult<Node> getChildren(Folder folder, ListingContext listingContext) throws AlfrescoServiceException;

    /**
     * Gets the node object stored at the specified path.
     * 
     * @param path : path from the root folder.
     * @return Returns the node object stored at the specified path.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public Node getChildByPath(String path) throws AlfrescoServiceException;

    /**
     * Gets the node object stored at the relative specified path from the
     * folder object.
     * 
     * @param folder : context folder
     * @param relativePath : relative path from the root folder.
     * @return Returns the node object stored at the given path relative from
     *         the given folder.
     */
    public Node getChildByPath(Folder folder, String relativePath) throws AlfrescoServiceException;

    /**
     * @param identifier
     * @return Returns the node object with the specified identifier.
     */
    public Node getNodeByIdentifier(String identifier) throws AlfrescoServiceException;

    /**
     * Lists all immediate child documents of the given context node </br>Note:
     * this could be a long list
     * 
     * @param folder : context folder
     * @return Returns a list of all immediate child documents of the given
     *         folder.
     * @see #getDocuments(Folder, ListingContext)
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public List<Document> getDocuments(Folder folder) throws AlfrescoServiceException;

    /**
     * Lists all immediate child documents of the given context folder.
     * 
     * @param folder : context folder
     * @param listingContext : defines the behaviour of paging results
     *            {@link org.alfresco.mobile.android.api.model.ListingContext
     *            ListingContext}
     * @return Returns a paged list of all immediate child documents of the
     *         given folder.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public PagingResult<Document> getDocuments(Folder folder, ListingContext listingContext)
            throws AlfrescoServiceException;

    /**
     * Lists all immediate child folders of the given context folder.
     * 
     * @param folder : Parent Folder
     * @return Returns a list of all immediate child folders of the given
     *         folder.
     * @see #getFolders(Folder, ListingContext)
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public List<Folder> getFolders(Folder folder) throws AlfrescoServiceException;

    /**
     * Lists all immediate child folders of the given context folder.
     * 
     * @param folder : Parent Folder
     * @param listingContext : : defines the behaviour of paging results
     *            {@link org.alfresco.mobile.android.api.model.ListingContext
     *            ListingContext}
     * @return Returns a paged list of all immediate child folders of the given
     *         folder.
     * @see #getFolders(Folder)
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public PagingResult<Folder> getFolders(Folder folder, ListingContext listingContext)
            throws AlfrescoServiceException;

    /**
     * @return Returns the root folder of the repository.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public Folder getRootFolder() throws AlfrescoServiceException;

    /**
     * Gets the direct parent folder object.
     * 
     * @param node : Node object (Folder or Document).
     * @return Returns the parent folder object of the given node, null if the
     *         node does not have a parent.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public Folder getParentFolder(Node node) throws AlfrescoServiceException;

    /**
     * Creates a folder object in the specified location with an optional set of
     * properties.
     * 
     * @param folder : Parent Folder
     * @param folderName : Name of the future folder
     * @param properties : Map of properties to apply to the new folder
     * @return eturns the newly created folder
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public Folder createFolder(Folder folder, String folderName, Map<String, Serializable> properties)
            throws AlfrescoServiceException;

    /**
     * Creates a document object in the specified location with an optional set
     * of properties. The content for the node is taken from the provided file.
     * A the file parameter is not provided the document is created without
     * content. Depending on sessionSettings, it can launch metadata extraction
     * and thumbnail generation after creation.
     * 
     * @param folder: Future parent folder of a new document
     * @param documentName
     * @param properties : (Optional) list of property values that must be
     *            applied
     * @param file : (Optional) ContentFile that contains data stream or file
     * @return the newly created document object.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public Document createDocument(Folder folder, String documentName, Map<String, Serializable> properties,
            ContentFile file) throws AlfrescoServiceException;

    /**
     * Deletes the specified node.
     * 
     * @param node : Node object (Folder or Document).
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public void deleteNode(Node node) throws AlfrescoServiceException;

    /**
     * Updates the properties of the specified node. Can accept Alfresco Content
     * Model Properties id or cmis properties id.
     * 
     * @param node : Node to update
     * @param properties : Properties to update.
     * @return Returns Newly update node.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public Node updateProperties(Node node, Map<String, Serializable> properties) throws AlfrescoServiceException;

    /**
     * Updates the content on the given document using the provided local file.
     * 
     * @param document : Document object
     * @param file : File that is going to replace document content
     * @return newly updated Document.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public Document updateContent(Document document, ContentFile file) throws AlfrescoServiceException;

    /**
     * Downloads the content for the given document.
     * 
     * @param document : Document object
     * @return the contentFile representation that contains file informations +
     *         inputStream of the content.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public ContentFile getContent(Document document) throws AlfrescoServiceException;

    public ContentStream getContentStream(Document document) throws AlfrescoServiceException;

    /**
     * @param node
     * @return Returns a Permissions object representing the allowed actions for
     *         the current user on the given node.
     */
    public Permissions getPermissions(Node node) throws AlfrescoServiceException;

    /**
     * Represent the unique identifier for thumbnail rendition.
     * 
     * @see #getRendition(Node, String)
     */
    public static final String RENDITION_THUMBNAIL = "doclib";

    /**
     * Retrieve a specific type of Rendition for the specified identifier.
     * 
     * @param node : Node (Document in general)
     * @param type : : Type of rendition available
     * @return Returns a ContentFile object representing a rendition of the
     *         given node.
     * @throws AlfrescoServiceException : if network or internal problems occur
     *             during the process.
     */
    public ContentFile getRendition(Node node, String type) throws AlfrescoServiceException;

    public ContentStream getRenditionStream(Node node, String type) throws AlfrescoServiceException;

}