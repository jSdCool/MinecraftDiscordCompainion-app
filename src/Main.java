
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.jsdcool.discompnet.CAuthRequest;
import net.jsdcool.discompnet.CAuthResponce;
import net.jsdcool.discompnet.CComandList;
import net.jsdcool.discompnet.CDataType;
import net.jsdcool.discompnet.CDiscordMessageData;
import net.jsdcool.discompnet.CGamemodeCommand;
import net.jsdcool.discompnet.CKickCommand;
import net.jsdcool.discompnet.CMinecraftMessageData;
import net.jsdcool.discompnet.CPlayerPositionCommand;
import net.jsdcool.discompnet.CompanionData;
import net.jsdcool.discompnet.CShutdownData;
import net.jsdcool.discompnet.CTeleportCommand;
import net.jsdcool.discompnet.CUnAuthRequest;

public class Main extends ListenerAdapter implements ActionListener, WindowListener{

	static Socket s;
	static ServerSocket ss;
	static int port = 15643;
	static String token ="",channelid="",guildid="";
	static JDA jda;
	public static MessageChannel chatChannel;
	static JFrame frame;
	static JPanel panel;
	static JLabel status,passwordLabel,success;
	static JButton shutdownButton;
	static boolean connected=false;
	static String statusMessage="status: starting";
	static ObjectOutputStream output;
	static ObjectInputStream input;
	static CompanionData dataToSend=new CompanionData();
	static String authFileName="admins.auth";
	static AuthedUsers admins;
	
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
        try {
        	FileInputStream auths=new FileInputStream(authFileName);
        	ObjectInputStream in =new ObjectInputStream(auths);
        	admins=(AuthedUsers)in.readObject();
        	in.close();
        }catch(IOException i) {
        	admins=new AuthedUsers();
        }catch (ClassNotFoundException c) {}
        
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
        	
