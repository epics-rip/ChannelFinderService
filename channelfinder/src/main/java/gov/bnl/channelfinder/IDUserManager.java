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

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.Principal;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.naming.InitialContext;

/**
 * Uses Unix command 'id' to determine the group membership.
 *
 * @author Ralph Lange {@literal <ralph.lange@gmx.de>}
 */
public class IDUserManager extends UserManager {
    
    private static final Logger log = Logger.getLogger(IDUserManager.class.getName());
    
    private static final String defaultCommand = "id";
    private static final String command;
    
    static {
        String newCommand = defaultCommand;
        try {
            newCommand = (String) new InitialContext().lookup("channelfinder/idManagerCommand");
            log.log(Level.CONFIG, "Found channelfinder/idManagerCommand: {0}", newCommand);
        } catch (Exception ex) {
            log.log(Level.CONFIG, "Using default channelfinder/idManagerCommand: {0}", newCommand);
        }
        command = newCommand;
    }
    
    public static String readInputStreamAsString(InputStream in) throws IOException {
        if (in == null)
            throw new NullPointerException();
        try {
            BufferedInputStream bis = new BufferedInputStream(in);
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            try {
                int result = bis.read();
                while (result != -1) {
                    byte b = (byte) result;
                    buf.write(b);
                    result = bis.read();
                }
                return buf.toString();
            } finally {
                bis.close();
                buf.close();
            }
        } finally {
            in.close();
        }
    }

    @Override
    protected Set<String> getGroups(Principal user) {
        try {
            Set<String> groups = new HashSet<String>();
            ProcessBuilder pb = new ProcessBuilder(command, user.getName());
            Process proc = pb.start();
            try {
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
            } finally {
                proc.destroy();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Error while retrieving group information for user '"
                    + user.getName() + "'", e);
        }
    }
}
