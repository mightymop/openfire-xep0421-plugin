package org.igniterealtime.openfire.plugin.xep0421;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.apache.commons.codec.binary.Hex;
import org.dom4j.Element;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.jivesoftware.openfire.muc.MUCOccupant;
import org.jivesoftware.openfire.muc.MUCRoom;
import org.jivesoftware.openfire.muc.MultiUserChatService;
import org.jivesoftware.openfire.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xmpp.packet.IQ;
import org.xmpp.packet.IQ.Type;
import org.xmpp.packet.JID;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;
import org.xmpp.packet.Presence;

public class XEP0421IQHandler implements PacketInterceptor
{
    private static final Logger Log = LoggerFactory.getLogger(XEP0421IQHandler.class);

    //XEP-0421
    public static String NAMESPACE_XEP0421="urn:xmpp:occupant-id:0";

    public static String NAMESPACE_DISCO_INFO = "http://jabber.org/protocol/disco#info";
    public static String NAMESPACE_MUC_USER = "http://jabber.org/protocol/muc#user";

    private XEP0421Plugin plugin;

    //Constructors
    public XEP0421IQHandler(XEP0421Plugin reference)
    {
        this.plugin=reference;
    }

    public String createKeyForMuc(JID roomjid)
    {
        String rawkey = roomjid.toBareJID()+
                         String.valueOf(System.currentTimeMillis())+
                         String.valueOf(ThreadLocalRandom.current().nextInt(1, 999 + 1));
        return org.apache.commons.codec.digest.DigestUtils.sha256Hex(rawkey);
    }

