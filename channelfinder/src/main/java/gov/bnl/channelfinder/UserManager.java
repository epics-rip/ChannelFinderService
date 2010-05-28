/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import java.security.Principal;
import java.util.Collection;
import java.util.HashSet;
import javax.annotation.Resource;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

/**
 * Group Manager: Manages user -> group map
 * @author rlange
 */
public class UserManager {
    private static UserManager instance = new UserManager();
    private ThreadLocal<Principal> user = new ThreadLocal<Principal>();
    private ThreadLocal<Boolean> hasAdminRole = new ThreadLocal<Boolean>();
    private ThreadLocal<Collection<String>> groups = new ThreadLocal<Collection<String>>();
    private ThreadLocal<DirContext> ctx = new ThreadLocal<DirContext>();
    private static final String ldapResourceName = "channelfinderGroups";

    @Resource(name="ldapGroupMemberField") protected String memberUidField = "memberUid";
    @Resource(name="ldapGroupTargetField") protected String groupTargetField = "cn";

    private UserManager() {
    }

    /**
     * Returns the (singleton) instance of UserManager.
     *
     * @return instance of UserManager
     */
    public static UserManager getInstance() {
        return instance;
    }

    private void clearGroups() {
        if (this.groups.get() == null) {
            this.groups.set(new HashSet<String>());
        } else {
            this.groups.get().clear();
        }
    }

    private DirContext getJndiContext() {
        DirContext dirctx = this.ctx.get();
        if (dirctx == null) {
            try {
                Context initCtx = new InitialContext();
                dirctx = (DirContext) initCtx.lookup(ldapResourceName);
                this.ctx.set(dirctx);
            } catch (NamingException e ) {
                throw new IllegalStateException("Cannot find JNDI LDAP resource '"
                        + ldapResourceName + "'", e);
            }
        }
        return dirctx;
    }

    /**
     * Sets the (thread local) user principal to be used in further calls, initializes
     * the LDAP directory context, if necessary, then queries LDAP to find the groups
     * mapping for the given user.
     *
     * @param user principal
     */
    public void setUser(Principal user, boolean isAdmin) {
        this.user.set(user);
        this.hasAdminRole.set(isAdmin);
        clearGroups();
        DirContext dirctx = getJndiContext();
        try {
            SearchControls ctrls = new SearchControls();
            ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            String searchfilter = "(" + memberUidField + "=" + this.user.get().getName() + ")";
            NamingEnumeration<SearchResult> result = dirctx.search("", searchfilter, ctrls);

            while (result.hasMore()) {
                Attribute att = result.next().getAttributes().get(groupTargetField);
                if (att != null) {
                    groups.get().add((String)att.get());
                }
            }
        } catch (Exception e) {
                throw new IllegalStateException("Error while retrieving group information for user '"
                        + this.user.get().getName() + "'", e);
        }
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
     * @param group name of the group to check membership
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
