/*
 * ====================================================================
 * Copyright (c) 2004-2012 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.core.wc;

import java.io.File;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import org.tmatesoft.svn.core.ISVNCanceller;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepository;
import org.tmatesoft.svn.core.internal.wc.DefaultSVNOptions;
import org.tmatesoft.svn.core.io.ISVNConnectionListener;
import org.tmatesoft.svn.core.io.ISVNSession;
import org.tmatesoft.svn.core.io.ISVNTunnelProvider;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.util.ISVNDebugLog;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * The <b>DefaultSVNRepositoryPool</b> class is a default implementation of
 * the <b>ISVNRepositoryPool</b> interface.
 *
 * <p>
 * It creates <b>SVNRepository</b> objects that may be stored in a common
 * pool and reused later. The objects common pool may be shared by different
 * threads, but each thread can retrieve only those objects, that have been
 * created within that thread. So, <b>DefaultSVNRepositoryPool</b> is thread-safe.
 * An objects pool may be global during runtime, or it may be private - one separate
 * pool per one <b>DefaultSVNRepositoryPool</b> object. Also there's a possibility to
 * have a <b>DefaultSVNRepositoryPool</b> object with the pool feature
 * disabled (<b>SVNRepository</b> objects instantiated by such a creator are never
 * cached).
 *
 * <p>
 * <b>DefaultSVNRepositoryPool</b> caches one <b>SVNRepository</b> object per one url
 * protocol (per one thread), that is the number of protocols used equals to
 * the number of objects cached per one thread (if all objects are created as reusable).
 *
 * <p>
 * Also <b>DefaultSVNRepositoryPool</b> is able to create <b>SVNRepository</b> objects
 * that use a single socket connection (i.e. don't close a connection after every repository
 * access operation but reuse a single one).
 *
 * @version 1.3
 * @author  TMate Software Ltd.
 * @since   1.2
 */
public class DefaultSVNRepositoryPool implements ISVNRepositoryPool, ISVNSession, ISVNConnectionListener {

    /**
     * Defines a common shared objects pool. All objects that will be
     * created by different threads will be stored in this common pool.
     *
     * @deprecated
     */
    public static final int RUNTIME_POOL = 1;

    /**
     * Defines a private pool. All objects that will be created by
     * different threads will be stored only within this pool object.
     * This allows to have more than one separate pools.
     *
     * @deprecated
     */
    public static final int INSTANCE_POOL = 2;

    /**
     * Defines a without-pool configuration. Objects that are created
     * by this <b>DefaultSVNRepositoryPool</b> object are not cached,
     * the pool feature is disabled.
     *
     * @deprecated
     */
    public static final int NO_POOL = 4;

    private static final long DEFAULT_IDLE_TIMEOUT = 60*1000;

    private static volatile ScheduledExecutorService ourTimer;
    private static volatile int ourInstanceCount;

    private ISVNAuthenticationManager myAuthManager;
    private ISVNTunnelProvider myTunnelProvider;
    private ISVNDebugLog myDebugLog;
    private ISVNCanceller myCanceller;
    private Map<String, SVNRepository> myPool;
    private long myTimeout;
    private Map<SVNRepository, Long> myInactiveRepositories = new HashMap<SVNRepository, Long>();
    private ScheduledExecutorService myTimer;

    private boolean myIsKeepConnection;

    private ScheduledFuture<?> myScheduledTimeoutTask;
    private File mySpoolLocation;

    /**
     * Constructs a <b>DefaultSVNRepositoryPool</b> instance
     * that represents {@link #RUNTIME_POOL} objects pool.
     * <b>SVNRepository</b> objects created by this instance will
     * use a single socket connection.
     *
     * <p/>
     * This constructor is identical to
     * <code>DefaultSVNRepositoryPool(authManager, tunnelProvider, DEFAULT_IDLE_TIMEOUT, true)</code>.
     *
     * @param authManager      an authentication driver
     * @param tunnelProvider   a tunnel provider
     */
    public DefaultSVNRepositoryPool(ISVNAuthenticationManager authManager, ISVNTunnelProvider tunnelProvider) {
        this(authManager, tunnelProvider, DEFAULT_IDLE_TIMEOUT, true);
    }

