/**
 * Copyright (C) 2010-2012 Brookhaven National Laboratory
 * Copyright (C) 2010-2012 Helmholtz-Zentrum Berlin für Materialien und Energie GmbH
 * All rights reserved. Use is subject to license terms.
 */
package gov.bnl.channelfinder;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;

/**
 * JDBC query to find properties/tags.
 * 
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */
public class ListPropertiesQuery {
    private String name;

    private ListPropertiesQuery() {
    }

    private ListPropertiesQuery(String name) {
        this.name = name;
    }

    /**
     * Creates and executes a JDBC based query returning properties or tags.
     *
     * @param con connection to use
     * @return result set with columns named <tt>id</tt>, <tt>name</tt> or null if no result
     * @throws CFException wrapping an SQLException
     */
    private ResultSet executeQuery(Connection con, boolean isTagQuery) throws CFException {
        PreparedStatement ps;
        List<String> name_params = new ArrayList<String>();

        StringBuilder query = new StringBuilder("SELECT id, name, owner FROM property ");

        if (isTagQuery) {
            query.append("WHERE is_tag = TRUE");
        } else {
            query.append("WHERE is_tag = FALSE");
        }
        if (name != null) {
            query.append(" AND name = ?");
        }
        try {
            ps = con.prepareStatement(query.toString());
            if (name != null) {
                ps.setString(1, name);
            }
            return ps.executeQuery();
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception during property/tag list query", e);
        }
    }

    /**
     * Returns the list of properties in the database.
     *
     * @return XmlProperties
     * @throws CFException wrapping an SQLException
     */
    public static XmlProperties getProperties() throws CFException {
        XmlProperties result = new XmlProperties();
        ListPropertiesQuery q = new ListPropertiesQuery();
        try {
            ResultSet rs = q.executeQuery(DbConnection.getInstance().getConnection(), false);
            if (rs != null) {
                while (rs.next()) {
                    result.addXmlProperty(new XmlProperty(rs.getString("name"), rs.getString("owner")));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception scanning result of property list request", e);
        }
    }

    /**
     * Finds a property in the database by name.
     *
     * @return XmlProperty
     * @throws CFException wrapping an SQLException
     */
    public static XmlProperty findProperty(String name) throws CFException {
        XmlProperty result = null;
        ListPropertiesQuery q = new ListPropertiesQuery(name);
        try {
            ResultSet rs = q.executeQuery(DbConnection.getInstance().getConnection(), false);
            if (rs != null) {
                while (rs.next()) {
                    result = new XmlProperty(rs.getString("name"), rs.getString("owner"));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception scanning result of find property request", e);
        }
    }

    /**
     * Returns the list of tags in the database.
     *
     * @return XmlTags
     * @throws CFException wrapping an SQLException
     */
    public static XmlTags getTags() throws CFException {
        XmlTags result = new XmlTags();
        ListPropertiesQuery q = new ListPropertiesQuery();
        try {
            ResultSet rs = q.executeQuery(DbConnection.getInstance().getConnection(), true);
            if (rs != null) {
                while (rs.next()) {
                    result.addXmlTag(new XmlTag(rs.getString("name"), rs.getString("owner")));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception scanning result of tag list request", e);
        }
    }

    /**
     * Finds a tag in the database by name.
     *
     * @return XmlTag
     * @throws CFException wrapping an SQLException
     */
    public static XmlTag findTag(String name) throws CFException {
        XmlTag result = null;
        ListPropertiesQuery q = new ListPropertiesQuery(name);
        try {
            ResultSet rs = q.executeQuery(DbConnection.getInstance().getConnection(), true);
            if (rs != null) {
                while (rs.next()) {
                    result = new XmlTag(rs.getString("name"), rs.getString("owner"));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception scanning result of find tag request", e);
        }
    }
}
