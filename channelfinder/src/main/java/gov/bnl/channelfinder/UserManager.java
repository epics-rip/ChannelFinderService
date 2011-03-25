/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * Subject to license terms and conditions.
 */

package gov.bnl.channelfinder;

import java.security.Principal;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Owner (group) membership management.
 *
 * @author Ralph Lange <Ralph.Lange@bessy.de>
 * @author Gabriele Carcassi <carcassi@bnl.gov>
 */
public abstract class UserManager {
    
    private static final Logger log = Logger.getLogger(UserManager.class.getName());
    
    private ThreadLocal<Principal> user = new ThreadLocal<Principal>();
    private ThreadLocal<Boolean> hasAdminRole = new ThreadLocal<Boolean>();
    private ThreadLocal<Collection<String>> groups = new ThreadLocal<Collection<String>>();
    
    private static final String userManager = "gov.bnl.channelfinder.IDUserManager";
    private static UserManager instance;
    
    static {
        try {
            instance = (UserManager) Class.forName(userManager).newInstance();
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Could not instance userManager " + userManager, ex);
        }
    }

    protected UserManager() {
    }

    /**
     * Returns the (singleton) instance of UserManager.
     *
     * @return instance of UserManager
     */
    public static UserManager getInstance() {
        if (instance == null)
            throw new IllegalStateException("UserManager could not be instanced");
        return instance;
    }
    
    /**
     * Retrieves the group membership for the given principal.
     * 
     * @param user a user
     * @return the group names
     */
    protected abstract Set<String> getGroups(Principal user);

    /**
     * Sets the (thread local) user principal to be used in further calls
     * and retrieves the group information.
     *
     * @param user principal
     * @param isAdmin flag: true = user has Admin role
     */
    public void setUser(Principal user, boolean isAdmin) {
        this.user.set(user);
        this.hasAdminRole.set(isAdmin);
        this.groups.set(getGroups(user));
    }

    /**
     * Checks if the user is in the specified <tt>group</tt>.
     *
     * @param group name of the group to check membership
     * @return true if user is a member of <tt>group</tt>
     */
    public boolean userIsInGroup(String group) {
        return group == null ? true : groups.get().contains(group);
    }

    /**
     * Checks if the user has admin role.
     *
     * @return true if user is a member of <tt>group</tt>
     */
    public boolean userHasAdminRole() {
        return hasAdminRole.get();
    }

    /**
     * Returns the current user's name.
     *
     * @return name of current user
     */
    public String getUserName() {
        return user.get().getName();
    }
}