    /**
     * Constructs a <b>DefaultSVNRepositoryPool</b> instance
     * that represents {@link #RUNTIME_POOL} objects pool.
     * <b>SVNRepository</b> objects created by this instance will
     * use a single socket connection.
     *
     * @param authManager      an authentication driver
     * @param tunnelProvider   a tunnel provider
     * @param timeout          inactivity timeout after which open connections should be closed
     * @param keepConnection   whether to keep connection open
     */
    public DefaultSVNRepositoryPool(ISVNAuthenticationManager authManager, ISVNTunnelProvider tunnelProvider,
            long timeout, boolean keepConnection) {
        myAuthManager = authManager;
        myTunnelProvider = tunnelProvider;
        myDebugLog = SVNDebugLog.getDefaultLog();
        myTimeout = timeout > 0 ? timeout : DEFAULT_IDLE_TIMEOUT;
        myIsKeepConnection = keepConnection;
        myTimeout = timeout;

        synchronized (DefaultSVNRepositoryPool.class) {
            if (ourTimer == null) {
                ourTimer = createExecutor();
            }
            if (myIsKeepConnection) {
                myTimer = ourTimer;
                try {
                    myScheduledTimeoutTask = myTimer.scheduleWithFixedDelay(new TimeoutTask(), 10, 10, TimeUnit.SECONDS);
                } catch (IllegalStateException e) {
                    // Timer already cancelled error.
                    SVNDebugLog.getDefaultLog().logError(SVNLogType.DEFAULT, e);

                    ourTimer = createExecutor();
                    myTimer = ourTimer;
                    myScheduledTimeoutTask = myTimer.scheduleWithFixedDelay(new TimeoutTask(), 10, 10, TimeUnit.SECONDS);
                }
            }
            ourInstanceCount++;
        }
    }

    private ScheduledExecutorService createExecutor() {
        return Executors.newSingleThreadScheduledExecutor(new DaemonThreadFactory());
    }

    private static final class DaemonThreadFactory implements ThreadFactory {
        public Thread newThread(Runnable r) {
            final Thread t = new Thread(r);
            t.setDaemon(true);
            return t;
        }
    }

    /**
     * Constructs a <b>DefaultSVNRepositoryPool</b> instance.
     *
     * @param authManager         an authentication driver
     * @param tunnelProvider      a tunnel provider
     * @param keepConnections     if <span class="javakeyword">true</span>
     *                            then <b>SVNRepository</b> objects will keep
     *                            a single connection for accessing a repository,
     *                            if <span class="javakeyword">false</span> - open
     *                            a new connection per each repository access operation
     * @param poolMode            a mode of this object represented by
     *                            one of the constant fields of <b>DefaultSVNRepositoryPool</b>
     * @deprecated
     */
    public DefaultSVNRepositoryPool(ISVNAuthenticationManager authManager, ISVNTunnelProvider tunnelProvider, boolean keepConnections, int poolMode) {
        this(authManager, tunnelProvider);
    }

    /**
     * Creates a new <b>SVNRepository</b> driver object.
     * if <code>mayReuse</code> is <span class="javakeyword">true</span>
     * and the mode of this <b>DefaultSVNRepositoryPool</b> object is not
     * {@link #NO_POOL} then first tries to find the <b>SVNRepository</b>
     * object in the pool for the given protocol. If the object is not found,
     * creates a new one for that protocol, caches it in the pool and returns
     * back.
     *
     * <p>
     * <b>NOTE:</b> be careful when simultaneously using several <b>SVNRepository</b>
     * drivers for the same protocol - since there can be only one driver object in
     * the pool per a protocol, creating two objects for the same protocol
     * with <code>mayReuse</code> set to <span class="javakeyword">true</span>,
     * actually returns the same single object stored in the thread pool.
     *
     * @param url             a repository location for which a driver
     *                        is to be created
     * @param mayReuse        if <span class="javakeyword">true</span> then
     *                        <b>SVNRepository</b> object is reusable
     * @return                a new <b>SVNRepository</b> driver object
     * @throws SVNException
     * @see                   org.tmatesoft.svn.core.io.SVNRepository
     *
     */
    public synchronized SVNRepository createRepository(SVNURL url, boolean mayReuse) throws SVNException {
        synchronized (DefaultSVNRepositoryPool.class) {
            if (myIsKeepConnection && myTimer == null && ourTimer != null) {
                myTimer = ourTimer;
                myScheduledTimeoutTask = myTimer.scheduleWithFixedDelay(new TimeoutTask(), 10, 10, TimeUnit.SECONDS);
            }
        }

        SVNRepository repos = null;
        Map<String, SVNRepository> pool = getPool();
        if (!mayReuse || pool == null) {
            repos = SVNRepositoryFactory.create(url, this);
            repos.setAuthenticationManager(myAuthManager);
            repos.setTunnelProvider(myTunnelProvider);
            repos.setDebugLog(myDebugLog);
            repos.setCanceller(myCanceller);
            setOptionalSpoolLocation(repos, myTunnelProvider);
            return repos;
        }

        repos = (SVNRepository) pool.get(url.getProtocol());
        if (repos != null) {
            repos.setLocation(url, false);
        } else {
            repos = SVNRepositoryFactory.create(url, this);
            // add listener.
            if (myIsKeepConnection) {
                repos.addConnectionListener(this);
            }
            pool.put(url.getProtocol(), repos);
        }
        repos.setAuthenticationManager(myAuthManager);
        repos.setTunnelProvider(myTunnelProvider);
        repos.setDebugLog(myDebugLog);
        repos.setCanceller(myCanceller);
        setOptionalSpoolLocation(repos, myTunnelProvider);
        return repos;
    }

