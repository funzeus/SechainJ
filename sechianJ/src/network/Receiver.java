package network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


// Act like server 
public class Receiver implements Runnable{

	private int port;
	private InetAddress hostAddress;
	private Selector selector;
	private ServerSocketChannel channel;
	private NetEventHandler handler;
	
	private final List changeRequests = new LinkedList();
	private final Map pendingData = new HashMap();
	
	public Receiver(InetAddress hostAddress, int port) throws IOException{
		this.hostAddress = hostAddress;
		this.port = port;
		this.selector = this.initSelector();
		
	}
	
	
	private Selector initSelector() throws IOException{
		
		Selector s = SelectorProvider.provider().openSelector();
		
		channel = ServerSocketChannel.open();
		channel.configureBlocking(false);
		channel.socket().bind(new InetSocketAddress(this.hostAddress, this.port));
		channel.register(s, SelectionKey.OP_ACCEPT);
		
		if(hostAddress == null){
			hostAddress = channel.socket().getInetAddress();
		}
		
		return s;
		
	}
	
	public boolean hasChannel(SocketChannel ch){
		return ch != null && ch.keyFor(selector) != null; 
	}
	
	@Override
	public void run() {
		while(true){
			try{
				synchronized(changeRequests){
					Iterator changes = changeRequests.iterator();
					
					while(changes.hasNext()){
						ChangeRequest change = (ChangeRequest) changes.next();
						
						switch(change.type){
							case ChangeRequest.CHANGEOPS:
								SelectionKey key = change.socket.keyFor(selector);
								key.interestOps(change.ops);
						}
						
						changeRequests.clear();
					}
					selector.select();
					
					Iterator selectedKeys = selector.selectedKeys().iterator();
					
					while(selectedKeys.hasNext()){
						SelectionKey key = (SelectionKey) selectedKeys.next();
						selectedKeys.remove();
						
						if(!key.isValid()){
							continue;
						}
						
						if(key.isAcceptable())
							accept(key);
						else if(key.isReadable())
							read(key);
						else if(key.isWritable())
							write(key);
					}			
					
				}
			} catch(IOException e){
				e.printStackTrace();
			}
			
		}
			
	}
	
	
	
	public void accept(SelectionKey key) throws IOException{
		ServerSocketChannel ch = (ServerSocketChannel) key.channel();
		SocketChannel sCH = ch.accept();
		
		sCH.configureBlocking(false);
		sCH.register(selector, SelectionKey.OP_READ);
		
		if(!handler.handleConnection(sCH)){
			sCH.close();
			key.cancel();
		}

	}
	
	public void read(SelectionKey key) throws IOException{
		SocketChannel ch = (SocketChannel) key.channel();
		
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int count;
		
		try{
			count = ch.read(buffer);
		} catch(IOException e){
			close(ch);
			return;
		}
		
		buffer.flip();
		if(count == -1 || (handler != null && !handler.handleRead(ch, buffer, count))){
			close(ch);
		}
		
	}
	
	
	
	public void write(SelectionKey key) throws IOException{
		SocketChannel ch = (SocketChannel) key.channel();
		int count = 0;
		
		synchronized (pendingData){
			List queue = (List) pendingData.get(ch);
			
			if(queue == null){
				return;
			}
			
			while(!queue.isEmpty()){
				ByteBuffer buf = (ByteBuffer) queue.get(0);
				ch.write(buf);
				
				count += buf.capacity() - buf.remaining();
				
				if(buf.remaining() > 0)
					break;
				
				queue.remove(0);
			}
			
			if(queue.isEmpty())
				key.interestOps(SelectionKey.OP_READ);
						
		}
		
		if(handler.handleWrite(ch, count))
			close(ch);
	}

	
	
	public void send(SocketChannel ch, byte[] data){
		synchronized(changeRequests){
			changeRequests.add(new ChangeRequest(ch, ChangeRequest.CHANGEOPS, SelectionKey.OP_WRITE));
			
			synchronized(pendingData){
				List queue = (List) pendingData.get(ch);
				if(queue == null){
					queue = new ArrayList();
					pendingData.put(ch, queue);
				}
				
				queue.add(ByteBuffer.wrap(data));
				
			}
		}
		
		selector.wakeup();
		
	}
	
	public void close(SocketChannel ch){
		if(!handler.handleConnectionClose(ch)){
			return;
		}
		
		try{
			ch.close();
		}catch(IOException e){
			e.printStackTrace();
		}
		
		ch.keyFor(selector).cancel();
		
		synchronized(changeRequests){
			Iterator changes = changeRequests.iterator();
			
			while(changes.hasNext()){
				ChangeRequest req = (ChangeRequest) changes.next();
				
				if(req.socket == ch){
					changeRequests.remove(req);
					break;
				}
			}
			
		}
		
	}
	
}
