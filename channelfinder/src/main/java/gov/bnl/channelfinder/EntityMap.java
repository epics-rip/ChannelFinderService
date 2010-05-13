/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.bnl.channelfinder;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
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
public class EntityMap {

    private static ThreadLocal<EntityMap> instance = new ThreadLocal<EntityMap>() {

        @Override
        protected EntityMap initialValue() {
            return new EntityMap();
        }
    };
    private Map<String, List<String>> db_cowner = new HashMap<String, List<String>>();
    private Map<String, List<String>> db_powner = new HashMap<String, List<String>>();
    private Map<String, List<String>> pl_cowner = new HashMap<String, List<String>>();
    private Map<String, List<String>> pl_powner = new HashMap<String, List<String>>();

    /**
     *
     */
    public EntityMap() {
    }

    /**
     * Gets the (thread local) instance of the EntityMap.
     * @return instance (thread local)
     */
    public static EntityMap getInstance() {
        return instance.get();
    }

    private void loadNewDbChannelMap(Collection<String> names) throws CFException {
        FindEntitiesQuery eq = FindEntitiesQuery.createFindChannelNamesQuery(names);
        try {
            fillMap(db_cowner, eq.executeQuery(DbConnection.getInstance().getConnection()), false);
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while loading channel name map", e);
        }
    }