        	try {
        		if(s.isConnected()&&!s.isClosed()) {
        		
        	
        			CompanionData send=dataToSend;
        			dataToSend=new CompanionData();
        			output.writeObject(send);
        			output.flush();
        			output.reset();
        			CompanionData dataIn;
        			try {
        				dataIn=(CompanionData)input.readObject();
        				for(int i=0;i<dataIn.data.size();i++) {
        					if(dataIn.data.get(i) instanceof CMinecraftMessageData msg) {
        						chatChannel.sendMessage(msg.message).queue();
        					}
        					if(dataIn.data.get(i) instanceof CShutdownData) {
        						socketDisconnect();
        					}
        					if(dataIn.data.get(i) instanceof CAuthRequest dat) {
        						if(admins.ids.contains(dat.userID)) {
        							dataToSend.data.add(new CAuthResponce(dat.reqnum,false,"user is alleady authorized"));
        							//System.out.println("failed to auth");
        							continue;
        						}
        						
        						//jda.getGuildById(guildid).loadMembers();//re visit this later it is a good idea I just can't figure out how to make this work with the API
        						//List<Member> members =jda.getGuildById(guildid).getMembers();
        						if(/*hasMemberById(members,dat.userID)*/true) {
        							admins.ids.add(dat.userID);
        							saveAuths();
        							dataToSend.data.add(new CAuthResponce(dat.reqnum,true,"authorized "/*+jda.getGuildById(guildid).getMemberById(dat.userID).getUser().getName()*/));
        							//System.out.println("authed user");
        						}/*else {
        							dataToSend.data.add(new CAuthResponce(dat.reqnum,false,"could not find user"));
        							System.out.println("failed to auth");
        							continue;
        						}*/
        						
        					}
        					if(dataIn.data.get(i) instanceof CUnAuthRequest dat) {
        						if(admins.ids.contains(dat.userID)) {
        							admins.ids.remove(dat.userID);
        							saveAuths();
        							dataToSend.data.add(new CAuthResponce(dat.reqnum,true,"un authorized user"));
        						}else {
        							dataToSend.data.add(new CAuthResponce(dat.reqnum,false,"user was not authorized"));
        						}
        					}
        				}
        			}catch(EOFException e) {
        				//e.printStackTrace();
        					socketDisconnect();
        					
        			}
            
           
        		}else {
        			socketDisconnect();
        			//System.out.println("EEEEEEEEEEEEEEEEEE");
        		}
        	}catch(Exception e) {
        		e.printStackTrace();
        		socketDisconnect();
        		
        	}
            
            
            
        }//end of while 
  
          
		}catch(Throwable e) {
			e.printStackTrace();
			shutdown();
		}

	}
	
	static void createSocket() throws Exception {
		ss = new ServerSocket(port); 
		  
        // connect it to client socket 
        s = ss.accept(); 
        System.out.println("Connection established"); 
        status.setText("status: connected");
        connected =true;
        // to send data to the client 
        output = new ObjectOutputStream(s.getOutputStream()); 
  
        // to read data coming from the client 
        input  = new ObjectInputStream( s.getInputStream()); 
  
	}
	
	@Override
	public void onMessageReceived(MessageReceivedEvent event) {
        Message msg = event.getMessage();
        MessageChannel channel = event.getChannel();
        Guild guild = event.getGuild();
        User author = msg.getAuthor();
        String content = msg.getContentRaw();
        String contentSections[] = content.split(" ");
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
        
        if(content.equals("/list")) {
        	dataToSend.data.add(new CComandList());
        	System.out.println("sending list command");
        	return;
        }
        
        if(contentSections[0].equals("/tp")) {
        	if(admins.ids.contains(author.getId())){
        		if(contentSections.length<5) {
        			channel.sendMessage("missing parameters").queue();
        			return;
        		}
        		double x=0,y=0,z=0;
        		try {
        			x = Double.parseDouble(contentSections[2]);
        			y = Double.parseDouble(contentSections[3]);
        			z = Double.parseDouble(contentSections[4]);
        		}catch(NumberFormatException n) {
        			channel.sendMessage("value entered was not a number").queue();
        			return;
        		}
        		dataToSend.data.add(new CTeleportCommand(contentSections[1],x,y,z));
        		return;
        	}else {
        		channel.sendMessage("you are not authorized to use this command").queue();
        		return;
        	}
        }
        
        if(contentSections[0].equals("/pos")) {
        	if(admins.ids.contains(author.getId())){
        		if(contentSections.length<2) {
        			channel.sendMessage("missing parameters").queue();
        			return;
        		}
        		
        		dataToSend.data.add(new CPlayerPositionCommand(contentSections[1]));
        		return;
        	}else {
        		channel.sendMessage("you are not authorized to use this command").queue();
        		return;
        	}
        }
        
        if(contentSections[0].equals("/kickMC")) {
        	if(admins.ids.contains(author.getId())){
        		if(contentSections.length<2) {
        			channel.sendMessage("missing parameters").queue();
        			return;
        		}
        		String reason="";
        		for(int j=2;j<contentSections.length;j++) {
        			reason+=contentSections[j]+" ";
        		}
        		
        		dataToSend.data.add(new CKickCommand(contentSections[1],reason));
        		return;
        	}else {
        		channel.sendMessage("you are not authorized to use this command").queue();
        		return;
        	}
        }
        
        if(contentSections[0].equals("/gamemode")) {
        	if(admins.ids.contains(author.getId())){
        		if(contentSections.length<3) {
        			channel.sendMessage("missing parameters").queue();
        			return;
        		}
        		
        		dataToSend.data.add(new CGamemodeCommand(contentSections[2],contentSections[1]));
        		return;
        	}else {
        		channel.sendMessage("you are not authorized to use this command").queue();
        		return;
        	}
        }
        
        String name;
        if(event.getMember().getNickname()==null){
            name = author.getName();
        }else{
            name = event.getMember().getNickname();
        }
        List<Role> roles = event.getMember().getRoles(); 
        int roleColor=16777215;
        for(int i=roles.size()-1;i>=0;i--) {
        	if(536870911!=roles.get(i).getColorRaw())
        	roleColor = roles.get(i).getColorRaw();
        }
        
       dataToSend.data.add(new CDiscordMessageData(content,author.getName(),name,author.getId(),roleColor));
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
	
	static void socketDisconnect() throws Exception {
		try {
		status.setText("status: disconnected");
		connected=false;
		s.close();
		ss.close();
		output.close();
		input.close();
		s=null;
		ss=null;
		createSocket();
		}catch(Exception e) {
			
		}
	
	}
	static boolean hasMemberById(List<Member> mems,String id) {
		System.out.println(mems.size());
		for(int i=0;i<mems.size();i++) {
			System.out.println(mems.get(i).getUser().getId()+" "+id);
			if(mems.get(i).getUser().getId().equals(id))
				return true;
		}
		return false;
	}
	
	public static void saveAuths() {
		try {
			FileOutputStream fileOut =new FileOutputStream(authFileName);
			ObjectOutputStream out = new ObjectOutputStream(fileOut);
			out.writeObject(admins);
			out.close();
        	fileOut.close();
		}catch(IOException i) {
			i.printStackTrace();
		}
	}

}
