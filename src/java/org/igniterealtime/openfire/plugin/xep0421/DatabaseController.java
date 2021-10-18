package org.igniterealtime.openfire.plugin.xep0421;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.jivesoftware.database.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.JID;

public class DatabaseController {

    private static final Logger Log = LoggerFactory.getLogger(DatabaseController.class);

    private static final String GET_KEY_FROM_MUC = "SELECT _key FROM ofMucPrivateKeys WHERE _roomjid=?";
    private static final String INSERT_KEY_FROM_MUC = "INSERT INTO ofMucPrivateKeys (_roomjid,_key) VALUES (?,?)";
    private static final String DELETE_KEY_FROM_MUC = "DELETE FROM ofMucPrivateKeys where _roomjid=?";

    public DatabaseController() {

    }

    public String getKey(JID roomjid)
    {
        Connection con;
        try {
            con = DbConnectionManager.getConnection();
        } catch (SQLException e) {
            Log.error("Could not connect to database: "+e.getMessage(),e);
            return null;
        }

        ResultSet rs = null;
        PreparedStatement pstmt = null;

        try {

            pstmt = con.prepareStatement(GET_KEY_FROM_MUC);
            pstmt.setString(1, roomjid.toBareJID());
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getString(1);
            }
            else
            {
                return null;
            }
        } catch (SQLException sqle) {
            Log.error("Error while fetching the key: "+sqle.getMessage(), sqle);
            return null;
        } finally {
            DbConnectionManager.closeConnection(rs,pstmt,con);
        }
    }

    public boolean deleteKey(JID roomjid) {
        return deleteKey(roomjid,true);
    }

    private boolean deleteKey(JID roomjid,boolean writelog) {
        Connection con;
        try {
            con = DbConnectionManager.getConnection();
        } catch (SQLException e) {
            if (writelog)
            {
                Log.error("Could not connect to database: "+e.getMessage(),e);
            }
            return false;
        }

        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement(DELETE_KEY_FROM_MUC);
            pstmt.setString(1, roomjid.toBareJID());

            return pstmt.executeUpdate()>0||!writelog?true:false;
        } catch (SQLException sqle) {            
            Log.error("Could not delete the key for room: \"{}\"",roomjid.toBareJID(), sqle);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pstmt,con);
        }
    }
    
    public boolean insertKey(JID roomjid, String key) {
        deleteKey(roomjid,false);

        Connection con;
        try {
            con = DbConnectionManager.getConnection();
        } catch (SQLException e) {
            Log.error("Could not connect to database: "+e.getMessage(),e);
            return false;
        }

        PreparedStatement pstmt = null;

        try {
            pstmt = con.prepareStatement(INSERT_KEY_FROM_MUC);
            pstmt.setString(1, roomjid.toBareJID());
            pstmt.setString(2, key);

            return pstmt.executeUpdate()>0?true:false;
        } catch (SQLException sqle) {
            Log.error("Could not insert the key for room: \"{}\"",roomjid.toBareJID(), sqle);
            return false;
        } finally {
            DbConnectionManager.closeConnection(pstmt,con);
        }
    }
}
