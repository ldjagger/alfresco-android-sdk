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
package org.alfresco.mobile.android.api.session.impl;

import java.io.Serializable;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.alfresco.mobile.android.api.exceptions.AlfrescoConnectionException;
import org.alfresco.mobile.android.api.exceptions.ErrorCodeRegistry;
import org.alfresco.mobile.android.api.model.Folder;
import org.alfresco.mobile.android.api.model.ListingContext;
import org.alfresco.mobile.android.api.model.RepositoryInfo;
import org.alfresco.mobile.android.api.services.ServiceRegistry;
import org.alfresco.mobile.android.api.session.AlfrescoSession;
import org.alfresco.mobile.android.api.session.CloudSession;
import org.alfresco.mobile.android.api.session.RepositorySession;
import org.alfresco.mobile.android.api.session.authentication.AuthenticationProvider;
import org.alfresco.mobile.android.api.session.authentication.impl.PassthruAuthenticationProviderImpl;
import org.alfresco.mobile.android.api.utils.CloudUrlRegistry;
import org.alfresco.mobile.android.api.utils.OnPremiseUrlRegistry;
import org.alfresco.mobile.android.api.utils.messages.Messagesl18n;
import org.apache.chemistry.opencmis.client.api.Session;
import org.apache.chemistry.opencmis.client.api.SessionFactory;
import org.apache.chemistry.opencmis.client.runtime.SessionFactoryImpl;
import org.apache.chemistry.opencmis.commons.SessionParameter;
import org.apache.chemistry.opencmis.commons.enums.BindingType;
import org.apache.chemistry.opencmis.commons.exceptions.CmisPermissionDeniedException;

/**
 * AlfrescoSession is the base class for all connection to a repository.
 * 
 * @author Jean Marie Pascal
 */
public abstract class AbstractAlfrescoSessionImpl implements AlfrescoSession
{

    public static final String CACHE_FOLDER_PATH = "/sdcard/Android/data/org.alfresco.mobile.android.sdk/cache";

    protected String baseUrl;

    private String userIdentifier;

    private String password;

    /** Root Folder for the specific session. */
    protected Folder rootNode;

    /** Service Registry for all features available with this repository. */
    protected ServiceRegistry services;

    /** Repository Informations to the specific session. */
    protected RepositoryInfo repositoryInfo;

    /** Cmis Session that comes from OpenCMIS binding. */
    protected Session cmisSession;

    /** Authentication Provider. */
    protected AuthenticationProvider authenticator;

    protected org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider passThruAuthenticator;

    // ////////////////////////
    // Constructor
    // ///////////////////////
    protected void initSettings(String url, Map<String, Serializable> settings)
    {
        String tmpuser = null, tmppassword = null, tmpurl = url;

        // Basic Authentication Case.
        // Retrieve credentials informations
        if (settings.containsKey(USER))
        {
            tmpuser = (String) settings.get(USER);
        }
        if (settings.containsKey(PASSWORD))
        {
            tmppassword = (String) settings.get(PASSWORD);
        }
        if (settings.containsKey(BASE_URL))
        {
            tmpurl = (String) settings.get(BASE_URL);
        }

        initSettings(tmpurl, tmpuser, tmppassword, settings);
    }

    protected void initSettings(String url, String username, String password, Map<String, Serializable> settings)
    {
        if (url == null) { throw new IllegalArgumentException(String.format(
                Messagesl18n.getString("ErrorCodeRegistry.GENERAL_INVALID_ARG_NULL"), "url")); }

        Map<String, Serializable> tmpSettings = settings;
        if (tmpSettings == null)
        {
            tmpSettings = new HashMap<String, Serializable>(1);
        }

        if (username != null && username.length() > 0)
        {
            tmpSettings.put(SessionParameter.USER, username);
            this.userIdentifier = username;
        }

        if (password != null && password.length() > 0)
        {
            tmpSettings.put(SessionParameter.PASSWORD, password);
            this.password = password;
        }

        this.baseUrl = url;
        tmpSettings.put(BASE_URL, url);

        // default cache storage
        if (!tmpSettings.containsKey(CACHE_FOLDER))
        {
            tmpSettings.put(CACHE_FOLDER, CACHE_FOLDER_PATH);
        }

        if (!tmpSettings.containsKey(SessionParameter.AUTHENTICATION_PROVIDER_CLASS))
        {
            tmpSettings.put(SessionParameter.AUTHENTICATION_PROVIDER_CLASS,
                    "org.alfresco.mobile.android.api.session.authentication.impl.PassthruAuthenticationProviderImpl");
        }

        if (!tmpSettings.containsKey(AUTHENTICATOR_CLASSNAME))
        {
            tmpSettings.put(AUTHENTICATOR_CLASSNAME,
                    "org.alfresco.mobile.android.api.session.authentication.impl.BasicAuthenticationProviderImpl");
        }
        userParameters = tmpSettings;
    }

