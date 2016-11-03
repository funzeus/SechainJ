package network;

import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;

public class Peer implements NetEventHandler{

	@Override
	public boolean handleWrite(SocketChannel ch, int count) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleRead(SocketChannel ch, ByteBuffer buffer, int count) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleConnection(SocketChannel ch) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean handleConnectionClose(SocketChannel ch) {
		// TODO Auto-generated method stub
		return false;
	}

}
