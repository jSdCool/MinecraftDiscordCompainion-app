package net.jsdcool.discompnet;

public class CAuthRequest extends CDataType {


	private static final long serialVersionUID = 1L;
	public String userID,reqnum;
	
	public CAuthRequest(String id,String reqn) {
		userID=id;
		reqnum=reqn;
	}
}