    // ////////////////////////
    // Parameters
    // ///////////////////////
    protected Map<String, Serializable> userParameters;

    private Map<String, String> sessionParameters = new HashMap<String, String>();

    private ListingContext lc;

    /**
     * Allow to add some extra parameters as settings to modify behaviour of the
     * session. Settings provide session configuration parameters e.g. cache
     * settings, default paging values, custom Authentication Providers,
     * ordering etc. <br>
     * 
     * @param key : All Public parameters are available at
     *            {@link org.alfresco.mobile.android.api.session.SessionSettings
     *            SessionSettings}
     * @param value : Value associated to the specific setting value.
     */
    public void addParameter(String key, Serializable value)
    {
        userParameters.put(key, value);
    }

    public void addParameters(Map<String, Serializable> parameters)
    {
        userParameters.putAll(parameters);
    }

    public boolean hasParameter(String key)
    {
        return userParameters.containsKey(key);
    }

    public Serializable getParameter(String key)
    {
        return userParameters.get(key);
    }

    public void removeParameter(String key)
    {
        userParameters.remove(key);
    }

    public List<String> getParameterKeys()
    {
        return new ArrayList<String>(userParameters.keySet());
    }

    /**
     * VIM : This method allows to create the CMIS Session Parameters based on
     * Public Session Parameters provided. Depending on session type, it creates
     * the bunch of parameters OpenCMIS can understand.
     * 
     * @return OpenCMIS Map of Session Parameters.
     */
    protected Map<String, String> retrieveSessionParameters()
    {
        init();
        return sessionParameters;
    }

    public void init()
    {
        int type = BINDING_TYPE_ALFRESCO_CMIS;
        if (hasParameter(BINDING_TYPE))
        {
            type = (Integer) getParameter(BINDING_TYPE);
        }
        else
        {
            if (this instanceof CloudSession)
            {
                type = BINDING_TYPE_ALFRESCO_CLOUD;
            }
            else if (this instanceof RepositorySession)
            {
                type = BINDING_TYPE_ALFRESCO_CMIS;
            }
        }

        switch (type)
        {
            case BINDING_TYPE_CMIS:
                createCmisSettings();
                break;
            case BINDING_TYPE_ALFRESCO_CMIS:
                createAlfrescoCmisSettings();
                break;
            case BINDING_TYPE_ALFRESCO_CLOUD:
                createCloudCmisSettings();
                break;
            default:
                createAlfrescoCmisSettings();
                break;
        }

        lc = createListingContext();
    }

    private void createCmisSettings()
    {
        // Credentials
        sessionParameters.put(SessionParameter.USER, userIdentifier);
        sessionParameters.put(SessionParameter.PASSWORD, password);
        sessionParameters.put(SessionParameter.BINDING_TYPE, BindingType.ATOMPUB.value());
        // sessionParameters.put(SessionParameter.CLIENT_COMPRESSION, "true");

        // connection settings
        addParameterIfExist(BINDING_URL, SessionParameter.ATOMPUB_URL);
        addParameterIfExist(BASE_URL, BASE_URL);
        addParameterIfExist(RepositorySession.REPOSITORY_ID, SessionParameter.REPOSITORY_ID);
        addParameterIfExist(SessionParameter.CONNECT_TIMEOUT, SessionParameter.CONNECT_TIMEOUT);
        addParameterIfExist(SessionParameter.READ_TIMEOUT, SessionParameter.READ_TIMEOUT);
        addParameterIfExist(SessionParameter.PROXY_USER, SessionParameter.PROXY_USER);
        addParameterIfExist(SessionParameter.PROXY_PASSWORD, SessionParameter.PROXY_PASSWORD);
        addParameterIfExist(SessionParameter.AUTHENTICATION_PROVIDER_CLASS,
                SessionParameter.AUTHENTICATION_PROVIDER_CLASS);
        addParameterIfExist(AlfrescoSession.AUTHENTICATOR_CLASSNAME, AlfrescoSession.AUTHENTICATOR_CLASSNAME);
    }

