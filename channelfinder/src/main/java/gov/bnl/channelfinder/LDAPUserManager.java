package gov.bnl.channelfinder;
/**
 * #%L
 * ChannelFinder Directory Service
 * %%
 * Copyright (C) 2010 - 2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * %%
 * Copyright (C) 2010 - 2012 Brookhaven National Laboratory
 * All rights reserved. Use is subject to license terms.
 * #L%
 */

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
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
 * Owner (group) membership management: LDAP connection and binding.
 *
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */
public class LDAPUserManager extends UserManager {
    private ThreadLocal<DirContext> ctx = new ThreadLocal<DirContext>();
    private static final String ldapResourceName = "channelfinder/ldapManagerConnection";

    /**
     * LDAP field name for the member UID
     */
    @Resource(name="ldapGroupMemberField") protected String memberUidField = "memberUid";

    /**
     * LDAP field name for the group name in group entries
     */
    @Resource(name="ldapGroupTargetField") protected String groupTargetField = "cn";


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

    @Override
    protected Set<String> getGroups(Principal user) {
        try {
            Set<String> groups = new HashSet<String>();
            DirContext dirctx = getJndiContext();
            SearchControls ctrls = new SearchControls();
            ctrls.setSearchScope(SearchControls.SUBTREE_SCOPE);

            String searchfilter = "(" + memberUidField + "=" + user.getName() + ")";
            NamingEnumeration<SearchResult> result = dirctx.search("", searchfilter, ctrls);

            while (result.hasMore()) {
                Attribute att = result.next().getAttributes().get(groupTargetField);
                if (att != null) {
                    groups.add((String)att.get());
                }
            }
            return groups;
        } catch (Exception e) {
                throw new IllegalStateException("Error while retrieving group information for user '"
                        + user.getName() + "'", e);
        }
    }
}
