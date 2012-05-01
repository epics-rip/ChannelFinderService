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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 * JDBC query to find database ids for properties/tags.
 * 
 * @author Ralph Lange <Ralph.Lange@helmholtz-berlin.de>
 */
public class FindPropertyIdsQuery {

    private List<String> names = new ArrayList();

    private FindPropertyIdsQuery(XmlChannel data) {
        for (XmlProperty prop : data.getXmlProperties().getProperties()) {
            names.add(prop.getName().toLowerCase());
        }
        for (XmlTag tag : data.getXmlTags().getTags()) {
            names.add(tag.getName().toLowerCase());
        }
    }

    private FindPropertyIdsQuery(String name) {
        names.add(name.toLowerCase());
    }

    /**
     * Creates and executes a JDBC based query returning ids and names.
     *
     * @param con connection to use
     * @return result set with columns named <tt>id</tt>, <tt>name</tt> or null if no result
     * @throws CFException wrapping an SQLException
     */
    private ResultSet executeQuery(Connection con) throws CFException {
        PreparedStatement ps;
        List<String> name_params = new ArrayList<String>();

        StringBuilder query = new StringBuilder("SELECT id, name FROM property");
        for (String name : names) {
            if (!query.toString().endsWith(" OR")) {
                query.append(" WHERE");
            }
            query.append(" LOWER(name) = ? OR");
            name_params.add(name);
        }
        if (query.toString().endsWith(" OR")) {
            query.delete(query.length() - 3, query.length());
        }

        try {
            ps = con.prepareStatement(query.toString());
            int i = 1;
            for (String s : name_params) {
                ps.setString(i++, s);
            }
            return ps.executeQuery();
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception during property id query", e);
        }
    }

    /**
     * Find the property names and ids for a specified channel.
     *
     * @param data the XmlChannel for which to generate the map
     * @return map of property names and ids
     * @throws CFException wrapping an SQLException
     */
    public static Map<String, Integer> getPropertyIdMap(XmlChannel data) throws CFException {
        Map<String, Integer> result = new HashMap<String, Integer>();
        FindPropertyIdsQuery q = new FindPropertyIdsQuery(data);
        try {
            ResultSet rs = q.executeQuery(DbConnection.getInstance().getConnection());
            if (rs != null) {
                while (rs.next()) {
                    result.put(rs.getString("name"), rs.getInt("id"));
                }
            }
            return result;
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception scanning result of property id request", e);
        }
    }

    /**
     * Find the id of a single tag or property.
     *
     * @param name the name to find
     * @return id
     * @throws CFException wrapping an SQLException
     */
    public static Long getPropertyId(String name) throws CFException {
        FindPropertyIdsQuery q = new FindPropertyIdsQuery(name);
        try {
            ResultSet rs = q.executeQuery(DbConnection.getInstance().getConnection());
            if (rs != null && rs.first()) {
                return rs.getLong("id");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception scanning result of single property id request", e);
        }
    }
}
