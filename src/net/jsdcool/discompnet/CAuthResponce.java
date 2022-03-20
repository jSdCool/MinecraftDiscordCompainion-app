package net.jsdcool.discompnet;

public class CAuthResponce extends CDataType {

	
	private static final long serialVersionUID = 1L;
	public String reqnum,reason;
	public boolean success;
	
	public CAuthResponce(String req,boolean resault,String reason) {
		reqnum=req;
		success=resault;
		this.reason=reason;
	}

}