    private void loadNewDbPropertyMap(Collection<String> names) throws CFException {
        FindEntitiesQuery eq = FindEntitiesQuery.createFindPropertyNamesQuery(names);
        try {
            fillMap(db_powner, eq.executeQuery(DbConnection.getInstance().getConnection()), true);
        } catch (SQLException e) {
            throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                    "SQL Exception while loading property name map", e);
        }
    }

    private void fillMap(Map<String, List<String>> map, ResultSet rs, boolean ignoreCase) throws CFException, SQLException {
        map.clear();
        while (rs.next()) {
            String name = rs.getString("name");
            String key = name;
            if (ignoreCase) {
                key = name.toLowerCase();
            }
            String owner = rs.getString("owner").toLowerCase();
            if (map.containsKey(key) && !map.get(key).get(1).equals(owner)) {
                throw new CFException(Response.Status.INTERNAL_SERVER_ERROR,
                        "Inconsistent ownership in database for " + name);
            }
            map.put(key, Arrays.asList(name, owner));
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
            if (pl_cowner.containsKey(c.getName())) {
                throw new CFException(Response.Status.BAD_REQUEST,
                        "Payload contains multiple instances of channel " + c.getName());
            } else {
                pl_cowner.put(c.getName(), Arrays.asList(c.getName(), c.getOwner()));
            }
            for (XmlProperty p : c.getXmlProperties()) {
                String key = p.getName().toLowerCase();
                if (pl_powner.containsKey(key)) {
                    if (!p.getOwner().equals(pl_powner.get(key).get(1))) {
                        throw new CFException(Response.Status.BAD_REQUEST,
                                "Inconsistent payload owner for property " + p.getName());
                    }
                } else {
                    pl_powner.put(key, Arrays.asList(p.getName(), p.getOwner()));
                }
            }
            for (XmlTag t : c.getXmlTags()) {
                String key = t.getName().toLowerCase();
                if (pl_powner.containsKey(key)) {
                    if (!t.getOwner().equals(pl_powner.get(key).get(1))) {
                        throw new CFException(Response.Status.BAD_REQUEST,
                                "Inconsistent payload owner for tag " + t.getName());
                    }
                } else {
                    pl_powner.put(key, Arrays.asList(t.getName(), t.getOwner()));
                }
            }
        }
    }

    /**
     * Load the payload name map with the payload <tt>data</tt>.
     *
     * @param data XmlTag to load
     * @throws CFException on owner mismatch
     */
    public void loadMapsFromPayloadFor(XmlTag data) throws CFException {
        pl_cowner.clear();
        pl_powner.clear();
        String key = data.getName().toLowerCase();
        if (pl_powner.containsKey(key)) {
            if (!data.getOwner().equals(pl_powner.get(key).get(1))) {
                throw new CFException(Response.Status.BAD_REQUEST,
                        "Inconsistent payload owner for tag " + data.getName());
            }
        } else {
            pl_powner.put(key, Arrays.asList(data.getName(), data.getOwner()));
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
            if (db_cowner.containsKey(n) && !pl_cowner.get(n).get(1).equals(db_cowner.get(n).get(1))) {
                throw new CFException(Response.Status.BAD_REQUEST,
                        "Database and payload owner for channel "
                        + pl_cowner.get(n).get(0) + " do not match");
            }
        }
        for (String n : pl_powner.keySet()) {
            if (db_powner.containsKey(n) && !pl_powner.get(n).get(1).equals(db_powner.get(n).get(1))) {
                throw new CFException(Response.Status.BAD_REQUEST,
                        "Database and payload owner for property/tag "
                        + pl_powner.get(n).get(0) + " do not match");
            }
        }
        return true;
    }

    /**
     * Returns the (database) owner of the specified property or tag, or,
     * if tag is new, the owner from the specified XmlChannels <tt>data</tt>.
     *
     * @param name of tag/property to look up
     * @param data XmlChannel data to use for new tag/property
     * @return owner of specified <tt>name</tt>, null if undefined
     */
    public String enforcedPropertyOwner(String name, XmlChannels data) {
        String key = name.toLowerCase();
        List<String> val = db_powner.get(key);
        if (val != null) {
            return val.get(1);
        }
        for (XmlChannel c : data.getChannels()) {
            for (XmlProperty p : c.getXmlProperties()) {
                if (key.equals(p.getName().toLowerCase())) {
                    return p.getOwner();
                }
            }
            for (XmlTag t : c.getXmlTags()) {
                if (key.equals(t.getName().toLowerCase())) {
                    return t.getOwner();
                }
            }
        }
        return null;
    }

    /**
     * Returns the (database) name of the specified property or tag (in correct capitalization),
     * or, if tag is new, the name from the specified XmlChannels <tt>data</tt>.
     *
     * @param name of tag/property to look up
     * @param data XmlChannel data to use for new tag/property
     * @return owner of specified <tt>name</tt>, null if undefined
     */
    public String enforcedPropertyName(String name, XmlChannels data) {
        String key = name.toLowerCase();
        List<String> val = db_powner.get(key);
        if (val != null) {
            return val.get(0);
        }
        for (XmlChannel c : data.getChannels()) {
            for (XmlProperty p : c.getXmlProperties()) {
                if (key.equals(p.getName().toLowerCase())) {
                    return p.getName();
                }
            }
            for (XmlTag t : c.getXmlTags()) {
                if (key.equals(t.getName().toLowerCase())) {
                    return t.getName();
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
        List<String> val = db_powner.get(name.toLowerCase());
        if (val != null) {
            return val.get(1);
        } else {
            return null;
        }
    }

    /**
     * Returns the name of the specified property or tag in database capitalization.
     *
     * @param name of tag/property to look up
     * @return specified <tt>name</tt> in database capitalization, null if not in db
     */
    public String enforceDbPropertyName(String name) {
        List<String> val = db_powner.get(name.toLowerCase());
        if (val != null) {
            return val.get(0);
        } else {
            return name;
        }
    }

    /**
     * Returns the (database) owner of the specified channel.
     *
     * @param name of channel to look up
     * @return owner of specified <tt>name</tt>, null if not in db
     */
    public String getDbChannelOwner(String name) {
        List<String> val = db_cowner.get(name);
        if (val != null) {
            return val.get(1);
        } else {
            return null;
        }
    }
    
    /**
     * Enforces the database capitalization in the property and tag names of <tt>data</tt>. 
     *
     * @param data XmlChannels collection to work on
     */
    public void enforceDbCapitalization(XmlChannels data) {
        for (XmlChannel c : data.getChannels()) {
            for (XmlProperty p : c.getXmlProperties()) {
                if (db_powner.containsKey(p.getName().toLowerCase())) {
                    p.setName(db_powner.get(p.getName().toLowerCase()).get(0));
                }
            }
            for (XmlTag t : c.getXmlTags()) {
                if (db_powner.containsKey(t.getName().toLowerCase())) {
                    t.setName(db_powner.get(t.getName().toLowerCase()).get(0));
                }
            }
        }
    }
}