    private void addParameterIfExist(String keySettings, String keyParameters)
    {
        if (hasParameter(keySettings))
        {
            sessionParameters.put(keyParameters, (String) getParameter(keySettings));
        }
    }

    private void createAlfrescoCmisSettings()
    {
        createCmisSettings();

        // Binding with Alfresco Webscript CMIS implementation
        if (hasParameter(BASE_URL) && !sessionParameters.containsKey(SessionParameter.ATOMPUB_URL))
        {
            sessionParameters.put(SessionParameter.ATOMPUB_URL,
                    ((String) getParameter(BASE_URL)).concat(OnPremiseUrlRegistry.BINDING_CMIS));
        }

        // Object Factory
        sessionParameters.put(SessionParameter.OBJECT_FACTORY_CLASS,
                "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");
        addParameterIfExist(AlfrescoSession.ONPREMISE_SERVICES_CLASSNAME, AlfrescoSession.ONPREMISE_SERVICES_CLASSNAME);
    }

    private void createCloudCmisSettings()
    {
        createCmisSettings();

        if (!sessionParameters.containsKey(BINDING_URL))
        {
            sessionParameters.put(
                    SessionParameter.ATOMPUB_URL,
                    ((String) getParameter(BASE_URL)).concat(CloudUrlRegistry.BINDING_NETWORK_CMISATOM).replace(
                            CloudUrlRegistry.VARIABLE_NETWORKID, (String) getParameter(CloudSession.CLOUD_NETWORK_ID)));
        }

        // Object Factory
        sessionParameters.put(SessionParameter.OBJECT_FACTORY_CLASS,
                "org.alfresco.cmis.client.impl.AlfrescoObjectFactoryImpl");

        addParameterIfExist(AlfrescoSession.CLOUD_SERVICES_CLASSNAME, AlfrescoSession.CLOUD_SERVICES_CLASSNAME);
    }

    private ListingContext createListingContext()
    {
        lc = new ListingContext();

        if (hasParameter(AlfrescoSession.LISTING_MAX_ITEMS))
        {
            lc.setMaxItems((Integer) getParameter(AlfrescoSession.LISTING_MAX_ITEMS));
        }

        return lc;
    }

    protected Session createSession(SessionFactory sessionFactory, AuthenticationProvider authenticator,
            Map<String, String> param)
    {
        try
        {
            if (param.get(SessionParameter.REPOSITORY_ID) != null)
            {
                return ((SessionFactoryImpl) sessionFactory).createSession(param, null,
                        new PassthruAuthenticationProviderImpl(authenticator), null);
            }
            else
            {
                return ((SessionFactoryImpl) sessionFactory)
                        .getRepositories(param, null, new PassthruAuthenticationProviderImpl(authenticator), null)
                        .get(0).createSession();
            }
        }
        catch (CmisPermissionDeniedException e)
        {
            throw new AlfrescoConnectionException(ErrorCodeRegistry.SESSION_UNAUTHORIZED, e);
        }
        catch (Exception e)
        {
            throw new AlfrescoConnectionException(ErrorCodeRegistry.SESSION_GENERIC, e);
        }
    }

    protected Session createSession(SessionFactory sessionFactory, Map<String, String> param)
    {
        try
        {
            if (param.get(SessionParameter.REPOSITORY_ID) != null)
            {
                return sessionFactory.createSession(param);
            }
            else
            {
                return sessionFactory.getRepositories(param).get(0).createSession();
            }
        }
        catch (CmisPermissionDeniedException e)
        {
            throw new AlfrescoConnectionException(ErrorCodeRegistry.SESSION_UNAUTHORIZED, e);
        }
        catch (Exception e)
        {
            throw new AlfrescoConnectionException(ErrorCodeRegistry.SESSION_GENERIC, e);
        }
    }

