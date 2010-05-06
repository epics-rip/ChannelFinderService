/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package gov.bnl.channelfinder;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import java.security.Principal;

/**
 * Group Manager: Manages user -> group map
 * @author rlange
 */
public class UserManager {
    private static UserManager instance = new UserManager();
    private ThreadLocal<Principal> user = new ThreadLocal<Principal>();
    private static Multimap<String, String> groups = ArrayListMultimap.create();

    static {
        /* FIXME: For the time being: hardcoded */
        groups.put("taggy", "group1");
        groups.put("proppy", "group1");
        groups.put("proppy", "group2");
        groups.put("channy", "group1");
        groups.put("channy", "group2");
        groups.put("channy", "group3");
    }

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

    /**
     * Sets the (thread local) user principal to be used in further calls.
     *
     * @param user principal
     */
    public void setUser(Principal user) {
        this.user.set(user);
    }

    /**
     * Checks if the user is in the specified <tt>group</tt>.
     *
     * @param group name of the group to check membership
     * @return true if user is a member of <tt>group</tt>
     */
    public boolean isUserInGroup(String group) {
        return groups.containsEntry(getUserName(), group);
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
