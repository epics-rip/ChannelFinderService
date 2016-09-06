/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010-2011 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms and conditions.
 */
package gov.bnl.channelfinder;

import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.jacc.PolicyContext;
import javax.security.jacc.PolicyContextException;


/**
 * Owner (group) membership management: JACC connection and binding.
 *
 * @author beryman
 */
public class JACCUserManager extends UserManager {

    private static final String SUBJECT_HANDLER_KEY = "javax.security.auth.Subject.container";

    @Override
    protected Set<String> getGroups(Principal user) {

        try {
            Subject s = (Subject) PolicyContext.getContext(SUBJECT_HANDLER_KEY);
            Principal principals[] = (s == null ? new Principal[0] : s.getPrincipals().toArray(new Principal[0]));

            HashSet<String> roleSet = new HashSet();
            for(Principal principal :principals){
                roleSet.add(principal.getName());
            }
            return roleSet;

//            Do we want to look at only Web Role Permissions?  Checking againist all principals for now.
//            CodeSource cs = new CodeSource(null, (java.security.cert.Certificate[]) null);
//            ProtectionDomain pd = new ProtectionDomain(cs, null, null, principals);
//
//            Policy policy = Policy.getPolicy();
//            PermissionCollection pc = policy.getPermissions(pd);
//            pc.implies(new WebRoleRefPermission(null, null));
//
//            HashSet roleSet = null;
//
//            Enumeration e = pc.elements();
//            while (e.hasMoreElements()) {
//                Object p = e.nextElement();
//                if (p instanceof WebRoleRefPermission) {
//                    String roleRef = ((WebRoleRefPermission) p).getActions();
//                    if (roleSet == null) {
//                        roleSet = new HashSet();
//                    }
//                    roleSet.add(((WebRoleRefPermission) p).getActions());
//                }
//            }
//            if (roleSet != null) {
//                roleSet.toArray(new String[0]);
//            }
//            return roleSet;
//
        } catch (PolicyContextException ex) {
            Logger.getLogger(JACCUserManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
