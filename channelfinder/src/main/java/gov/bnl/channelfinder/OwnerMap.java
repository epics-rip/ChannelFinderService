/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.channelfinder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.ws.rs.core.Response;

/**
 *
 * @author rlange
 */
public class OwnerMap {

    private static ThreadLocal<OwnerMap> instance = new ThreadLocal<OwnerMap>() {

        @Override
        protected OwnerMap initialValue() {
            return new OwnerMap();
        }
    };
    private Map<String, String> db_cowner = new HashMap<String, String>();
    private Map<String, String> db_powner = new HashMap<String, String>();
    private Map<String, String> pl_cowner = new HashMap<String, String>();
    private Map<String, String> pl_powner = new HashMap<String, String>();

    /**
     *
     */
    public OwnerMap() {
    }

    /**
     * Gets the (thread local) instance of the OwnerMap.
     * @return instance (thread local)
     */
    public static OwnerMap getInstance() {
        return instance.get();
    }

    private void loadNewDbChannelMap(Collection<String> names) throws CFException {
        FindEntitiesQuery eq = FindEntitiesQuery.createFindChannelNamesQuery(names);
        try {
            fillMap(db_cowner, eq.executeQuery(DbConnection.getInstance().getConnection()));
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while loading channel name map", e);
        }
    }

    private void loadNewDbPropertyMap(Collection<String> names) throws CFException {
        FindEntitiesQuery eq = FindEntitiesQuery.createFindPropertyNamesQuery(names);
        try {
            fillMap(db_powner, eq.executeQuery(DbConnection.getInstance().getConnection()));
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while loading property name map", e);
        }
    }

    private void fillMap(Map<String, String> map, ResultSet rs) throws CFException, SQLException {
        map.clear();
        while (rs.next()) {
            String name = rs.getString("name");
            String owner = rs.getString("owner");
            if (map.get(name) != null && !map.get(name).equals(owner)) {
                throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Inconsistent ownership in database for " + name);
            }
            map.put(name, owner);
        }
    }

    /**
     * Loads new database owner maps for channels and properties/tags in the specified XmlChannels
     * collection.
     *
     * @param data channels to create the db owner maps for
     * @throws CFException wrapping an SQLException
     */
    public void loadMapsFromDbFor(XmlChannels data) throws CFException {
        List<String> cnames = new ArrayList<String>();
        List<String> pnames = new ArrayList<String>();
        for (XmlChannel c : data.getChannels()) {
            cnames.add(c.getName());
            for (XmlProperty p : c.getXmlProperties()) {
                pnames.add(p.getName());
            }
            for (XmlTag t : c.getXmlTags()) {
                pnames.add(t.getName());
            }
        }
        loadNewDbChannelMap(cnames);
        loadNewDbPropertyMap(pnames);
    }

    /**
     * Loads a new channel owner map for the single specified channel.
     *
     * @param name channel to create owner map for
     * @throws CFException wrapping an SQLException
     */
    public void loadMapFromDbForChannel(String name) throws CFException {
        loadNewDbChannelMap(Collections.singleton(name));
        db_powner.clear();
    }

    /**
     * Loads a new property owner map for the single specified property.
     *
     * @param name property/tag name to create owner map for
     * @throws CFException wrapping an SQLException
     */
    public void loadMapFromDbForProperty(String name) throws CFException {
        loadNewDbPropertyMap(Collections.singleton(name));
        db_cowner.clear();
    }

    /**
     * Load the payload name map with the payload <tt>data</tt>.
     *
     * @param data XmlChannels collection to load
     * @throws CFException on owner mismatch
     */
    public void loadMapsFromPayloadFor(XmlChannels data) throws CFException {
        pl_cowner.clear();
        pl_powner.clear();
        for (XmlChannel c : data.getChannels()) {
            if (pl_cowner.get(c.getName()) == null) {
                pl_cowner.put(c.getName(), c.getOwner());
            } else {
                throw new CFException(Response.Status.BAD_REQUEST,
                        "Payload contains multiple instances of channel " + c.getName());
            }
            for (XmlProperty p : c.getXmlProperties()) {
                if (pl_powner.get(p.getName()) == null) {
                    pl_powner.put(p.getName(), p.getOwner());
                } else {
                    if (!p.getOwner().equals(pl_powner.get(p.getName()))) {
                        throw new CFException(Response.Status.BAD_REQUEST,
                                "Inconsistent payload owner for property " + p.getName());
                    }
                }
            }
            for (XmlTag t : c.getXmlTags()) {
                if (pl_powner.get(t.getName()) == null) {
                    pl_powner.put(t.getName(), t.getOwner());
                } else {
                    if (!t.getOwner().equals(pl_powner.get(t.getName()))) {
                        throw new CFException(Response.Status.BAD_REQUEST,
                                "Inconsistent payload owner for tag " + t.getName());
                    }
                }
            }
        }
    }

    /**
     * Load the database and payload name maps for/from the payload <tt>data</tt>.
     *
     * @param data XmlChannel collection to load
     * @throws CFException on owner mismatch
     */
    public void loadMapsFor(XmlChannels data) throws CFException {
        loadMapsFromDbFor(data);
        loadMapsFromPayloadFor(data);
    }

    public boolean checkDbAndPayloadOwnersMatch() throws CFException {
        for (String n : pl_cowner.keySet()) {
            if (db_cowner.containsKey(n) && !pl_cowner.get(n).equals(db_cowner.get(n))) {
                throw new CFException(Response.Status.BAD_REQUEST,
                        "Database and payload owner for channel " + n + " do not match");
            }
        }
        for (String n : pl_powner.keySet()) {
            if (db_powner.containsKey(n) && !pl_powner.get(n).equals(db_powner.get(n))) {
                throw new CFException(Response.Status.BAD_REQUEST,
                        "Database and payload owner for property/tag " + n + " do not match");
            }
        }
        return true;
    }

    /**
     * Returns the (database) owner of the specified property or tag, or,
     * if tag is new, the owner from the specified XmlChannel <tt>data</tt>.
     *
     * @param name of tag/property to look up
     * @param data XmlChannel data to use for new tag/property
     * @return owner of specified <tt>name</tt>, null if undefined
     */
    public String enforcedPropertyOwner(String name, XmlChannels data) {
        String owner = db_powner.get(name);
        if (owner != null) {
            return owner;
        }
        for (XmlChannel c : data.getChannels()) {
            for (XmlProperty p : c.getXmlProperties()) {
                if (name.equals(p.getName())) {
                    return p.getOwner();
                }
            }
            for (XmlTag t : c.getXmlTags()) {
                if (name.equals(t.getName())) {
                    return t.getOwner();
                }
            }
        }
        return null;
    }

    /**
     * Returns the (database) owner of the specified property or tag.
     *
     * @param name of tag/property to look up
     * @return owner of specified <tt>name</tt>, null if not in db
     */
    public String getDbPropertyOwner(String name) {
        return db_powner.get(name);
    }

    /**
     * Returns the (database) owner of the specified channel.
     *
     * @param name of channel to look up
     * @return owner of specified <tt>name</tt>, null if not in db
     */
    public String getDbChannelOwner(String name) {
        return db_cowner.get(name);
    }
}