    public void setSpoolLocation(File location) {
        mySpoolLocation = location;
    }

    public File getSpoolLocation() {
        return mySpoolLocation;
    }

    private void setOptionalSpoolLocation(SVNRepository repos, ISVNTunnelProvider options) {
        if (!(repos instanceof DAVRepository)) {
            return;
        }
        final File poolSpoolLocation = getSpoolLocation();
        final File spoolLocation;
        if (options instanceof DefaultSVNOptions) {
            final File configSpoolLocation = ((DefaultSVNOptions) options).getHttpSpoolDirectory();
            spoolLocation = poolSpoolLocation != null ? poolSpoolLocation : configSpoolLocation;
        } else {
            spoolLocation = poolSpoolLocation;
        }

        if (spoolLocation != null) {
            ((DAVRepository) repos).setSpoolLocation(spoolLocation);
        }
    }

    /**
     * Sets the given authentication instance to this pool and to all {@link SVNRepository} objects
     * stored in this pool.
     *
     * @param authManager    authentication manager instance
     */
    public void setAuthenticationManager(ISVNAuthenticationManager authManager) {
        myAuthManager = authManager;
        Map<String, SVNRepository> pool = getPool();
        for (Iterator<String> protocols = pool.keySet().iterator(); protocols.hasNext();) {
            String key = protocols.next();
            SVNRepository repository = (SVNRepository) pool.get(key);
            repository.setAuthenticationManager(myAuthManager);
        }
    }

    /**
     * Says if the given <b>SVNRepository</b> driver object should
     * keep a connection opened. If this object was created with
     * <code>keepConnections</code> set to <span class="javakeyword">true</span>
     * and if <code>repository</code> is not created for the
     * <span class="javastring">"svn+ssh"</span> protocol (since for this protocol there's
     * no extra need to keep a connection opened - it remains opened), this
     * method returns <span class="javakeyword">true</span>.
     *
     * @param  repository  an <b>SVNRepository</b> driver
     * @return             <span class="javakeyword">true</span> if
     *                     the driver should keep a connection
     */
    public boolean keepConnection(SVNRepository repository) {
        return myIsKeepConnection;
    }

    /**
     * Closes connections of cached <b>SVNRepository</b> objects.
     *
     * <p>
     * Actually, calls the {@link #dispose()} routine.
     *
     * @param shutdownAll if <span class="javakeyword">true</span> - closes
     *                    connections of all the cached objects, otherwise only
     *                    connections of those cached objects which owner threads
     *                    have already disposed
     * @see               SVNRepository
     */
    public synchronized void shutdownConnections(boolean shutdownAll) {
        dispose();
    }

    /**
     * Disposes this pool. Clears all inactive {@link SVNRepository} objects from this pool.
     *
     * @since 1.2.0
     */
    public void dispose() {
        synchronized (myInactiveRepositories) {
            myTimer = null;
        }
        shutdownInactiveRepositories(Long.MAX_VALUE);

        Map<String, SVNRepository> pool = getPool();
        for (Iterator<String> protocols = pool.keySet().iterator(); protocols.hasNext();) {
            String key = protocols.next();
            SVNRepository repository = pool.get(key);
            repository.closeSession();
        }
        myPool = null;

        synchronized (DefaultSVNRepositoryPool.class) {
            if (myScheduledTimeoutTask != null) {
                myScheduledTimeoutTask.cancel(false);
                myScheduledTimeoutTask = null;
            }
            ourInstanceCount--;
            if (ourInstanceCount <= 0) {
                ourInstanceCount = 0;
                shutdownTimer();
            }
        }
    }

    /**
     * Stops the daemon thread that checks whether there are any <code>SVNRepository</code> objects
     * expired.
     *
     * @see   #connectionClosed(SVNRepository)
     * @since 1.1.5
     */
    public static void shutdownTimer() {
        synchronized (DefaultSVNRepositoryPool.class) {
            if (ourTimer != null) {
                try {
                    ourTimer.shutdownNow();
                } catch (SecurityException se) {

                }
                ourTimer = null;
            }
        }
    }

    // no caching in this class
    /**
     * Does nothing.
     *
     * @param repository  an <b>SVNRepository</b> driver (to distinguish
     *                    that repository for which this message is actual)
     * @param revision    a revision number
     * @param message     the commit message for <code>revision</code>
     */
    public void saveCommitMessage(SVNRepository repository, long revision, String message) {
    }

