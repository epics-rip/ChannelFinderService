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

/**
 *
 * @author rlange
 */
public class DbOwnerMap {

    private static ThreadLocal<DbOwnerMap> instance = new ThreadLocal<DbOwnerMap>() {

        @Override
        protected DbOwnerMap initialValue() {
            return new DbOwnerMap();
        }
    };
    private Map<String, String> cowner = new HashMap<String, String>();
    private Map<String, String> powner = new HashMap<String, String>();

    /**
     *
     */
    public DbOwnerMap() {
    }

    /**
     * Gets the (thread local) instance of the DbOwnerMap.
     * @return instance (thread local)
     */
    public static DbOwnerMap getInstance() {
        return instance.get();
    }

    private void loadNewChannelMap(Collection<String> names) throws SQLException {
        FindEntitiesQuery eq = FindEntitiesQuery.createFindChannelNamesQuery(names);
        fillMap(cowner, eq.executeQuery(DbConnection.getInstance().getConnection()));
    }

    private void loadNewPropertyMap(Collection<String> names) throws SQLException {
        FindEntitiesQuery eq = FindEntitiesQuery.createFindPropertyNamesQuery(names);
        fillMap(powner, eq.executeQuery(DbConnection.getInstance().getConnection()));
    }

    private void fillMap(Map<String, String> map, ResultSet rs) throws SQLException {
        map.clear();
        while (rs.next()) {
            map.put(rs.getString("name"), rs.getString("owner"));
        }
    }

    /**
     * Loads new owner maps for channels and properties/tags in the specified XmlChannels
     * collection.
     *
     * @param data channels to create the owner maps for
     * @throws SQLException
     */
    public void loadMapsFor(XmlChannels data) throws SQLException {
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
        loadNewChannelMap(cnames);
        loadNewPropertyMap(pnames);
    }

    /**
     * Loads new owner maps for channels and properties/tags in the specified XmlChannel.
     *
     * @param data channel to create the owner maps for
     * @throws SQLException
     */
    public void loadMapsFor(XmlChannel data) throws SQLException {
        loadMapsFor(new XmlChannels(data));
    }

    /**
     * Loads a new channel owner map for the single specified channel.
     *
     * @param name channel to create owner map for
     * @throws SQLException
     */
    public void loadMapForChannel(String name) throws SQLException {
        loadNewChannelMap(Collections.singleton(name));
        powner.clear();
    }

    /**
     * Loads a new property owner map for the single specified property.
     *
     * @param name
     * @throws SQLException
     */
    public void loadMapForProperty(String name) throws SQLException {
        loadNewPropertyMap(Collections.singleton(name));
        cowner.clear();
    }

    /**
     * Checks if the owners of channels, properties, and tags in the specified
     * XmlChannels collection match the values in the current owner maps.
     *
     * @param data XmlChannels collection to check ownership for
     * @return <tt>true</tt> if owners match
     */
    public boolean matchesOwnersIn(XmlChannels data) {
        for (XmlChannel c : data.getChannels()) {
            if (cowner.get(c.getName()) != null
                    && !c.getOwner().equals(cowner.get(c.getName()))) {
                return false;
            }
            for (XmlProperty p : c.getXmlProperties()) {
                if (powner.get(p.getName()) != null
                        && !p.getOwner().equals(powner.get(p.getName()))) {
                    return false;
                }
            }
            for (XmlTag t : c.getXmlTags()) {
                if (powner.get(t.getName()) != null
                        && !t.getOwner().equals(powner.get(t.getName()))) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks if the owners of channel, properties, and tags in the specified
     * XmlChannel match the values in the current owner maps.
     *
     * @param data XmlChannel to check ownership for
     * @return <tt>true</tt> if owners match
     */
    public boolean matchesOwnersIn(XmlChannel data) {
        return matchesOwnersIn(new XmlChannels(data));
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
        String owner = powner.get(name);
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
    public String getPropertyOwner(String name) {
        return powner.get(name);
    }

    /**
     * Returns the (database) owner of the specified channel.
     *
     * @param name of channel to look up
     * @return owner of specified <tt>name</tt>, null if not in db
     */
    public String getChannelOwner(String name) {
        return cowner.get(name);
    }

}
