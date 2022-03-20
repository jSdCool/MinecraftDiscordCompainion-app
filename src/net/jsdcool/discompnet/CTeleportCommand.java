package net.jsdcool.discompnet;

public class CTeleportCommand extends CDataType {

	private static final long serialVersionUID = 1L;
	public String name;
	public double x,y,z;
	public CTeleportCommand(String username,double xpos,double ypos,double zpos) {
		name=username;
		x=xpos;
		y=ypos;
		z=zpos;
	}

}
