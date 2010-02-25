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
    private Multimap<String, String> groups = ArrayListMultimap.create();

    public UserManager() {
        /* FIXME: For the time being: hardcoded */
        groups.put("taggy", "group1");
        groups.put("proppy", "group1");
        groups.put("proppy", "group2");
        groups.put("channy", "group1");
        groups.put("channy", "group2");
        groups.put("channy", "group3");
    }

    public static UserManager getInstance() {
        return instance;
    }

    public void setUser(Principal user) {
        this.user.set(user);
    }

    public boolean isUserInGroup(String group) {
        return groups.containsEntry(getUserName(), group);
    }

    public String getUserName() {
        return user.get().getName();
    }
}
