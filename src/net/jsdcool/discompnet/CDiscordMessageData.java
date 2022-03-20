package net.jsdcool.discompnet;

public class CDiscordMessageData extends CDataType {

	private static final long serialVersionUID = 1L;
	public String message,name,displayName,id;
	public int nameColor;
	
	public CDiscordMessageData(String message,String name,String displayName,String id,int color) {
		this.message=message;
		this.name=name;
		this.displayName=displayName;
		this.id=id;
		nameColor=color;
	}

}
