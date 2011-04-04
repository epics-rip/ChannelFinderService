/*
 * Copyright (c) 2010 Brookhaven National Laboratory
 * Copyright (c) 2010-2011 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms and conditions.
 */
package gov.bnl.channelfinder;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Uses Unix command 'id' to determine the group membership.
 *
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */
public class IDUserManager extends UserManager {
    
    public static String readInputStreamAsString(InputStream in) throws IOException {
        BufferedInputStream bis = new BufferedInputStream(in);
        ByteArrayOutputStream buf = new ByteArrayOutputStream();
        int result = bis.read();
        while (result != -1) {
            byte b = (byte) result;
            buf.write(b);
            result = bis.read();
        }
        return buf.toString();
    }

    @Override
    protected Set<String> getGroups(Principal user) {
        try {
            Set<String> groups = new HashSet<String>();
            ProcessBuilder pb = new ProcessBuilder("id", user.getName());
            Process proc = pb.start();
            String output = readInputStreamAsString(proc.getInputStream());
            if (output.indexOf("groups") == -1) {
                return Collections.emptySet();
            }
            output = output.substring(output.indexOf("groups"));
            
            Pattern pattern = Pattern.compile("\\((.*?)\\)");
            Matcher match = pattern.matcher(output);
            while (match.find()) {
                groups.add(match.group(1));
            }
            return groups;
        } catch (Exception e) {
            throw new IllegalStateException("Error while retrieving group information for user '"
                    + user.getName() + "'", e);
        }
    }
}
