
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class Main extends ListenerAdapter implements ActionListener, WindowListener{

	static Socket s;
	static ServerSocket ss;
	static int port = 15643;
	static String token ="",channelid="",guildid="";
	static JDA jda;
	public static MessageChannel chatChannel;
	static PrintStream ps;
	static JFrame frame;
	static JPanel panel;
	static JLabel status,passwordLabel,success;
	static JButton shutdownButton;
	static boolean connected=false;
	static BufferedReader br;
	static String statusMessage="status: starting";
	
	public Main() {
		frame= new JFrame();
		frame.setSize(400, 200);

		panel= new JPanel();
		frame.add(panel);
		frame.setVisible(true);
		frame.addWindowListener(this);
		panel.setLayout(null);
		frame.setTitle("Minecraft Discord connection companion app");
		status=new JLabel(statusMessage);
		status.setBounds(10, 20, 300, 25);
		panel.add(status);
		shutdownButton=new JButton("shutdown");
		shutdownButton.setBounds(10,80,120,25);
		shutdownButton.addActionListener(this);
		panel.add(shutdownButton);
	}
	
	public static void main(String[] args) {
		
		//status.setText("status: starting");
		System.out.println("starting");
		File config;
        Scanner cfs;
        try {
            config = new File("config.txt");
            cfs = new Scanner(config);
        }catch(Throwable e){
            try {
                FileWriter mr = new FileWriter("config.txt");
                mr.write("#botToken=\n#sendServerId=\n#sendChannelId=\n#companion port=15643");
                mr.close();
                System.out.println("config file created.");

            } catch (IOException ee) {
                System.out.println("\n\n\nAn error occurred while creating config file. you may need to make the config folder if it does not already exist\n\n\n");
                ee.printStackTrace();
                statusMessage="status: config error";
                return;
            }
            statusMessage="status: config error";
            System.out.println("\n\n\n config file created. populate the fields and then restart this server.\n\n\n");
            return;
        }
        while (cfs.hasNextLine()) {
            String line=cfs.nextLine();
            if(line.indexOf("#")==0){
                String pt1=line.substring(1,line.indexOf("="));
                String data=line.substring(line.indexOf("=")+1);
                if(pt1.equals("botToken")){
                    token=data;
                }
                if(pt1.equals("sendServerId")){
                    guildid=data;
                }
                if(pt1.equals("sendChannelId")){
                    channelid=data;
                }
                if(pt1.equals("companion port")){
                    port=Integer.parseInt(data);
                }

            }
        }
        cfs.close();
		try {
			jda = JDABuilder.createDefault(token)
			        .addEventListeners(new ReadyListener())
			        .addEventListeners(new Main())
			        .build();
			jda.awaitReady();
			status.setText("status: JDA readdy, wating for connection");
		} catch (LoginException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
			status.setText("error");
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			status.setText("error");
		}
		
        chatChannel = jda.getGuildById(guildid).getTextChannelById(channelid);
		
		try {
		 // Create server Socket 
        
			createSocket();
        // server executes continuously 
        while (true) { 
        	
            String str = null; 
  
            // read from client 
           //*
            if (br.ready())
                str = br.readLine();
           
            if (str != null) {
           
            	if(str.contains("<message>§")) {
            		String lines[] = str.substring(10, str.length()).split("\\\\\\\\n");
            		String messageOut ="";
            		for(int i=0;i<lines.length;i++) {
            			messageOut+=lines[i]+"\n";
            		}
            		
                chatChannel.sendMessage(messageOut).queue();
            	}
            	if(str.contains("<info>§")) {
            		if(str.substring(7,str.length()).equals("<shutdown>")) {
            			status.setText("status: disconnected");
            			connected=false;
            			s.close();
            			ss.close();
            			ps.close();
            			br.close();
            			s=null;
            			ss=null;
            			ps=null;
            			br=null;
            			createSocket();
            		}
            	}
            }
            
             
            
            
            
        }//end of while 
  
          
		}catch(Throwable e) {
			e.printStackTrace();
			shutdown();
		}

	}
	
	static void createSocket() throws Throwable {
		ss = new ServerSocket(port); 
		  
        // connect it to client socket 
        s = ss.accept(); 
        System.out.println("Connection established"); 
        status.setText("status: connected");
        connected =true;
        // to send data to the client 
        ps = new PrintStream(s.getOutputStream()); 
  
        // to read data coming from the client 
        br  = new BufferedReader( new InputStreamReader( s.getInputStream())); 
  
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        MessageChannel channel = event.getChannel();
        Guild guild = event.getGuild();
        User author = msg.getAuthor();
        String content = msg.getContentRaw();
        //String contentSections[] = content.split(" ");
        //System.out.println(author + " " + content);
        if(connected) {
        if (guild == null) {
            return;
        }
        if(!channel.getId().equals(channelid)){
            return;
        }
        if(author.isBot()) {
            return;
        }
        
        String name;
        if(event.getMember().getNickname()==null){
            name = author.getName();
        }else{
            name = event.getMember().getNickname();
        }
        
        ps.println("<name>\\`"+name+"\\`<message>\\`"+content);
        }
	}

	@Override
	public void windowOpened(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowClosing(WindowEvent e) {
		shutdown();
		
	}

	@Override
	public void windowClosed(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowIconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeiconified(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowActivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void windowDeactivated(WindowEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource().equals(shutdownButton)) {
			shutdown();
		}
		
	}
	
	static void shutdown() {
		try {
		jda.shutdown();
		}catch(Throwable e) {}
		System.out.println("shutting down");
		System.exit(0);
	}

}
