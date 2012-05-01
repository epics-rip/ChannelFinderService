/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 * JDBC query to create a property/tag.
 *
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */
public class CreatePropertyQuery {

    private String name;
    private String owner;
    private boolean isTagQuery = false;

    private String getType() {
        if (isTagQuery) {
            return "tag";
        } else {
            return "property";
        }
    }

    private CreatePropertyQuery(String name, String owner, boolean isTagQuery) {
        this.name = name;
        this.owner = owner;
        this.isTagQuery = isTagQuery;
    }

    /**
     * Executes a JDBC based query to add properties/tags.
     *
     * @param con database connection to use
     * @throws CFException wrapping an SQLException
     */
    private void executeQuery(Connection con) throws CFException {
        List<List<String>> params = new ArrayList<List<String>>();
        PreparedStatement ps;

        // Insert property
        String query = "INSERT INTO property (name, owner, is_tag) VALUE (?, ?, ?)";
        try {
            ps = con.prepareStatement(query);
            ps.setString(1, name);
            ps.setString(2, owner);
            ps.setBoolean(3, isTagQuery);
            ps.execute();
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while adding " + getType() + " '" + name +"'", e);
        }
    }

    /**
     * Creates a property in the database.
     *
     * @param name name of property
     * @param owner owner of property
     * @throws CFException wrapping an SQLException
     */
    public static void createProperty(String name, String owner) throws CFException {
        CreatePropertyQuery q = new CreatePropertyQuery(name, owner, false);
        q.executeQuery(DbConnection.getInstance().getConnection());
    }

    /**
     * Creates a tag in the database.
     *
     * @param name name of tag
     * @param owner owner of tag
     * @throws CFException wrapping an SQLException
     */
    public static void createTag(String name, String owner) throws CFException {
        CreatePropertyQuery q = new CreatePropertyQuery(name, owner, true);
        q.executeQuery(DbConnection.getInstance().getConnection());
    }
}
