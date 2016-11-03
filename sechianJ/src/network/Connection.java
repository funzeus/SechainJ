package network;

import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class Connection implements Runnable{

	private InetAddress hostAddress;
	private int port;
	
	private Selector selector;
	private SocketChannel channel;
	private NetEventHandler handler;
	
	private final List changeRequests = new LinkedList();
	private final List pendingData = new ArrayList();
	
	private boolean connected = false;
	
	private final int MAX_BUFF = 1024;
	
	public Connection(InetAddress hostAddress, int port, NetEventHandler handler) throws IOException{
		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = this.initSelector();
		this.handler = handler;
		this.channel = this.initiateConnection();
		
	}

	
	public Connection(SocketChannel ch, NetEventHandler handler) throws IOException{
		this.selector = this.initSelector();
		this.channel = ch;
		this.handler = handler;
		ch.configureBlocking(false);
		
		synchronized(changeRequests){
			changeRequests.add(new ChangeRequest(ch, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
		}
		
	}
	
	
	private Selector initSelector() throws IOException{
		
		return SelectorProvider.provider().openSelector();
	}
	
	private SocketChannel initiateConnection() throws IOException{
		
		SocketChannel ch = SocketChannel.open();
			
		return ch;
	}
	
	public SocketChannel getChannel(){
		return channel;
	}
	
	public boolean isConnected(){
		return connected;
	}
	
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		
	}
	
	private void finishConnection(SelectionKey key) throws IOException{
		try{
			channel.finishConnect();
		}catch(IOException e){
			key.channel();
			return;
		}
		
		key.interestOps(SelectionKey.OP_WRITE);
		
		if(handler != null && !handler.handleConnection(channel)){
			disconnect();
		}
		else{
			connected = true;
		}
		
	}
	
	private void read(SelectionKey key) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocate(MAX_BUFF);
		int count = 0;
		
		try{
			count = channel.read(buffer);
			
		}catch(IOException e){
			disconnect();
		}
		
		buffer.flip();
		
		if(count == -1 || (handler != null & !handler.handleRead(channel, buffer, count)))
			disconnect();
		
	}
	
	private void write(SelectionKey key) throws IOException{
		int count = 0;
		
		synchronized(pendingData){
			while(!pendingData.isEmpty()){
				ByteBuffer buf = (ByteBuffer) pendingData.get(0);
				channel.write(buf);
				
				count+= buf.capacity() - buf.remaining();
				
				if(buf.remaining() > 0){
					break;
				}
				
				pendingData.remove(0);
			}
			
			
			if(pendingData.isEmpty())
				key.interestOps(SelectionKey.OP_READ);
			
		}
		
		if(handler != null && !handler.handleWrite(channel, count))
			disconnect();
		
	}
	
	private void send(byte[] data){
		synchronized(changeRequests){
			changeRequests.add(new ChangeRequest(channel, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
			
			synchronized(pendingData){
				pendingData.add(ByteBuffer.wrap(data));
			}
		}
		selector.wakeup();
		
	}
	
	private void disconnect(){
		if (handler!= null && !handler.handleConnectionClose(channel))
			return;

		try {
			channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		channel.keyFor(selector).cancel();
		
		synchronized(changeRequests) {
			changeRequests.clear();
		}
		
	}
	
}