    /**
     * Returns <span class="javakeyword">null</span>.
     *
     * @param repository  an <b>SVNRepository</b> driver (to distinguish
     *                    that repository for which a commit message is requested)
     * @param revision    a revision number
     * @return            the commit message for <code>revision</code>
     */
    public String getCommitMessage(SVNRepository repository, long revision) {
        return null;
    }

    /**
     * Returns <span class="javakeyword">false</span>.
     *
     * @param repository  an <b>SVNRepository</b> driver (to distinguish
     *                    that repository for which a commit message is requested)
     * @param revision    a revision number
     * @return            <span class="javakeyword">true</span> if the cache
     *                    has got a message for the given repository and revision,
     *                    <span class="javakeyword">false</span> otherwise
     */
    public boolean hasCommitMessage(SVNRepository repository, long revision) {
        return false;
    }

    /**
     * Places the specified <code>repository</code> into the pool of inactive <code>SVNRepository</code>
     * objects.
     *
     * <p/>
     * If this pool keeps connections open (refer to the <code>keepConnection</code> parameter of the
     * {@link #DefaultSVNRepositoryPool(ISVNAuthenticationManager, ISVNTunnelProvider, long, boolean) constructor}),
     * then each <code>SVNRepository</code> object which is passed to this method (what means it finished
     * the operation), must be reused in a period of time not greater than the timeout value. The timeout value
     * is either equal to the value passed to the {@link #DefaultSVNRepositoryPool(ISVNAuthenticationManager, ISVNTunnelProvider, long, boolean) constructor},
     * or it defaults to 60 seconds if no valid timeout value was provided. Otherwise the repository object will
     * be {@link SVNRepository#closeSession() closed}. Timeout checking occurs one time in 10 seconds. This
     * behavior - closing repository objects after timeout - can be changed by switching off the timer thread
     * via {@link #shutdownTimer()}.
     *
     * @param repository repository access object
     * @since 1.1.4
     */
    public void connectionClosed(final SVNRepository repository) {
        // start inactivity timer.
        synchronized (myInactiveRepositories) {
            myInactiveRepositories.put(repository, System.currentTimeMillis());
            // schedule timeout cleanup.
        }
    }

    /**
     * Removes the specified <code>repository</code> object from the pool of inactive <code>SVNRepository</code>
     * objects held by this object. This method is synchronized.
     *
     * @param repository repository access object to remove from the pool
     * @since 1.1.4
     */
    public void connectionOpened(SVNRepository repository) {
        synchronized (myInactiveRepositories) {
            myInactiveRepositories.remove(repository);
        }
    }

    /**
     * Sets a canceller to be used in all {@link SVNRepository} objects produced by this
     * pool.
     *
     * @param canceller caller's canceller
     * @since 1.1.4
     */
    public void setCanceller(ISVNCanceller canceller) {
        myCanceller = canceller;
        Map<String, SVNRepository> pool = getPool();
        for (Iterator<String> protocols = pool.keySet().iterator(); protocols.hasNext();) {
            String key = (String) protocols.next();
            SVNRepository repository = (SVNRepository) pool.get(key);
            repository.setCanceller(canceller);
        }
    }

    /**
     * Sets a debug logger to be used in all {@link SVNRepository} objects produced by this
     * pool.
     *
     * @param log debug logger
     * @since 1.1.4
     */
    public void setDebugLog(ISVNDebugLog log) {
        myDebugLog = log == null ? SVNDebugLog.getDefaultLog() : log;
        Map<String, SVNRepository> pool = getPool();
        for (Iterator<String> protocols = pool.keySet().iterator(); protocols.hasNext();) {
            String key = (String) protocols.next();
            SVNRepository repository = (SVNRepository) pool.get(key);
            repository.setDebugLog(myDebugLog);
        }
    }

    private long getTimeout() {
        return myTimeout;
    }

    private Map<String, SVNRepository> getPool() {
        if (myPool == null) {
            myPool = new HashMap<String, SVNRepository>();
        }
        return myPool;
    }


    private void shutdownInactiveRepositories(long currentTime) {
        synchronized (myInactiveRepositories) {
            for (Iterator<SVNRepository> repositories = myInactiveRepositories.keySet().iterator(); repositories.hasNext();) {
              SVNRepository repos = repositories.next();
              long time = ((Long) myInactiveRepositories.get(repos)).longValue();
              if (currentTime - time >= getTimeout()) {
                  repositories.remove();
                  repos.closeSession();
              }
            }
        }
    }

    private class TimeoutTask implements Runnable {
        public void run() {
            try {
                shutdownInactiveRepositories(System.currentTimeMillis());
            } catch (Throwable th) {
                SVNDebugLog.getDefaultLog().logSevere(SVNLogType.WC, th);
            }
        }
    }
}
