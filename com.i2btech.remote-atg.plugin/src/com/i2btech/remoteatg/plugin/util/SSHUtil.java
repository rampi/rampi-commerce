package com.i2btech.remoteatg.plugin.util;

import java.io.IOException;
import java.io.InputStream;

import javax.swing.JOptionPane;

import com.i2btech.remoteatg.plugin.event.JobListener;
import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UserInfo;


public class SSHUtil {
	
	private JSch ssh;
	
	private Session session;
	
	private String username;
	
	private String password;
	
	private String host;
	
	private int port;
	
	private JobListener listener;
	
	public SSHUtil() {
		ssh = new JSch();			
	}
	
	public void connect(String host, int port, String user, String pass)throws JSchException{
		this.username = user;
		this.password = pass;
		this.host = host;
		this.port = port;
		session = ssh.getSession(this.username, this.host, this.port);
	    session.setUserInfo(new I2bCommerceUserInfo());
	    session.setPassword(this.password);	    
	    session.connect();
	}
	
	public void reconnect()throws JSchException{
		connect(this.host, this.port, this.username, this.password);
	}
	
	public boolean isConnected(){
		return session.isConnected();
	}
	
	public String exec(String command) throws JSchException, IOException{
		
		if( session == null || !session.isConnected() || command == null ){
			return null;
		}
		
		Channel channel=session.openChannel("exec");
	    ((ChannelExec)channel).setCommand(command);
	    
	    channel.setInputStream(null);	    
	    ((ChannelExec)channel).setErrStream(System.err);
	 
	    InputStream in=channel.getInputStream();	      
	    channel.connect();
	 
	    byte[] tmp = new byte[1024];
	    StringBuilder builder = new StringBuilder();
	    
	    while(true){
	    	while(in.available()>0){
	          int i=in.read(tmp, 0, 1024);
	          if(i<0)break;
	          String msg = new String(tmp, 0, i);	          
	          if( listener != null ){	        	  
	        	  listener.write(msg);
	          }
	          builder.append(msg);
	        }	        
	    	if(channel.isClosed()){
	          if(in.available()>0) continue;	          
	          break;
	        }	        
	    	try{Thread.sleep(500);}catch(Exception ee){}	      
	    }	      
	    
	    channel.disconnect();
		
	    return builder.toString();
		
	}
	
	public void close(){
		if( session != null ){
			session.disconnect();
		}
	}
	
	class I2bCommerceUserInfo implements UserInfo{
        public void showMessage(String message){
          JOptionPane.showMessageDialog(null, message);
        }
        public boolean promptYesNo(String message){
          return true;
        }		 
		@Override
		public String getPassphrase() {
			return null;
		}
		@Override
		public String getPassword() {
			return null;
		}
		@Override
		public boolean promptPassphrase(String arg0) {
			return false;
		}
		@Override
		public boolean promptPassword(String arg0) {
			return false;
		}
    };

	public JobListener getListener() {
		return listener;
	}

	public void setListener(JobListener listener) {
		this.listener = listener;
	}
}