    @Override
    public void disconnect()
    {
        this.authenticator = null;
        this.cmisSession = null;
        this.repositoryInfo = null;
        this.rootNode = null;
        this.services = null;
    }

    // ///////////////////////////////////////////////
    // BINDINGS
    // ///////////////////////////////////////////////
    /**
     * Define the specific binding type associated with which we want to create
     * the session.
     */
    private static final String BINDING_TYPE = "org.alfresco.mobile.internal.binding";

    /** CMIS Binding type. */
    private static final int BINDING_TYPE_CMIS = 1;

    /** Alfresco CMIS Binding type. */
    private static final int BINDING_TYPE_ALFRESCO_CMIS = 2;

    /** Alfresco Public API Binding type. */
    @SuppressWarnings("unused")
    private static final int BINDING_TYPE_ALFRESCO_PUBLIC_API = 3;

    /** Alfresco Cloud API Binding type. */
    private static final int BINDING_TYPE_ALFRESCO_CLOUD = 4;

    private static final String BINDING_URL = "org.alfresco.mobile.binding.internal.url";

    private static final String BASE_URL = "org.alfresco.mobile.binding.internal.baseurl";

    protected static final String USER = "org.alfresco.mobile.internal.credential.user";

    protected static final String PASSWORD = "org.alfresco.mobile.internal.credential.password";

    // ////////////////////////
    // SHORTCUTS
    // ///////////////////////
    /**
     * @return Returns
     *         {@link org.alfresco.mobile.android.api.model.RepositoryInfo
     *         RepositoryInformations} object representing the repository the
     *         session is connected to.
     */
    public RepositoryInfo getRepositoryInfo()
    {
        return repositoryInfo;
    }

    /**
     * @return Returns repository unique identifier.
     */
    public String getRepositoryIdentifier()
    {
        return repositoryInfo.getIdentifier();
    }

    /**
     * @return Returns the root folder of the repository.
     */
    public Folder getRootFolder()
    {
        return rootNode;
    }

    /**
     * @return Returns the base URL associated to the repository </br> For
     *         example : <i>http://hostname:port/alfresco</i>
     */
    public String getBaseUrl()
    {
        return baseUrl;
    }

    /**
     * @return Returns the username with which we have created the session.
     */
    public String getPersonIdentifier()
    {
        return userIdentifier;
    }

    /**
     * @return Returns the authenticationProvider associated to the session.
     */
    public AuthenticationProvider getAuthenticationProvider()
    {
        return authenticator;
    }

    public org.apache.chemistry.opencmis.commons.spi.AuthenticationProvider getPassthruAuthenticationProvider()
    {
        return passThruAuthenticator;
    }

    /**
     * @return Returns the current default listing parameters for filtering,
     *         paging and caching.
     */
    public ListingContext getDefaultListingContext()
    {
        return lc;
    }

    /**
     * Return all services available with this repository.
     * 
     * @return Service Provider associated to the session.
     */
    public ServiceRegistry getServiceRegistry()
    {
        return services;
    }

    /**
     * Direct access to the underlying cmis session. Use with caution.
     * 
     * @return the cmisSession
     */
    public Session getCmisSession()
    {
        return cmisSession;
    }

    /**
     * Extension Point to use a specific serviceRegistry.
     * 
     * @param className : ClassName of the serviceRegistry to implement
     * @return an instance of serviceRegistry.
     */
    protected ServiceRegistry createServiceRegistry(String className)
    {
        ServiceRegistry s = null;
        try
        {
            Class<?> c = Class.forName(className);
            Constructor<?> t = c.getDeclaredConstructor(AlfrescoSession.class);
            s = (ServiceRegistry) t.newInstance(this);
        }
        catch (Exception e)
        {
            throw new AlfrescoConnectionException(ErrorCodeRegistry.SESSION_SERVICEREGISTRY, e);
        }
        return s;
    }
}