    public String getOccupantId(JID userjid,JID roomjid)
    {
        String key = this.plugin.getCache().get(roomjid.toBareJID());

        if (key==null)
        {
            key = this.plugin.getDB().getKey(roomjid);

            if (key==null)
            {
                key = createKeyForMuc(roomjid);

                if (this.plugin.getDB().insertKey(roomjid, key))
                {
                    Log.debug("Added muc private key {} for muc {} to database",key,roomjid.toBareJID());
                }
                else {
                    Log.error("Could not add key {} for muc {} to database",key,roomjid.toBareJID());
                }
            }
            if (key!=null)
            {
                this.plugin.getCache().put(roomjid.toBareJID(), key);
                Log.debug("Key {} for muc {} was added to cache",key,roomjid.toBareJID());
            }
            else
            {
                Log.error("Could not add key={} for muc {} to cache","NULL",roomjid.toBareJID());
            }
        }

        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(key.getBytes("UTF-8"), "HmacSHA256");
            sha256_HMAC.init(secret_key);

            return Hex.encodeHexString(sha256_HMAC.doFinal(userjid.toBareJID().getBytes()));
        }
        catch (Exception e)
        {
            Log.error("Could not encode occupant identifier: "+e.getMessage(),e);
            return null;
        }
    }

    private void handleOutgoingIQ(IQ iq) {

        if (iq.getType()!=Type.result||iq.getFrom()==null)
        {
            return;
        }

        try {
            MultiUserChatService service = XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(iq.getFrom());
            if (service==null)
            {
                return;
            }
        }
        catch (Exception e)
        {
            Log.error("Could not find muc: "+e.getMessage(),e);
        }

        if (iq.getChildElement()!=null)
        {
            Element childElement = iq.getChildElement();
            String childns = childElement.getNamespaceURI();

            if (childns!=null&&childns.equalsIgnoreCase(NAMESPACE_DISCO_INFO))
            {
                Element feature = childElement.addElement("feature");
                feature.addAttribute("var", NAMESPACE_XEP0421);
            }
        }
    }

    private MultiUserChatService getServiceFromJid(JID jid)
    {
        try
        {
            return XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(jid);
        }
        catch (Exception e)
        {
            Log.debug("Could not find muc service from JID \"{}\"",jid.toString());
            return null;
        }
    }

    private MultiUserChatService getServiceFromDomain(JID jid)
    {
        String service = jid.getDomain().substring(0,jid.getDomain().indexOf("."));
        try
        {
            return XMPPServer.getInstance().getMultiUserChatManager().getMultiUserChatService(service);
        }
        catch (Exception e)
        {
            Log.error("Could not find muc service from JID \"{}\" (domain {})",jid.toString(),service);
            return null;
        }
    }

    private void handleOutgoingPresence(Presence p, Session s)
    {
        Element x = p.getChildElement("x", NAMESPACE_MUC_USER);
        if (x!=null)
        {

            MultiUserChatService service = getServiceFromJid(p.getFrom());
            if (service==null)
            {
                service = getServiceFromDomain(p.getFrom());
                return;
            }

            if (service!=null)
            {

                String node = p.getFrom().getNode();

                if (node==null)
                {
                    return;
                }

                try
                {
                    MUCRoom room = service.getChatRoom(node);
                    if (room!=null)
                    {
                        JID barejid = getBareJid(room,s,p.getFrom());
                        if (barejid!=null)
                        {
                            String newid = getOccupantId(barejid,p.getFrom().asBareJID());
                            if (newid!=null)
                            {
                                Element occupant_id = p.getChildElement("occupant-id", NAMESPACE_XEP0421);
                                if (occupant_id!=null)
                                {
                                    occupant_id.detach();
                                }
                                occupant_id = p.addChildElement("occupant-id", NAMESPACE_XEP0421);
                                occupant_id.addAttribute("id", newid);
                            }
                        }
                    }

                }
                catch (Exception e)
                {
                    Log.error("Could not find muc room from JID \"{}\"",p.getFrom().toString(),e.getMessage(),e);
                }
            }
        }
    }

    private void handleOutgoingMessage(Message m, Session s)
    {

        if (m.getType()!=Message.Type.groupchat)
        {
            return;
        }

        MultiUserChatService service = getServiceFromJid(m.getFrom());
        if (service==null)
        {
            service = getServiceFromDomain(m.getFrom());
            return;
        }

        if (service!=null)
        {

            String node = m.getFrom().getNode();

            if (node==null)
            {
                return;
            }

            try
            {
                MUCRoom room = service.getChatRoom(node);
                if (room!=null)
                {
                    JID barejid = getBareJid(room,s,m.getFrom());
                    if (barejid!=null)
                    {
                        String newid = getOccupantId(barejid,m.getFrom().asBareJID());
                        if (newid!=null)
                        {
                            Element occupant_id = m.getChildElement("occupant-id", NAMESPACE_XEP0421);
                            if (occupant_id!=null)
                            {
                                occupant_id.detach();
                            }
                            occupant_id = m.addChildElement("occupant-id", NAMESPACE_XEP0421);
                            occupant_id.addAttribute("id", newid);
                        }
                    }
                }
            }
            catch (Exception e)
            {
                Log.error("Could not find muc service from JID \"{}\"",m.getFrom().toString());
            }
        }
    }
    
    private JID getBareJid(MUCRoom room, Session s, JID from) {
        List<MUCOccupant> occupants = null;

        if (!s.getAddress().toString().equalsIgnoreCase(from.toString()))
        {
            String nickname = from.getResource();
            if (nickname!=null&&room.getOccupantsCount()>0)
            {
                try 
                {
                    occupants = room.getOccupantsByNickname(nickname);
                }
                catch (Exception e)
                {
                    //user not in room?!
                    return null;
                }

                if (occupants!=null&&occupants.size()>0)
                {
                    return occupants.get(0).getUserAddress().asBareJID();
                }
                else {
                    //user not in room?!
                    return null;
                }
            }
            else
            {
                //packet from service?
                return null;
            }
        }
        else {
            return s.getAddress().asBareJID();
        }
    }

    @Override
    public void interceptPacket(Packet packet, Session session, boolean incoming, boolean processed)
            throws PacketRejectedException {

        if (XEP0421Plugin.XMPP_OCCUPANTIDENTIFIERS_ENABLED.getValue()&&packet!=null)
        {
            if (!incoming && !processed)
            {
                if (packet instanceof IQ)
                {
                    if (packet.getFrom()!=null)
                    {
                        handleOutgoingIQ((IQ)packet);
                    }
                }
                else 
                if (packet instanceof Presence) 
                {
                    if (packet.getFrom()!=null)
                    {
                        handleOutgoingPresence((Presence) packet,session);
                    }
                }
                else
                if (packet instanceof Message) 
                {
                    if (packet.getFrom()!=null)
                    {
                        handleOutgoingMessage((Message) packet,session);
                    }
                }
            }
        }
    }
}
