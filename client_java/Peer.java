package rz;

import java.util.*;
import java.util.regex.*;

public class Peer {
    private Socket socket;
    private Map<File, BufferMap> peerFilesMaps;

    public Peer(String ip, short port) {
        try {
            socket = new Socket(ip, port);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @brief Create a peer form an inline couple: "addr:port"
     */
    public Peer(String peer) {
	String[] addrPort = peer.split(":");
	if (addrPort.length != 2) {
	    throw new RuntimeException(
                "Wrong group 'addr:port': \"" + peer + '"');
        }
        try {
            socket = new Socket(
                addrPort[0],
                Short.parseShort(addrPort[1])
            );
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String toString() {
	return String.format("[peer: %s]", socket);
    }
    
    /* -------------------- SEND -------------------- */

    public void sendInterested(File file) {
	socket.send("interested %s", file.getKey());
    }

    public void sendHave(File file) {
	socket.send("have %s %s", file.getKey(), "TODO");
    }

    public void sendGetpieces(File file, int[] index) {
	socket.send("getpieces %s [%s]", file.getKey(), indexString(index));
    }

    public void sendData(File file, int[] index) {
	socket.send("data %s [%s]", file.getKey(), "TODO");
    }

    /* -------------------- Reception -------------------- */

    public void parseInterested(String question) {
	Matcher match = PatternMatcher.INTERESTED.getMatcher(question);
	File file = Client.client.getFile(match.group(1));
	sendHave(file);
    }

    public File parseHave(String question) {
        /* this function does not work with a binary buffer map */
	Matcher match = PatternMatcher.HAVE.getMatcher(question);
        String key = match.group(1);
        String strBufferMap = match.group(2);
        
        File f = File.get(key);
	byte bufferMap[] = strBufferMap.getBytes();
        
        for (int i = 0; i < bufferMap.length; ++i) {
            byte b = bufferMap[i];
            if (b != 0) {
                peerFilesMaps.get(f).addCompletedPart(i);
            }
        }
        return f;
    }

    public void parseGetpieces(String question) {
	Matcher match = PatternMatcher.GETPIECES.getMatcher(question);
	String key = match.group(1);
	String[] strIndex = match.group(2).split("\\s+");
	int[] index = Arrays.stream(strIndex)
            .mapToInt(Integer::parseInt)
            .toArray();
	File file = Client.client.getFile(match.group(1));
        sendData(file, index);
    }

    public void parseData(String question) {
        /* this function does not work with a binary buffer map */
	Matcher match = PatternMatcher.DATA.getMatcher(question);
	File file = Client.client.getFile(match.group(1));
	String[] piecesStr = match.group(2).split("\\s+");

	for (String piece: piecesStr) {
	    String[] tmp = piece.split(":");
	    if (tmp.length != 2) {
		throw new RuntimeException("Wrong group 'pieceIndex:data'");
            }
	    int i = Integer.parseInt(tmp[0]);
	    file.addPiece(i, tmp[1].getBytes());
	}
    }

    /* -------------------- SEND RELATED -------------------- */
    private String indexString(int[] index) {
	String out = "";
	for(int i: index) {
	    out += i + " ";
	}
	return out;
    }
}
